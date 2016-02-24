#!/usr/bin/env bash
#
# This script encrypts the specified raw value using the given salt value and SimpleCrypt.java
#
# Usage:
#   encrypter.sh [salt] [raw_value_to_be_encrypted]
#
# Assumptions:
#   java 7
#   jruby 1.7.12

wget http://central.maven.org/maven2/org/robolectric/android-all/4.4_r1-robolectric-0/android-all-4.4_r1-robolectric-0.jar
javac -cp android-all-4.4_r1-robolectric-0.jar app/src/main/java/com/mapzen/erasermap/SimpleCrypt.java

jruby scripts/encrypter.rb $1 $2

rm android-all-4.4_r1-robolectric-0.jar
rm app/src/main/java/com/mapzen/erasermap/SimpleCrypt.class
