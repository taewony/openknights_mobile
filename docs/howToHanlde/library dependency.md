**Kotlin Serialization 플러그인**과 **`kotlinx-serialization-json`** 라이브러리를
**Gradle + Kotlin DSL** 환경에서, **모듈의 `build.gradle.kts`** 와 **`libs.versions.toml`** 두 곳만 수정하는 **가장 간결하고 안정적인 방법**을 보여줄게.

---

## 1. `gradle/libs.versions.toml`

```toml
[versions]
kotlin = "2.0.21"
kotlinx-serialization = "1.8.1"

[plugins]
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }

[libraries]
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
```

> **포인트**
>
> * 플러그인 `org.jetbrains.kotlin.plugin.serialization`의 버전은 **Kotlin 컴파일러 버전(`2.0.21`)과 동일**하게 설정.
> * `kotlinx-serialization-json` 런타임 라이브러리는 안정 버전(`1.8.1`) 사용.

---

## 2. `app/build.gradle.kts` (모듈)

```kotlin
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization) // Kotlinx Serialization 플러그인 적용
}

dependencies {
    implementation(libs.kotlinx.serialization.json) // JSON 직렬화 라이브러리
}
```

---

이렇게 하면

* 플러그인과 라이브러리 버전을 **모두 `libs.versions.toml`에서 중앙 관리**
* 빌드 스크립트는 최소한의 설정만 남아서 간결
* Kotlin 버전과 Serialization 플러그인 버전 불일치 문제 방지

