
# OpenKnights App Data Model Guide

## 소개

이 문서는 OpenKnights 앱의 핵심 데이터 모델에 대한 개요를 제공합니다. 이 앱은 경진대회(Contest)를 중심으로 프로젝트(Project)와 사용자(User) 데이터를 관리합니다. 각 데이터 모델은 Kotlin의 `data class`로 정의되어 있으며, `kotlinx.serialization` 라이브러리를 통해 직렬화됩니다.

## 핵심 모델

### `User`

`User` 모델은 앱의 사용자를 나타냅니다. 학생, 심사위원, 멘토 등 다양한 역할을 가질 수 있습니다.

- `uid`: Firebase Auth에서 발급하는 고유 식별자입니다.
- `studentId`: 학번
- `email`: 사용자 이메일
- `name`: 사용자 이름
- `introduction`: 자기소개
- `imageUrl`: 프로필 이미지 URL
- `roles`: 사용자가 가진 역할 목록입니다. (`Role` enum 참조)
- `projects`: 참여하고 있는 프로젝트의 ID 목록입니다.

```kotlin
@Serializable
data class User (
    val uid: String = "", // Firebase Auth UID, 기본 식별자
    val studentId: String = "", // 학번,
    val email: String = "", // 사용자 이메일
    val name: String = "",
    val introduction: String = "",
    val imageUrl: String = "",
    val roles: List<Role> = emptyList(),
    val projects: List<String> = emptyList(), // 프로젝트 ID 목록
    val id: Long = 0L // 기존 id는 더미 데이터로 처리
)
```

### `Project`

`Project` 모델은 경진대회에 제출된 프로젝트를 나타냅니다.

- `id`: 프로젝트의 고유 식별자입니다.
- `name`: 프로젝트 이름
- `term`: 경진대회 기간 (예: "2025-1")
- `phase`: 프로젝트의 현재 진행 단계입니다. (`Phase` enum 참조)
- `teamName`: 팀 이름
- `leaderName`: 팀장 이름
- `leaderId`: 팀장 `User`의 `uid`
- `members`: 팀원들의 `User` `uid` 목록입니다.
- `language`: 주 사용 언어
- `description`: 프로젝트 설명
- `mentor`: 멘토 `User`의 `uid`
- `note`: 메모
- `preTotal`: 예선 총점
- `preScore`: 예선 점수 (`Score` 모델 참조)
- `finalTotal`: 본선 총점
- `finalScore`: 본선 점수 (`Score` 모델 참조)

```kotlin
@Serializable
data class Project(
    val id: Long = 0L,
    val name: String = "",
    val term: String = "",
    val phase: Phase = Phase.PLANED,
    val teamName: String = "",
    val leaderName: String = "",
    val leaderId: String = "",
    val members: List<String> = emptyList(),
    val language: String = "",
    val description: String = "",
    val mentor: String = "",
    val note: String = "",
    val preTotal: Int = 0,
    val preScore: Score = Score(0, 0, 0, 0),
    val finalTotal: Int = 0,
    val finalScore: Score = Score(0, 0, 0, 0)
)
```

### `Contest`

`Contest` 모델은 경진대회 정보를 나타냅니다.

- `id`: 경진대회의 고유 식별자입니다.
- `term`: 경진대회 기간 (예: "2025-1")
- `description`: 경진대회 설명
- `staff`: 운영진 `User`의 `uid` 목록입니다.
- `phase`: 현재 경진대회 진행 단계입니다. (`Phase` enum 참조)

```kotlin
@Serializable
data class Contest(
    val id: Long = 0,
    val term: String = "",
    val description: String = "",
    val staff: List<String> = emptyList(),
    val phase: Phase? = null
)
```

### `Schedule`

`Schedule` 모델은 경진대회의 주요 일정을 나타냅니다.

- `id`: 일정의 고유 식별자입니다.
- `term`: 경진대회 기간 (예: "2025-1")
- `contestStart`: 대회 시작일
- `preRoundStart`: 예선 시작일
- `finalRoundStart`: 본선 시작일
- `contestEnd`: 대회 종료일

```kotlin
data class Schedule(
    val id: Long,
    val term: String,
    val contestStart: LocalDate?,
    val preRoundStart: LocalDate?,
    val finalRoundStart: LocalDate?,
    val contestEnd: LocalDate?
)
```

## Enums

### `Role`

`Role` enum은 사용자가 가질 수 있는 역할을 정의합니다.

- `ADMIN`: 관리자
- `JUDGE_PRELIMINARY`: 예선 심사위원
- `JUDGE_FINAL`: 본선 심사위원
- `STAFF`: 운영진
- `MENTOR`: 멘토
- `TEAM_MEMBER`: 팀원
- `TEAM_LEADER`: 팀장
- `GUEST`: 게스트

```kotlin
@Serializable
enum class Role {
    ADMIN,
    JUDGE_PRELIMINARY,
    JUDGE_FINAL,
    STAFF,
    MENTOR,
    TEAM_MEMBER,
    TEAM_LEADER,
    GUEST
}
```

### `Phase`

`Phase` enum은 프로젝트의 진행 단계를 나타냅니다.

- `PLANED`: 예정
- `REGISTERED`: 등록
- `PRELIMINARY_SUBMITTED`: 예선 제출
- `PRELIMINARY_PASSED`: 예선 통과
- `FINAL_SUBMITTED`: 본선 제출
- `FINALIST`: 본선 진출
- `PRESENTATION`: 본선 발표
- `AWARDED_GRAND`: 대상 수상
- `AWARDED_EXCELLENCE`: 최우수상 수상
- `AWARDED_ENCOURAGEMENT`: 우수상 수상
- `DELETED`: 삭제
- `FINISHED`: 종료

```kotlin
@Serializable
enum class Phase(val label: String) {
    PLANED("예정"),
    REGISTERED("등록"),
    PRELIMINARY_SUBMITTED("예선 제출"),
    PRELIMINARY_PASSED("예선 통과"),
    FINAL_SUBMITTED("본선 제출"),
    FINALIST("본선 진출"),
    PRESENTATION("본선 발표"),
    AWARDED_GRAND("대상 수상"),
    AWARDED_EXCELLENCE("최우수상 수상"),
    AWARDED_ENCOURAGEMENT("우수상 수상"),
    DELETED("삭제"),
    FINISHED("종료")
}
```

## 지원 모델

### `Score`

`Score` 모델은 프로젝트의 점수를 나타냅니다.

- `usability`: 사용성 점수
- `techStack`: 기술 스택 점수
- `creativity`: 창의성 점수
- `completeness`: 완성도 점수

```kotlin
@Serializable
data class Score(
    val usability: Int = 0,
    val techStack: Int = 0,
    val creativity: Int = 0,
    val completeness: Int = 0,
)
```
