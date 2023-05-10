pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://sdk.uxcam.com/android/")
        maven {
            url = uri("http://maven.usehover.com/releases")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("http://maven.usehover.com/snapshots")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("https://pro.maven.usehover.com/snapshots")
            credentials {
                username = providers.gradleProperty("HoverUsername").orNull
                password = providers.gradleProperty("HoverPassword").orNull
            }
            authentication { create<BasicAuthentication>("basic") }
            content { includeGroup("com.hover") }
        }
    }
}

rootProject.name = "Stax"

include(":app")

include(":internal:network")
include(":internal:database")
include(":internal:resources")

include(":features")
include(":features:user-profile")
include(":features:transactions")

include(":hover.sdk")
include(":internal:analytics")
include(":internal:remoteconfig")
include(":internal:sync")
include(":internal:testing")
include(":internal:ui")
include(":internal:datastore")
include(":internal:model")
include(":internal:data")
include(":internal:core")
include(":internal:firestore")