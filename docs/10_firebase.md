# Firebase 연동 가이드

이 문서는 OpenKnights 앱과 Firebase를 연동하기 위한 기술적인 가이드와 정책을 담고 있습니다.

---

## Firestore 데이터 접근 정책: 인증과 보안 규칙

Firebase Firestore 데이터베이스에 저장된 데이터에 접근하기 위한 정책을 정의합니다.

### 핵심 질문: 데이터를 가져오려면 로그인이 필요한가요?

결론부터 말하면, **"프로젝트에 설정된 Firestore 보안 규칙(Security Rules)에 따라 다릅니다."**

Firestore는 규칙을 어떻게 설정하느냐에 따라 로그인한 사용자만 접근하게 할 수도 있고, 로그인하지 않은 사용자(Guest)에게도 제한적으로 허용할 수 있습니다. 보안 규칙은 우리 데이터베이스의 "문지기" 역할을 하며, 모든 데이터 요청은 이 문지기의 허락을 받아야 합니다.

아래는 두 가지 대표적인 시나리오입니다.

### 시나리오 1: 로그인 필수 (Authenticated Access)

가장 일반적이고 안전한 방식입니다. 사용자의 개인 정보 등 민감한 데이터를 다룰 때 사용됩니다. 이 경우, 보안 규칙은 다음과 같이 설정됩니다.

```rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 'users' 컬렉션의 모든 문서에 대해
    match /users/{userId} {
      // 요청을 보낸 사용자가 로그인 상태일 때만 읽기/쓰기를 허용합니다.
      allow read, write: if request.auth != null;
    }
  }
}
```

*   **`request.auth != null`**: 이 규칙의 핵심입니다. Firestore로 데이터를 요청한 사용자가 유효한 인증 정보(`auth`)를 가지고 있을 때, 즉 **로그인한 상태일 때만** 접근을 허용한다는 의미입니다.
*   이 규칙 하에서 로그인을 하지 않고 데이터를 요청하면, Firestore는 `PERMISSION_DENIED` 오류를 반환하며 요청을 거부합니다.

### 시나리오 2: 로그인 없이 읽기 가능 (Guest Access)

공지사항, 공개 프로필, 랭킹 등 모든 사용자에게 공개해도 되는 데이터를 다룰 때 사용합니다.

```rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 'users' 컬렉션의 모든 문서에 대해
    match /users/{userId} {
      // 읽기(read)는 누구나 허용하지만,
      allow read: if true;
      // 쓰기(write)는 로그인한 사용자만 허용합니다.
      allow write: if request.auth != null;
    }
  }
}
```

*   **`allow read: if true;`**: 이 규칙은 **조건 없이 모든 읽기 요청을 허용**하겠다는 의미입니다. 따라서 로그인하지 않은 사용자도 `users` 컬렉션의 데이터를 읽을 수 있습니다.
*   **⚠️ 보안 경고:** 데이터를 쓰는 것(`write`)까지 `if true`로 설정하는 것은 매우 위험합니다. 악의적인 사용자가 데이터베이스를 마음대로 쓰고 지울 수 있게 되므로, 쓰기 권한은 항상 특정 조건(예: 로그인한 사용자)을 만족할 때만 부여해야 합니다.

### 🤷‍♂️ 우리 프로젝트는 어떤 방식인가요? (How to Check)

우리 프로젝트의 정책을 확인하는 방법은 두 가지입니다.

1.  **Firebase Console에서 확인:**
    *   Firebase 콘솔([https://console.firebase.google.com/](https://console.firebase.google.com/))에 접속합니다.
    *   프로젝트를 선택하고, **빌드 > Firestore Database** 메뉴로 이동합니다.
    *   상단의 **규칙(Rules)** 탭을 클릭하면 현재 적용된 보안 규칙을 직접 볼 수 있습니다.

2.  **프로젝트 파일에서 확인:**
    *   프로젝트 루트 디렉토리에서 `firestore.rules` 라는 이름의 파일을 찾아 내용을 확인합니다.
