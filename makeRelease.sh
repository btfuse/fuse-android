#!/bin/sh

# Copyright 2023 Norman Breau 

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

REPO="fuse-android"

cd ..
source compiler/_assertCleanWorkspace.sh
cd fuse-android

VERSION="$1"

if [ -z "$VERSION" ]; then
    echo "Version is required."
    exit 2
fi

cd ..
source compiler/_assertGitTag.sh
cd fuse-android

echo $VERSION > VERSION

cd ..
./build.sh android
cd fuse-android

git add VERSION
git commit -m "Android Release: $VERSION"
git push
git tag -a $VERSION -m "Android Release: $VERSION"
git push --tags

cd android
./gradlew publishReleasePublicationToMavenRepository
cd ..

gh release create $VERSION \
    ./fuse/build/outputs/aar/fuse-debug.aar \
    ./fuse/build/outputs/aar/fuse-release.aar \
    --verify-tag --generate-notes
