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
plugins {
    id("stax.android.library")
    id("stax.android.hilt")
    id("kotlin-kapt")
    id("kotlinx-serialization")
}

android {
    namespace = "com.hover.stax.core"
}

dependencies {
    implementation(project(path = ":internal:resources"))
    
    // Google
    implementation(libs.bundles.google)
    kapt(libs.lifecycle.common)

    implementation(libs.kotlinx.serialization)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:31.2.2"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    // logging
    implementation(libs.bundles.logging)
}