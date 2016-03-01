#!/usr/bin/env bash

export PATH="$PATH:$ANDROID_NDK"
git clone git@github.com:mapzen/leyndo.git
cd leyndo && ./install.sh
