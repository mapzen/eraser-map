#!/bin/sh
#
# This script installs the Map Burrito library project.
#
# Usage:
#   install-map-burrito.sh

git clone https://github.com/mapzen/map-burrito.git
cd map-burrito/map-burrito
../gradlew clean install
cd ../..
rm -rf map-burrito
