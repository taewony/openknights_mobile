plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization) // Kotlinx Serialization 플러그인 적용
}

android {
    namespace = "com.openknights.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.kotlinx.serialization.json) // JSON 직렬화 라이브러리

    implementation(project(":core:model"))

    // Firebase Integration
    implementation(platform(libs.firebase.bom)) // Firebase 버전 통합 관리
    implementation(libs.firebase.common) // Firebase 진입점(Firebase.*)
    implementation(libs.firebase.firestore)
    implementation(libs.coroutines.play.services) // await() 지원
}