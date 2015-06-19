#!/bin/bash

if [ -n "${PERFORM_RELEASE}" ]; then
  s3cmd put target/*.apk s3://android.mapzen.com/releases/
fi
