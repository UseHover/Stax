plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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

    api(libs.appcompat)
    api(libs.material)
    implementation(libs.core.ktx)
}