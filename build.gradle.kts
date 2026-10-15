// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktlint.gradle) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":domain"))
    kover(project(":app"))
}

kover {
    currentProject {
        createVariant("coverage") {}
    }
    reports {
        variant("coverage") {
            filters {
                includes {
                    classes(
                        "io.github.aleksandrkrichevtsov.fxwallet.domain.*",
                        "*ViewModel",
                        "*ViewModel$*",
                    )
                }
                excludes {
                    androidGeneratedClasses()
                }
            }
            verify {
                rule {
                    minBound(80)
                }
            }
        }
    }
}
