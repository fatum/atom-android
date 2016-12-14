#!/bin/bash All tasks done"
T1=$(cat logcat.txt | grep "ssss")
if [ -z "$T1" ]; then
    echo "Found error in Tracker tests!"
    exit -1
fi