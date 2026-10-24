package com.aliayali.vulnora.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryDesugaringConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.configure<LibraryExtension> {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                    isCoreLibraryDesugaringEnabled = true
                }
            }

            val libs = extensions
                .getByType<VersionCatalogsExtension>()
                .named("libs")

            dependencies {
                add(
                    "coreLibraryDesugaring",
                    libs.findLibrary("android-desugar-jdk-libs").get()
                )
            }
        }
    }
}