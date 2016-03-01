Eraser Map Release Checklist
============================

1. Update version name and version code number in `app/build.gradle`.
2. Tag release `eraser-mamp-x.y.z` and push tag to GitHub.
3. Add release name and notes to `https://github.com/mapzen/eraser-map/releases`.
4. Trigger release build on Circle CI using `scripts/perform-release.sh`.
5. Update listing in Google Play Store and upload APK.