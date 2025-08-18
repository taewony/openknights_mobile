import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.openknights.feature"
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
    buildFeatures {
        compose = true
    }

}

dependencies {

    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.androidx.compose.bom)) // Ensure libs.androidx.compose.bom is in TOML

    implementation(libs.androidx.ui) // Changed
    implementation(libs.androidx.ui.graphics) // Changed
    implementation(libs.androidx.ui.tooling.preview) // Changed
    implementation(libs.androidx.material3) // Changed
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Ensure libs.androidx.compose.bom is in TOML
    androidTestImplementation(libs.androidx.ui.test.junit4) // Changed
    debugImplementation(libs.androidx.ui.tooling) // Changed
    debugImplementation(libs.androidx.ui.test.manifest) // Changed
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)       // Auth API
    implementation(libs.firebase.storage)   // Storage API
    implementation(libs.firebase.common) // Firebase 클래스 제공
    implementation(libs.firebase.firestore) // Added Firestore dependency
    implementation(libs.firebase.messaging) // Added Messaging dependency
    implementation(libs.firebase.database)  // Firebase Realtime Database 클라이언트 SDK
    implementation(libs.coroutines.play.services) // 코루틴: Firebase Task → suspend 함수 변환 지원


    implementation(libs.kotlinx.immutable)
    implementation(libs.coil) // for AsyncImage
}