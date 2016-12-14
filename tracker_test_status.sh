#!/bin/bash "
LOG_DATA=$(cat logcat.txt)
echo $LOG_DATA
RESULT=$(cat logcat.txt | grep "All tasks done")
if [ -z "$RESULT" ]; then
    echo "Found error in Tracker tests!"
    exit 1
fi