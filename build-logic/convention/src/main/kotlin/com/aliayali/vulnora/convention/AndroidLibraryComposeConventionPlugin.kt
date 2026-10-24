package com.aliayali.vulnora.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {

            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>()
                .named("libs")

            dependencies {
                add(
                    "implementation",
                    platform(libs.findLibrary("androidx.compose.bom").get())
                )
                add(
                    "implementation",
                    libs.findLibrary("androidx.compose.ui").get()
                )
                add(
                    "implementation",
                    libs.findLibrary("androidx.compose.ui.graphics").get()
                )
                add(
                    "implementation",
                    libs.findLibrary("androidx.compose.ui.tooling.preview").get()
                )
                add(
                    "implementation",
                    libs.findLibrary("androidx.compose.material3").get()
                )

                add(
                    "androidTestImplementation",
                    platform(libs.findLibrary("androidx.compose.bom").get())
                )
                add(
                    "androidTestImplementation",
                    libs.findLibrary("androidx.compose.ui.test.junit4").get()
                )

                add(
                    "debugImplementation",
                    libs.findLibrary("androidx.compose.ui.test.manifest").get()
                )
                add(
                    "debugImplementation",
                    libs.findLibrary("androidx.compose.ui.tooling").get()
                )
            }
        }
    }
}