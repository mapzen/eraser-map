# Building a release APK

1. Clone the production keystore from its private repository and copy it into your home folder.
2. Add the keystore credentials to `~/.gradle/gradle.properties`.
3. Copy the production API key values encoded using `encrypter.sh` into `~/.gradle/gradle.properties`.
4. Install the `leyndo` project into your local Maven repository.
5. Build release APK using `./gradlew clean installProdRelease`.
