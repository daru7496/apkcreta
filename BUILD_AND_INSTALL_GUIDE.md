# Creta OEM Car Launcher — Key Largo 980 APK Build & Install Guide

This project compiles into a real Android `HOME` launcher APK (`com.creta.oemlauncher`) for 9-inch landscape head units like the **Key Largo 980** (1024×600 / 1280×720).

## Option A: Build APK Automatically in the Cloud (No Android Studio Needed)
1. Extract `CretaOEMLauncher-KeyLargo980.zip`.
2. Create a free GitHub repository and upload the extracted files.
3. Open the **Actions** tab -> click **Build Creta OEM Launcher APK** -> **Run workflow**.
4. In ~90 seconds, download the **CretaOEMLauncher-KeyLargo980-apk** artifact containing `app-debug.apk`.

## Option B: Compile Locally in Android Studio
1. Extract `CretaOEMLauncher-KeyLargo980.zip`.
2. Open **Android Studio** -> **Open** -> select `CretaOEMLauncher-KeyLargo980`.
3. Click **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
4. Grab `app/build/outputs/apk/debug/app-debug.apk`.

## Install on Your Key Largo 980 (Creta E)
1. Copy `app-debug.apk` onto a FAT32 USB pen drive.
2. Plug the USB drive into your Key Largo 980 USB cable.
3. Open **File Manager** on the head unit -> tap `app-debug.apk` -> **Install**.
4. Press the bezel **HOME** button -> choose **Creta OEM Launcher** -> tap **Always**.
