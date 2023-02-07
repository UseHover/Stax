plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        compileSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    /**
     * Room
     * Make me internal once all classes that require Room
     * are migrated to storage module
     */
    api(libs.bundles.room)
    kapt(libs.room.compiler)
    androidTestApi(libs.room.test)

    // DI
    implementation(libs.bundles.koin)

    // Tests
    testImplementation(libs.bundles.test)
}