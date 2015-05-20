#!/bin/sh
#
# This script installs the Pelias Android SDK library project.
#
# Usage:
#   install-pelias-sdk.sh

git clone https://github.com/mapzen/pelias-android-sdk.git
cd pelias-android-sdk
./gradlew clean install
cd ..
rm -rf pelias-android-sdk
