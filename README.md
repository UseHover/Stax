# Stax

### Public Setup
This project requires the Hover SDK. Because access to the SDK is restricted for security reasons, the simplest way to get this project to build is to change some of the dependencies to use the basic release version of the SDK. First, remove the pro dependency from the root `build.gradle` file:
```
maven {
    url 'https://pro.maven.usehover.com/snapshots'
    credentials {
        username "${HoverUsername}"
        password "${HoverPassword}"
    }
    authentication { basic(BasicAuthentication) }
    content { includeGroup "com.hover" }
}
```

Next, remove the following dependencies from the `app/build.gradle` file:

```
debugImplementation project(":hover.sdk")
debugImplementation 'com.android.volley:volley:1.2.1'
debugImplementation 'com.google.android.gms:play-services-analytics:18.0.1'
debugImplementation 'com.squareup.picasso:picasso:2.71828'
debugImplementation 'io.sentry:sentry-android:4.3.0'
```
    
Remove `-pro` from `def sdk_version = "2.0.0-stax-1.16.2-pro"`.
Then just change `releaseImplementation "com.hover:android-sdk:$sdk_version"` to `implementation`.

### Private Setup

If you have access to the pro version of the SDK (contact our team) then add the `HoverUsername` and `HoverPassword` credentials to your gradle.properties file.

If you have access to the debug version of the SDK then create a folder in the root directory of this project called `hover.sdk`. Add a `build.gradle` file with the following content:
```
configurations.maybeCreate("default")
artifacts.add("default", file('app-debug.aar'))
```
Then just copy the SDK aar to the folder and name it `app-debug.aar`
