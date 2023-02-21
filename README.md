# android-toolbox

## Implement library via Maven

In project gradle 
```groovy
    allprojects {
    repositories {
        google()
        maven {
            url = uri("https://maven.pkg.github.com/hyperether/android-toolbox")

            credentials {
                username = "hyperether"
                password = "YOUR_PERSONAL_GITHUB_TOKEN"
            }
        }
    }
}
```

In app gradle add dependency

```groovy
    dependencies {
    implementation 'com.hyperether:android-toolbox:1.0.17'
}
```