#!/usr/bin/env bash
#
# Builds master branch and uploads APK to s3://android.mapzen.com/erasermap-snapshots/.

./gradlew assembleDevDebug -PmintApiKey=$MINT_API_KEY -PapiKey=$API_KEY -PbuildNumber=$CIRCLE_BRANCH-$CIRCLE_BUILD_NUM -PsearchBaseUrl="$SEARCH_BASE_URL" -ProuteBaseUrl="$ROUTE_BASE_URL"
s3cmd put app/build/outputs/apk/app-dev-debug.apk s3://android.mapzen.com/erasermap-latest.apk
s3cmd put app/build/outputs/apk/app-dev-debug.apk s3://android.mapzen.com/erasermap-snapshots/master-$CIRCLE_BUILD_NUM.apk
