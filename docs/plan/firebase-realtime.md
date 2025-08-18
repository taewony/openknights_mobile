
# Firebase Realtime 연동 및 Notice 기능 추가 plan
- draft at: 2025-08-18

## 1. 기능 설계

Realtime Database(RTDB)만으로 **전체 공지(브로드캐스트)**와 **개인별 공지(퍼스널)**를 안정적으로 보내는 방법을 설계부터 규칙, 클라이언트 코드까지 한 번에 정리해 드릴게요.
(푸시 알림이 필요하면 RTDB에 더해 FCM으로 “깨우기”만 추가하면 됩니다.)

### 1) 데이터 모델(권장)

RTDB는 **팬아웃(denormalize)**이 핵심입니다. 읽기 경로를 단순하게 유지하세요.

```json
{
  "announcements": {
    "A123": {
      "title": "서버 점검",
      "body": "오늘 23:00~24:00",
      "publishAt": 1723900000000,
      "endAt": 1723986400000,
      "createdBy": "adminUid",
      "priority": 5
    }
  },

  "feeds": {
    "global": {
      "A123": { "publishAt": 1723900000000 }   // 전 사용자 공지
    },
    "users": {
      "uid_001": {
        "A200": { "publishAt": 1723950000000 }  // 개인 공지
      }
    }
  },

  "user_state": {
    "uid_001": {
      "read": { "A123": 1723951000000, "A200": 1723955000000 },
      "lastOpenedAt": 1723956000000,
      "unreadCount": 1
    }
  }
}
```

* `announcements`: 원본(정규화) 콘텐츠 저장소
* `feeds/global`: 전체 공지 피드(아이디 목록 + 메타)
* `feeds/users/{uid}`: 개인 피드(해당 사용자에게만 보이는 목록)
* `user_state/{uid}`: 읽음/안읽음/카운터 등 상태

> 왜 팬아웃?
> RTDB의 쿼리는 “한 경로” 기준으로만 강력합니다. 따라서 “내가 볼 수 있는 공지”를 빠르게 가져오기 위해 `feeds/global`과 `feeds/users/{uid}`를 따로 유지합니다.

### 2) 보안 규칙(예시)

* 인증된 사용자만 읽기
* 개인 피드는 본인만
* 공지는 **피드에 존재할 때만** 읽기 가능
* 관리자만 작성/수정

```json
{
  "rules": {
    ".read": false,
    ".write": false,

    "announcements": {
      ".indexOn": ["publishAt"],
      "$aid": {
        // 공지 읽기: 글로벌 피드 또는 내 개인 피드에 있을 때만
        ".read": "auth != null && (root.child('feeds/global/'+$aid).exists() || root.child('feeds/users/'+auth.uid+'/'+$aid).exists())",
        // 관리자만 쓰기 (커스텀 클레임 admin=true 가정)
        ".write": "auth != null && auth.token.admin === true",
        // 공개 기간 제어(선택): now는 ms 타임스탬프
        ".validate": "(!newData.child('endAt').exists()) || (now <= newData.child('endAt').val())"
      }
    },

    "feeds": {
      "global": {
        ".read": "auth != null",
        "$aid": { ".read": "auth != null" }
      },
      "users": {
        "$uid": {
          ".indexOn": ["publishAt"],
          ".read": "auth != null && auth.uid === $uid",
          "$aid": { ".read": "auth != null && auth.uid === $uid" }
        }
      }
    },

    "user_state": {
      "$uid": {
        ".read": "auth != null && auth.uid === $uid",
        ".write": "auth != null && auth.uid === $uid"
      }
    }
  }
}
```

### 3) 쓰기(발행) 패턴

### 전체 공지 발행(관리자)

* ① `announcements/{aid}`에 콘텐츠 쓰기
* ② `feeds/global/{aid}`에 메타 팬아웃

### 개인 공지 발행(관리자)

* ① `announcements/{aid}`
* ② 대상 사용자들에 대해 `feeds/users/{uid}/{aid}` fan-out

> 실무에서는 **Cloud Functions(HTTP or Admin SDK)**로 한 번에 팬아웃하는 걸 권장(트랜잭션/원자성 보장).

### 4) 읽기(클라이언트, Android/Kotlin)

Gradle:

```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
implementation("com.google.firebase:firebase-database-ktx")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
```

데이터 클래스:

