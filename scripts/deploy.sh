#!/usr/bin/env bash
#
# Builds development branches and uploads APK to s3://android.mapzen.com/erasermap-development/.

./gradlew assembleDevDebug -PmintApiKey=$MINT_API_KEY -PvectorTileApiKey=$VECTOR_TILE_API_KEY -PpeliasApiKey=$PELIAS_API_KEY -PvalhallaApiKey=$VALHALLA_API_KEY -PbuildNumber=$CIRCLE_BRANCH-$CIRCLE_BUILD_NUM
s3cmd put app/build/outputs/apk/app-dev-debug.apk s3://android.mapzen.com/erasermap-development/$CIRCLE_BRANCH-$CIRCLE_BUILD_NUM.apk
