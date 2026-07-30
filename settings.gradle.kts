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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Aivance"
include(":app")

// Core modules
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:util")
include(":core:designsystem")
include(":core:sdk")
include(":core:ai-providers")
include(":core:job-providers")

// Feature modules
include(":feature:dashboard")
include(":feature:resume")
include(":feature:ats")
include(":feature:coverletter")
include(":feature:tracker")
include(":feature:interview")
include(":feature:jobs")
include(":feature:profile")

// Navigation module
include(":navigation")