```kotlin
data class Announcement(
  var title: String = "",
  var body: String = "",
  var publishAt: Long = 0L,
  var endAt: Long = 0L,
  var createdBy: String = "",
  var priority: Int = 0
)
```

글로벌 피드 최신 30건 수신 → 본문 조회:

```kotlin
val db = Firebase.database

suspend fun loadGlobalAnnouncements(): List<Pair<String, Announcement>> {
    // 피드에서 아이디 목록 읽기
    val feedSnap = db.getReference("feeds/global")
        .orderByChild("publishAt")
        .limitToLast(30)
        .get().await()

    val ids = feedSnap.children.map { it.key!! }

    // 각 id의 본문을 순차/동시로 가져오기 (간단히 순차 예시)
    val list = mutableListOf<Pair<String, Announcement>>()
    for (id in ids) {
        val aSnap = db.getReference("announcements/$id").get().await()
        aSnap.getValue(Announcement::class.java)?.let { list += id to it }
    }
    return list.sortedByDescending { it.second.publishAt }
}
```

실시간 업데이트를 받으려면 `ChildEventListener` 또는 코루틴 `callbackFlow`로 `feeds/global`과 `feeds/users/{uid}`를 구독하고, 새 항목이 추가될 때마다 본문을 `announcements/{id}`에서 가져와 리스트에 반영하세요.
(왕복을 줄이고 싶다면 `feeds/*/{aid}`에 `title`, `snippet`, `thumbUrl` 정도를 **함께 저장**하는 “요약 팬아웃”이 효과적입니다.)

### 5) 읽음 처리 & 뱃지 카운트

```kotlin
fun markAsRead(uid: String, aid: String) {
  val ref = Firebase.database.getReference("user_state/$uid")
  val updates = mapOf(
    "read/$aid" to ServerValue.TIMESTAMP,
    // 간단 예시: 읽을 때 카운트 -1 (정확도 필요하면 트랜잭션 사용)
    "unreadCount" to ServerValue.increment(-1.0)
  )
  ref.updateChildren(updates)
}
```

* 새 공지 팬아웃 시 사용자별 `unreadCount`를 `++` (Functions에서 트랜잭션 권장)
* 앱 열 때 `user_state/{uid}/unreadCount`를 뱃지로 표시

### 6) 예약/만료 처리(선택)

* `publishAt`, `endAt` 필드를 두고 **규칙(now)**으로 읽기 제한 또는
* Cloud Functions(Scheduler)로 만료된 피드/원본 정리

### 7) 성능 & 비용 팁

* 항상 **limitToLast** + 정렬(`publishAt`)로 페이지네이션
* `feeds/*`에 **요약 데이터**를 팬아웃해 N+1 조회 줄이기
* 큰 본문/이미지는 Storage에 두고 RTDB에는 URL만 저장
* 인덱스: 위 규칙처럼 `.indexOn: ["publishAt"]`
* 개인 대상이 많으면 **Functions로 팬아웃**(클라이언트 루프 금지)

### 8) 푸시 알림이 필요하다면 (TBD : 추후 구현)

* RTDB는 “데이터 동기화”용, 백그라운드 알림은 **FCM**으로 보내세요.
* 발행 시 Functions에서 대상 토큰으로 FCM 전송 → 앱이 열리면 RTDB에서 실제 콘텐츠 로드.

---

## 2. 기능 개발 

### 2.1. 의존성 및 데이터 모델 설계

**팬아웃(denormalize)**이 핵심입니다. 읽기 경로를 단순하게 유지하세요.

1) **`firebase-realtime` 피처 브랜치 생성**: 신규 기능 개발을 위한 `firebase-realtime` 브랜치를 생성하고 해당 브랜치로 이동합니다.
    ```bash
    git checkout -b firebase-realtime
    ```
	
2) libs.versions.toml에 정의 추가
```
firebase-messaging = { module = "com.google.firebase:firebase-messaging" }
```
3) build.grade.kts에 의존성 추가
```
implementation(libs.firebase.messaging) // Added Messaging dependency
```

### 2.2 Firebase console에서 Realtime Database 생성

### 2.3 Notice 기능 구현

- realtime DB에 새 key-value pair가 저장되면,
- login user의 NoticeScreen에 알림 메시지가 추가된다.

**세부 구현 단계:**

