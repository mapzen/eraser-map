# Eraser Map
Privacy-focused mapping application for Android

## Install Dependencies
1. Install [Kotlin Plugin for Android Studio](https://plugins.jetbrains.com/plugin/6954?pr=androidstudio)

## API Keys
1. Go to https://mapzen.com/developers/ and auth with Github
2. Create a Mapzen API key
3. Add key to `~/.gradle/gradle.properties` or use as command line argument


**gradle.properties**

```bash
apiKey=mapzen-???
```

**Command-line arguments**

```bash
./gradlew clean installDebug -PapiKey=$API_KEY
```



## Clone and build project
```bash
$ git clone https://github.com/mapzen/eraser-map.git
$ ./gradlew
```

## Beta Builds

Beta builds (which have Splunk MINT Crash Reporting enabled) are available from here: http://android.mapzen.com/erasermap/

## Running Tests
2. Follow instructions to enable [unit testing support](http://tools.android.com/tech-docs/unit-testing-support) in Android Studio
3. Modify unit test run configuration working directory to `/path/to/project/eraser-map/app`
4. Rebuild and run tests `./gradlew test --continue`

