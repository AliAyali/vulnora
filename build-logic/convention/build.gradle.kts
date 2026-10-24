
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.aliayali.vulnora.buildlogic"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibraryDesugaring") {
            id = "com.aliayali.vulnora.android.library.desugaring"
            implementationClass =
                "com.aliayali.vulnora.convention.AndroidLibraryDesugaringConventionPlugin"
        }

        register("androidApplication") {
            id = "com.aliayali.vulnora.android.application"
            implementationClass =
                "com.aliayali.vulnora.convention.AndroidApplicationConventionPlugin"
        }

        register("androidLibrary") {
            id = "com.aliayali.vulnora.android.library"
            implementationClass =
                "com.aliayali.vulnora.convention.AndroidLibraryConventionPlugin"
        }

        register("androidCompose") {
            id = "com.aliayali.vulnora.android.compose"
            implementationClass =
                "com.aliayali.vulnora.convention.AndroidComposeConventionPlugin"
        }

        register("androidLibraryCompose") {
            id = "com.aliayali.vulnora.android.library.compose"
            implementationClass =
                "com.aliayali.vulnora.convention.AndroidLibraryComposeConventionPlugin"
        }
    }
}