1.  **Firebase Realtime Database 구조 설정 (설계 반영):**
    *   **`announcements` 노드:** 원본 공지 콘텐츠를 저장합니다.
        *   필드: `title`, `body`, `publishAt`, `endAt`, `createdBy`, `priority`
        *   인덱스: `publishAt`에 `.indexOn` 설정
    *   **`feeds/global` 노드:** 전체 공지 ID 목록을 저장합니다.
        *   필드: `publishAt` (팬아웃된 메타데이터)
    *   **`feeds/users/{uid}` 노드:** 특정 사용자에게만 보이는 공지 ID 목록을 저장합니다.
        *   필드: `publishAt` (팬아웃된 메타데이터)
        *   인덱스: `publishAt`에 `.indexOn` 설정
    *   **`user_state/{uid}` 노드:** 사용자의 공지 읽음 상태 및 카운터를 저장합니다.
        *   필드: `read/{aid}` (읽은 시간), `lastOpenedAt`, `unreadCount`

2.  **Firebase Realtime Database 보안 규칙 설정 (설계 반영):**
    *   `docs/plan/firebase-realtime.md`의 "1.2 보안 규칙(예시)" 섹션에 제시된 규칙을 적용합니다.
    *   주요 규칙:
        *   인증된 사용자만 읽기 허용.
        *   개인 피드는 본인만 읽기/쓰기 가능.
        *   `announcements`는 해당 공지가 `feeds/global` 또는 `feeds/users/{uid}`에 존재할 때만 읽기 가능.
        *   관리자만 `announcements`에 쓰기 가능 (Cloud Functions를 통한 발행 권장).
        *   `user_state`는 본인만 읽기/쓰기 가능.

3.  **Android 앱 구현:**

    *   **`core/model` 모듈:**
        *   `Announcement` 데이터 클래스를 정의합니다. (설계 섹션의 `data class Announcement` 참조)
        *   필요하다면 `FeedItem` (ID와 publishAt만 포함) 등 보조 데이터 클래스를 정의합니다.

    *   **`core/data` 모듈:**
        *   **`AnnouncementRepository` 인터페이스 및 구현체 생성:**
            *   Firebase Realtime Database와 상호작용합니다.
            *   **알림 리스너 구현:**
                *   현재 로그인한 사용자의 UID를 기반으로 `feeds/global`과 `feeds/users/{uid}` 두 경로를 동시에 구독합니다.
                *   각 피드에서 `Announcement` ID를 수신하면, 해당 ID를 사용하여 `announcements/{aid}` 경로에서 실제 `Announcement` 본문 데이터를 가져옵니다.
                *   두 피드에서 가져온 `Announcement` 목록을 합치고, 중복을 제거하며, `publishAt` 기준으로 최신순으로 정렬하여 `Flow` 또는 `LiveData`를 통해 상위 레이어에 전달합니다.
                *   `limitToLast`를 사용하여 페이지네이션을 구현합니다.
            *   **알림 읽음 상태 업데이트 기능:**
                *   `markAsRead(uid: String, aid: String)` 메서드를 구현하여 `user_state/{uid}/read/{aid}`에 읽은 시간을 기록하고, `unreadCount`를 감소시킵니다. (설계 섹션의 `markAsRead` 함수 참조)
            *   **unreadCount 구독:**
                *   `user_state/{uid}/unreadCount`를 구독하여 실시간으로 뱃지 카운트를 제공합니다.

    *   **`feature/notice` (또는 `app` 모듈 내 해당 UI 패키지):**
        *   **`AnnouncementViewModel` 생성:**
            *   `AnnouncementRepository`를 주입받아 사용합니다.
            *   `AnnouncementRepository`에서 노출하는 `Announcement` 목록 `Flow`를 수집하여 UI 상태로 노출합니다.
            *   `unreadCount` `Flow`를 수집하여 UI에 뱃지 카운트로 표시합니다.
            *   알림 읽음 상태 업데이트 요청을 처리합니다.
        *   **`NoticeScreen` (Compose UI) 구현:**
            *   `AnnouncementViewModel`에서 노출하는 `Announcement` 목록을 표시합니다.
            *   각 `Announcement`의 `publishAt`을 기준으로 정렬하여 표시합니다.
            *   새 알림이 도착했을 때 시각적인 피드백 (예: 목록 상단에 추가, 새로운 알림 배지, 스낵바)을 제공합니다.
            *   각 알림 항목 클릭 시 상세 내용 표시 및 읽음 상태 업데이트 로직을 연결합니다.
            *   알림이 없을 경우 "알림 없음" 메시지를 표시합니다.
            *   `unreadCount`를 사용하여 앱 아이콘 또는 알림 탭에 뱃지를 표시합니다.


