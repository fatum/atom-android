#!/bin/bash "
T1=$(cat logcat.txt | grep "All tasks done")
if [ -z "$T1" ]; then
    echo "Found error in Tracker tests!"
    exit 1
fi