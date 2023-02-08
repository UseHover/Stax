plugins {
    id("stax.android.library")
}

dependencies {
    // Modules
    api(project(path = ":core:network"))
    api(project(path = ":core:resources"))
    api(project(path = ":core:storage"))
}