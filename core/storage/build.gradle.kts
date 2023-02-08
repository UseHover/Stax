plugins {
    id("stax.android.library")
    id("kotlin-kapt")
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
}