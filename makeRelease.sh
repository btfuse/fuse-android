#!/bin/bash

# Copyright 2023 Breautek 

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

source ../build-tools/assertions.sh

assertMac "Mac is required for publishing"
assertGitRepo
assertCleanRepo

VERSION="$1"

assertVersion $VERSION

assetGitTagAvailable $VERSION

./gradlew :fuse:test
assertLastCall

echo $VERSION > VERSION

./gradlew :fuse:build
assertLastCall

git add VERSION
git commit -m "Android Release: $VERSION"
git push
git tag -a $VERSION -m "Android Release: $VERSION"
git push --tags

./gradlew publishReleasePublicationToMavenRepository

gh release create $VERSION \
    ./fuse/build/outputs/aar/fuse-debug.aar \
    ./fuse/build/outputs/aar/fuse-release.aar \
    --verify-tag --generate-notes
