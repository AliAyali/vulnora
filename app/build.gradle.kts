plugins {
    alias(libs.plugins.vulnora.android.application)
    alias(libs.plugins.vulnora.android.application.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.aliayali.vulnora"

    defaultConfig {
        applicationId = "com.aliayali.vulnora"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true
    }

}

dependencies {
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}