
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

if [ -n "$1" ]; then
    echo $1 > GRADLE_VERSION
fi

GRADLE_VERSION=`cat ./GRADLE_VERSION`

echo "Updating gradle to version $GRADLE_VERSION"

testApp="./testapp/android/testapp"
rm -rf $testApp/gradle
rm -rf $testApp/gradlew
rm -rf $testApp/gradlew.bat
rm -rf $testApp/.gradle

echoPlugin="./plugins/echo/android/EchoPlugin"
rm -rf $echoPlugin/gradle
rm -rf $echoPlugin/gradlew
rm -rf $echoPlugin/gradlew.bat
rm -rf $echoPlugin/.gradle

project="./android"
rm -rf $project/gradle
rm -rf $project/gradlew
rm -rf $project/gradlew.bat
rm -rf $project/.gradle

fuse="./android/fuse"
rm -rf $fuse/gradle
rm -rf $fuse/gradlew
rm -rf $fuse/gradlew.bat
rm -rf $fuse/.gradle

cd android
gradle wrapper
./gradlew wrapper --gradle-version $GRADLE_VERSION
cd ..
