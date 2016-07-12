Wear DisplayLock
================

This is a utility that allows you to keep the display of your Android 
Wear device permanently enabled. When activated, it grabs a wake lock 
that prevents the display from going to sleep.

The app is made up of both a phone/tablet app, and an embedded wearable 
APK. The wake lock can be controlled from any device, and the state of 
the wake lock is synchronized via a data item between all devices on the 
wearable network.

This is designed as a tool to be used during demonstrations and video 
filming where you want to prevent the display from going to sleep. You 
should disable the wake lock when not required, otherwise the screen 
could burn in.



Building
--------

This sample uses the Gradle build system. To build this project in release
mode with the embedded wearable APK, you will need to use
"gradlew assembleRelease" or use Android Studio and the "Generate Signed APK"
menu option.



Support
-------

- Google+ Community: https://g.co/androidweardev
- StackOverflow: https://stackoverflow.com/questions/tagged/android-wear

If you've found an error in this sample, please file an issue:
https://github.com/google/wear-displaylock

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING for more
details.



License
-------

Copyright 2015 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
