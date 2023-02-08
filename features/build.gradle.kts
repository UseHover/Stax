plugins {
    id("stax.android.library")
}

dependencies {
    implementation(libs.core.ktx)

    // Features
    api(project(path = ":features:transactions"))
    api(project(path = ":features:user-profile"))
}