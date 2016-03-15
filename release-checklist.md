Eraser Map Release Checklist
============================

## Building a release APK locally

1. Clone the production keystore from its private repository and copy it into your home folder.
2. Add the keystore credentials to `~/.gradle/gradle.properties`.
3. Copy the production API key values encoded using `encrypter.sh` into `~/.gradle/gradle.properties`.
4. Install the `leyndo` project into your local Maven repository.
5. Build release APK using `./gradlew clean installProdRelease`.

## Building a release APK on Circle CI

1. Remove `-SNAPSHOT` tag from version name in `gradle.properties` and commit changes. Push changes to GitHub (master).
2. Tag release `eraser-map-x.y.z` and push tag to GitHub.
3. Add release name and notes to `https://github.com/mapzen/eraser-map/releases`.
4. Trigger release build on Circle CI using `scripts/perform-release.sh eraser-map-x.y.z`.
5. Update listing (what's new, known issues, screenshots, etc.) in Google Play Store and upload APK.
6. Update version name and add `-SNAPSHOT` tag for next development cycle. Push changes to GitHub (master).
