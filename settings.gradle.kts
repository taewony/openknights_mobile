pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // <-- 이 줄이 가장 중요합니다. 안드로이드 플러그인은 여기에 있습니다.
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "openknights_mobile"
include(":app")
include(":core:designsystem")
include(":core:ui")
include(":core:data")
include(":core:model")
include(":feature")

