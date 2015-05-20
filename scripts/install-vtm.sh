#!/bin/sh
#
# This script installs the VTM project and native dependencies.
#
# Usage:
#   install-vtm.sh

git clone --recursive https://github.com/mapzen/vtm.git

echo "Installing vtm"
cd vtm/vtm && ../gradlew clean install

echo "Installing vtm-android"
cd ../vtm-android && ../gradlew clean install

echo "Cleaning up vtm project"
cd ../.. && rm -rf vtm
