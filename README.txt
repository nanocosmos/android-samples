nanoStream Live Video Encoder Software
(c) 2010-2016, nanocosmos gmbh
http://www.nanocosmos.de
sales@nanocosmos.de

The following samples can be found in the separate folders.
- BintuEncoder - This sample shows the basic usage of the nanoStream Android SDK with bintu.live connection for encoder apps.
- BintuPlayer - This sample shows the basic usage of the nanoStream Android SDK with bintu.live connection for player apps.
- SimpleEncoder - This sample shows the basic usage of the nanoStream Android SDK with bintu.live connection for streaming apps.

There is a separate README.md file for each sample project inside the project folder.

To integrate nanoStream and bintu.live in your own project follow these steps:
Step 1: Copy the SDK libraries into your Android Studio project:

Add the net.nanocosmos.nanoStream.jar java component to your Android Studio project by copying [SDK]/libs/net.nanocosmos.nanoStream.jar to the folder [projectpath]/app/libs/net.nanocosmos.nanoStream.jar
Add the nanoStream.so native components to the Android Studio project by copying the 5 folders  [SDK]/libs/[platform]/libRTMPStream.so to [projectpath]/app/src/main/jniLibs/[platform]/libRTMPStream.so

Platforms are armeabi, armeabi-v7a, arm64-v8a, x86, mips

Step 2: Add bintu aar to the Android Studio Project:
Open the Project in Android Studio. Go to File -> New -> New Module.
In the opening Window select Import .JAR/.AAR Package and click Next.
Click on the ... button and browse to the bintuSDK-$VERSION.aar file or enter the [SDK]/libs/bintu-$VERSION.aar path in the File name filed and click Finish.

Step 3: Add the nanoStream and/or bintu SDK to the gradle file:
Open the build.gradle file (Module:app) and add

compile files('libs/net.nanocosmos.nanoStream.jar')
compile project(':bintuSDK-$VERSION')

to the dependencies section.


Contact:

sales@nanocosmos.de
http://www.nanocosmos.de/contact
