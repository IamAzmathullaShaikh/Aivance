plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.bangersoul.aivance.core.util"
    compileSdk = 37
    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.timber)
    implementation(libs.pdfbox.android)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.tink.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.core)
}
