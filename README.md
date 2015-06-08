# Eraser Map
Privacy-focused mapping application for Android

## Install VTM dependency
Run the following script to deploy VTM dependencies to your local Maven repository.
```bash
$ scripts/install-vtm.sh
```

## Importing project into Android Studio
1. Clone https://github.com/mapzen/eraser-map.git
2. Open Android Studio and choose _File > Import project..._ and select project root folder
3. Install [Kotlin Plugin for Android Studio](https://plugins.jetbrains.com/plugin/6954?pr=androidstudio)
4. Follow instructions to enable [unit testing support](http://tools.android.com/tech-docs/unit-testing-support) in Android Studio
5. Modify unit test run configuration working directory to `/path/to/project/eraser-map/app`
6. Rebuild and run tests

## Setting vector tile API key
To set API key(s) to be appended to vector tile requests add the following lines to your `$HOME/.gradle/gradle.properties` file.
```bash
vectorTileApiKeyDebugProp=[your-debug-key]
vectorTileApiKeyReleaseProp=[your-release-key]
```
