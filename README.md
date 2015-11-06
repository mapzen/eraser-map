# Eraser Map
Privacy-focused mapping application for Android

## Importing project into Android Studio
1. Clone https://github.com/mapzen/eraser-map.git
2. Open Android Studio and choose _File > Import project..._ and select project root folder
3. Install [Kotlin Plugin for Android Studio](https://plugins.jetbrains.com/plugin/6954?pr=androidstudio)
4. Follow instructions to enable [unit testing support](http://tools.android.com/tech-docs/unit-testing-support) in Android Studio
5. Modify unit test run configuration working directory to `/path/to/project/eraser-map/app`
6. Rebuild and run tests

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
