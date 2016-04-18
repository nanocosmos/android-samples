# Bintu SDK: Bintu Player Sample

nanoStream Live Video Encoder and Player for Android
(c) 2016 nanocosmos gmbh, http://www.nanocosmos.de

## Description

This sample shows the basic usage of the nanoStream Android SDK with bintu.live connection for player apps.

## Usage

**Step 1: Unzip the sample project to the desired project path**

**Step 2: Copy the SDK libraries into the Android Studio project**

Before runnig the sample, six libraries have to copied from the SDK library folder:
Add the ``net.nanocosmos.nanoStream.jar`` java component to the Android Studio project by copying ``[SDK]/libs/net.nanocosmos.nanoStream.jar`` to the folder``[projectpath]/app/libs/net.nanocosmos.nanoStream.jar``

Add the nanoStream.so native components to the Android Studio project by copying the 5 folders  ``[SDK]/libs/[platform]/libRTMPStream.so`` to  
``[projectpath]/app/src/main/jniLibs/[platform]/libRTMPStream.so``

Platforms are armeabi, armeabi-v7a, arm64-v8a, x86, mips

**Step 3: Add bintu aar to the Android Studio Project**

Open the Project in Android Studio. Go to File -> New -> New Module.
In the opening Window select Import .JAR/.AAR Package and click Next.
Click on the ... button and browse to the bintuSDK-$VERSION.aar file or enter the ``[SDK]/libs/bintu-$VERSION.aar`` path in the File name filed and click Finish.

**Step 4: Add the bintu SDK to the gradel file**

Open the build.gradle file (Module:app) and add
```
compile project(':bintuSDK-$VERSION')
```
to the `dependencies` section.

**Step 5: Open the Configuration.java in the Android Studio Code Editor**
The PlayerActivity contains the main application code. The location is:  
[projectpath]/app/src/main/java/net/nanocosmos/nanoStream/demo/player/util

**Step 6: Enter your license in the PlayerActivity**

Just replace the placeholder with your license key.
```java

    // TODO: REPLACE WITH YOUR LICENSE
    public static final String license = "--- ADD YOUR LICENSE HERE ---";
```

**Step 7: Enter your bintu api key**

Just replace the placeholder with your bintu api key.
```java

    // TODO: REPLACE WITH YOUR LICENSE
    public static final String BINTU_API_KEY = "--- ADD YOUR BINTU API KEY HERE ---";
```

**Step 8: Connect an Android device, build and run the application**

## About

**Version** : nanoStream SDK 4.3.0 / bintu.live SDK 0.1.0

**Compatible with** : Android API-Level from 16 up to 23
