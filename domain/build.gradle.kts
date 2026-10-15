plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.ktlint.gradle)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

ktlint {
    version.set(libs.versions.ktlint)
}

kover {
    currentProject {
        createVariant("coverage") {
            add("jvm")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
