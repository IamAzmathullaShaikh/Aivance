plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.bangersoul.aivance.core.data"
    compileSdk = 37
    defaultConfig {
        minSdk = 26
    }
    testOptions {
        unitTests {
            // Allow android.util.Log etc. to no-op in JVM unit tests instead of throwing
            // "Method ... not mocked" RuntimeExceptions.
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:common"))
    implementation(project(":core:sdk"))
    implementation(project(":core:util"))

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.common)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    implementation(libs.timber)

    // Play Integrity SDK (device/app attestation) — real implementation in
    // PlayIntegrityManagerImpl, with graceful degradation when Play Services
    // is absent. kotlinx-coroutines-play-services bridges Task → suspend.
    implementation(libs.play.integrity)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.core.ktx)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