``` sample data:
  announcement:

    {
      "announcements": {
        "G001": {
          "title": "시스템 업데이트 안내",
          "body": "더 나은 서비스 제공을 위해 시스템 업데이트가 예정되어 있습니다. 서비스 이용에
      참고 부탁드립니다.",
          "publishAt": 1724000000000,
          "endAt": 1724200000000,
          "createdBy": "admin_user_id",
          "priority": 10
        }
      },
      "feeds": {
        "global": {
          "G001": {
            "publishAt": 1724000000000
          }
        }
      }
    }
   ```
   
   
---
JSON 데이터를 **Firebase Realtime Database 콘솔**에 직접 입력하고, 모바일 앱에서 잘 조회되는지 확인하려면 아래 순서대로 진행하면 됩니다.

## 1. Firebase Realtime DB에 데이터 입력

1. [Firebase Console](https://console.firebase.google.com/) 접속 → 프로젝트 선택
2. 왼쪽 메뉴에서 **Build → Realtime Database** 선택
3. `데이터(Data)` 탭 → "데이터베이스 위치" 클릭
4. 루트(`/`) 위치에서 `{ ... }` JSON을 붙여 넣거나,
   필요한 경로(`/announcements`, `/feeds`)로 이동해서 수동으로 입력

👉 예: 전체를 붙여넣을 경우

```json
{
  "announcements": {
    "G001": {
      "title": "시스템 업데이트 안내",
      "body": "더 나은 서비스 제공을 위해 시스템 업데이트가 예정되어 있습니다. 서비스 이용에 참고 부탁드립니다.",
      "publishAt": 1724000000000,
      "endAt": 1724200000000,
      "createdBy": "admin_user_id",
      "priority": 10
    }
  },
  "feeds": {
    "global": {
      "G001": {
        "publishAt": 1724000000000
      }
    }
  }
}
```

---

## 2. 모바일 앱에서 읽기 (예: Android Kotlin, Jetpack Compose)

Firebase Realtime DB를 읽는 방법은 `DatabaseReference`를 사용합니다.

```kotlin
import com.google.firebase.database.*

data class Announcement(
    val title: String? = null,
    val body: String? = null,
    val publishAt: Long? = null,
    val endAt: Long? = null,
    val createdBy: String? = null,
    val priority: Int? = null
)

fun fetchAnnouncements() {
    val db = FirebaseDatabase.getInstance()
    val ref = db.getReference("announcements")

    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (child in snapshot.children) {
                val announcement = child.getValue(Announcement::class.java)
                println("공지 ID=${child.key}, 데이터=$announcement")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            println("데이터 로드 실패: ${error.message}")
        }
    })
}
```

👉 실행 시 콘솔에 저장한 `"G001"` 데이터가 잘 조회되는지 확인할 수 있습니다.

---

## 3. 확인 방법

* 콘솔에 입력한 JSON → 앱 실행 → `fetchAnnouncements()` 호출
* **Logcat** (또는 println 출력)에서 데이터 출력 확인
* 잘 조회된다면, UI (예: `LazyColumn`)에 뿌려서 공지 리스트 형태로 보여줄 수 있습니다.

---

## 4. 개인별 공지 확인 방법 (향후)

* `/feeds/{uid}/{공지ID}` 형태로 개인별 경로를 만들어서 사용자별 공지를 구분
* 앱에서 로그인된 `uid` 기준으로 `db.getReference("feeds").child(uid)` 조회
* 해당 키에 연결된 `announcements` ID를 가져와 상세 내용을 `/announcements/{id}` 에서 다시 읽기

---

👉 지금 단계에서는 **Firebase Console → Data 탭 → JSON 붙여넣기 → 앱에서 ValueEventListener로 조회** 까지만 확인하면 충분합니다.

git add . && git commit -m "feat: complete realtime DB feature"
git checkout main
git pull origin main
git merge firebase-realtime
git push origin main
git push origin firebase-realtime
