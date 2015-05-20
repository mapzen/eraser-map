#!/bin/sh
#
# This script installs all local dependencies required to build Eraser Map.
#
# Usage:
#   install-dependencies.sh

for file in scripts/dependency/*.sh; do bash $file; done
