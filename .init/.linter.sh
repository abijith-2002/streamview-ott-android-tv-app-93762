#!/bin/bash
cd /home/kavia/workspace/code-generation/streamview-ott-android-tv-app-93762/streamly_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

