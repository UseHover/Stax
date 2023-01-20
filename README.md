# Stax

### Public Setup
This project requires the Hover SDK. Because access to the SDK is restricted for security reasons, the simplest way to get this project to build is to change some of the dependencies to use the basic release version of the SDK. 

Swith to project view in your IDE, inside gradle directory, open `libs.versions` file and find the hover android SDK dependency:

```toml
hover = "com.hover:android-sdk:2.0.0-stax-1.18.10-pro"
```

First, replace the this dependency from the `libs.versions` file with:

```toml
hover = "com.hover:android-sdk:2.0.0-stax-1.18.10"
```

Run either `productionDebug` or `productionRelease` variant.
