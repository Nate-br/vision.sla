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
        google()
        mavenCentral()
    }
}

rootProject.name = "VisionBetterLifeSLA"

// ─── App ──────────────────────────────────────────────────────────────────────
include(":app")

// ─── Core modules ─────────────────────────────────────────────────────────────
include(":core:designsystem")
include(":core:ui")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:security")
include(":core:model")

// ─── Feature modules ──────────────────────────────────────────────────────────
include(":feature:onboarding")
include(":feature:home")
include(":feature:payments")
include(":feature:loans")
include(":feature:savings")
include(":feature:profile")
include(":feature:notifications")
include(":feature:support")
