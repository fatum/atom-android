#!/usr/bin/env bash -x
./gradlew uploadArchives

TARGET_BRANCH="mvn-repo"
mkdir ${TARGET_BRANCH}
cd ${TARGET_BRANCH}

git clone -b ${TARGET_BRANCH} --single-branch https://github.com/ironSource/atom-android.git

cd atom-android

cp -r ../../mvn-repo-tmp/* .

git add .
git commit -m "Deploy to GitHub Maven Repository"

# Now that we're all set up, we can push.
git push origin ${TARGET_BRANCH}

cd ../../
rm -r -f ${TARGET_BRANCH}
rm -r -f mvn-repo-tmp