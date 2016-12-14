#!/bin/bash "
printf '%b\n' "$(cat logcat.txt)"
RESULT=$(cat logcat.txt | grep "All tasks done")
if [ -z "$RESULT" ]; then
    echo "Found error in Tracker tests!"
    exit 1
fi