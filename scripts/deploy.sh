#!/usr/bin/env bash
#
# Builds development branches and uploads APK to s3://android.mapzen.com/erasermap-development/.

if [ -z ${PERFORM_RELEASE} ]
  then
    if [ -z ${CIRCLE_PR_USERNAME} ]
      then
        # Build debug APK and upload to s3 (do not run for fork pull requests)
        ./gradlew assembleDevDebug -PmintApiKey=$MINT_API_KEY -PapiKey=$API_KEY -PbuildNumber=$CIRCLE_BRANCH-$CIRCLE_BUILD_NUM -PsearchBaseUrl="$SEARCH_BASE_URL" -ProuteBaseUrl="$ROUTE_BASE_URL"
        s3cmd put app/build/outputs/apk/app-dev-debug.apk s3://android.mapzen.com/erasermap-development/$CIRCLE_BRANCH-$CIRCLE_BUILD_NUM.apk
    fi
  else
    # Build release APK and upload to s3
    cd app && git clone $CONFIG_REPO
    cd ..
    ./gradlew clean assembleProdRelease --refresh-dependencies -PmintApiKey=$MINT_API_KEY -PapiKey=$API_KEY_PROD -PbuildNumber=$RELEASE_TAG -PreleaseStoreFile=$RELEASE_STORE_FILE -PreleaseStorePassword="$RELEASE_STORE_PASSWORD" -PreleaseKeyAlias=$RELEASE_KEY_ALIAS -PreleaseKeyPassword="$RELEASE_KEY_PASSWORD" -PsearchBaseUrl="$SEARCH_BASE_URL" -ProuteBaseUrl="$ROUTE_BASE_URL"
    s3cmd put app/build/outputs/apk/app-prod-release.apk s3://android.mapzen.com/erasermap-releases/$RELEASE_TAG.apk
fi
