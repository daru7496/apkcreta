# Keep JavascriptInterface methods intact
-keepclassmembers class com.creta.oemlauncher.CretaAndroidBridge {
    @android.webkit.JavascriptInterface <methods>;
}
