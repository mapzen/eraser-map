#!/usr/bin/env bash
#
# Builds development branches and uploads APK to s3://android.mapzen.com/erasermap-development/.

if [ -z ${PERFORM_RELEASE} ]
  then
    ./gradlew assembleDevDebug -PmintApiKey=$MINT_API_KEY -PvectorTileApiKey=$VECTOR_TILE_API_KEY -PpeliasApiKey=$PELIAS_API_KEY -PvalhallaApiKey=$VALHALLA_API_KEY -PbuildNumber=$CIRCLE_BRANCH-$CIRCLE_BUILD_NUM
    s3cmd put app/build/outputs/apk/app-dev-debug.apk s3://android.mapzen.com/erasermap-development/$CIRCLE_BRANCH-$CIRCLE_BUILD_NUM.apk
  else
    scripts/install-leyndo.sh
    cd app && git clone $CONFIG_REPO
    cd ..
    ./gradlew clean assembleProdRelease --refresh-dependencies -PmintApiKey=$MINT_API_KEY -PvectorTileApiKey=$VECTOR_TILE_API_KEY_PROD -PpeliasApiKey=$PELIAS_API_KEY_PROD -PvalhallaApiKey=$VALHALLA_API_KEY_PROD -PbuildNumber=$RELEASE_TAG -PreleaseStoreFile=$RELEASE_STORE_FILE -PreleaseStorePassword="$RELEASE_STORE_PASSWORD" -PreleaseKeyAlias=$RELEASE_KEY_ALIAS -PreleaseKeyPassword="$RELEASE_KEY_PASSWORD"
    s3cmd put app/build/outputs/apk/app-prod-release.apk s3://android.mapzen.com/erasermap-releases/$RELEASE_TAG.apk
fi
