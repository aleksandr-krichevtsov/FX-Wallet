plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint.gradle)
}

ktlint {
    version.set(libs.versions.ktlint)
}

android {
    namespace = "io.github.aleksandrkrichevtsov.fxwallet.designsystem"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
