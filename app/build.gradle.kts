/*
 * Copyright 2023 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.firebase.firebase-perf")
    id("androidx.navigation.safeargs")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if ((requested.group == "org.jetbrains.kotlin") && (requested.name.startsWith("kotlin-stdlib"))) {
                useVersion("1.8.0")
            }
        }
    }
}

group = "com.hover"
version = "1.19.14"

android {

    namespace = "com.hover.stax"

    compileSdk = 33

    defaultConfig {
        applicationId = "com.hover.stax"
        minSdk = 21
        targetSdk = 33
        versionCode = 224
        versionName = project.version.toString()
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0"
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
    }

    val userHome = Paths.get(System.getProperty("user.home"))
    val keystorePath = userHome.resolve(".stax/keystore.properties")
    val keystorePropertiesFile = file(keystorePath)
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("releaseConfig") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            withGroovyBuilder {
                "FirebasePerformance" {
                    invokeMethod("setInstrumentationEnabled", false)
                }
            }
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            if (keystoreProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("releaseConfig")
            }
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("staging") {
            // Assigns this product flavor to the "version" flavor dimension.
            // If you are using only one dimension, this property is optional,
            // and the plugin automatically assigns all the module's flavors to
            // that dimension.
            dimension = "version"
        }
        create("production") {
            dimension = "version"
        }
    }

    bundle {
        language {
            // Ensures all language string resources is bundled in the aab.
            enableSplit = false
        }
    }

    lint {
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("debug").assets.srcDirs(files("$projectDir/schemas"))
    }

    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel4api30").apply {
                    device = "Pixel 4"
                    apiLevel = 30
                    // ATDs currently support only API level 30.
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }
}

dependencies {
    // Modules
    implementation(project(path = ":core"))
    implementation(project(path = ":features"))

    // Google
    implementation(libs.bundles.google)
    kapt(libs.lifecycle.common)

    // compose
    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.tooling)
    androidTestImplementation(libs.compose.ui.test)

    // logging
    implementation(libs.bundles.logging)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:31.1.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-inappmessaging-display")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-perf")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.play:core:1.10.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    // Auth
    implementation(libs.auth)

    // Networking
    implementation(libs.bundles.network)

    implementation(libs.datastore)

    implementation(libs.phoenix)

    implementation(libs.libphonenumber)

    implementation(libs.lingver)

    // Images
    implementation(libs.bundles.image)
    kapt(libs.glide.compiler)

    // Room
    implementation(libs.bundles.room)
    kapt(libs.room.compiler)
    androidTestImplementation(libs.room.test)

    // DI
    implementation(libs.bundles.koin)

    // Tests
    testImplementation(libs.bundles.test)

    androidTestImplementation(libs.junit.androidx)
    androidTestImplementation(libs.espresso)

    // Hover SDK
    "stagingImplementation"(project(":hover.sdk"))
    "stagingImplementation"(libs.bundles.hover)
    "productionImplementation"(libs.hover)

    implementation(files("libs/named-regexp.jar"))
}

abstract class VersionTask : DefaultTask() {
    @TaskAction
    fun printVersion() {
        println(project.version)
    }
}

tasks.register<VersionTask>("printVersion")