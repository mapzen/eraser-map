# Eraser Map
Privacy-focused mapping application for Android

## Clone and build project
```bash
$ git clone https://github.com/mapzen/eraser-map.git
$ cd eraser-map
$ git submodule init && git submodule update
$ ./gradlew
```

## Importing project into Android Studio
1. Open Android Studio and choose _File > Import project..._ and select project root folder
2. Install [Kotlin Plugin for Android Studio](https://plugins.jetbrains.com/plugin/6954?pr=androidstudio)
3. Follow instructions to enable [unit testing support](http://tools.android.com/tech-docs/unit-testing-support) in Android Studio
4. Modify unit test run configuration working directory to `/path/to/project/eraser-map/app`
5. Rebuild and run tests

## API Keys

API keys for tiles, search, and routing services must be set in a local `gradle.properties` file or via Gradle command line arguments.

**gradle.properties**

```bash
vectorTileApiKey=vector-tiles-???
peliasApiKey=search-???
valhallaApiKey=valhalla-???
```

**Command-line arguments**

```bash
./gradlew clean installDebug -PvectorTileApiKey=$VECTOR_TILE_API_KEY \
    -PpeliasApiKey=$PELIAS_API_KEY \
    -PvalhallaApiKey=$VALHALLA_API_KEY
```
