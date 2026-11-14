package com.creta.oemlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableImmersiveMode()

        webView = WebView(this).apply {
            setBackgroundColor(Color.parseColor("#0A0D12"))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(CretaAndroidBridge(this@MainActivity), "CretaAndroidBridge")
        }

        setContentView(webView)
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        webView.evaluateJavascript("window.onAndroidBackPressed && window.onAndroidBackPressed();", null)
    }

    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }
}

class CretaAndroidBridge(private val activity: AppCompatActivity) {

    private val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @JavascriptInterface
    fun launchApp(packageName: String): Boolean {
        return try {
            val pm = activity.packageManager
            val intent = if (packageName == "com.android.settings") {
                Intent(Settings.ACTION_SETTINGS)
            } else {
                pm.getLaunchIntentForPackage(packageName)
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                true
            } else {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "App not installed on unit: $packageName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    @JavascriptInterface
    fun getInstalledApps(): String {
        val pm = activity.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val array = JSONArray()
        for (info in apps) {
            val pkg = info.activityInfo.packageName
            if (pkg == activity.packageName) continue
            val obj = JSONObject()
            obj.put("label", info.loadLabel(pm).toString())
            obj.put("packageName", pkg)
            array.put(obj)
        }
        return array.toString()
    }

    @JavascriptInterface
    fun getVolume(): Int {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return ((current.toFloat() / max.toFloat()) * 30f).toInt()
    }

    @JavascriptInterface
    fun setVolume(level0to30: Int) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val scaled = ((level0to30.coerceIn(0, 30) / 30f) * max).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, scaled, 0)
    }

    @JavascriptInterface
    fun toggleMute(): Boolean {
        val muted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (muted) AudioManager.ADJUST_UNMUTE else AudioManager.ADJUST_MUTE,
            0
        )
        return !muted
    }

    @JavascriptInterface
    fun setBrightness(percent: Int) {
        activity.runOnUiThread {
            val lp = activity.window.attributes
            lp.screenBrightness = (percent.coerceIn(10, 100) / 100f)
            activity.window.attributes = lp
        }
    }

    @JavascriptInterface
    fun openDefaultHomePicker() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }

    @JavascriptInterface
    fun isMusicActive(): Boolean {
        return audioManager.isMusicActive
    }
}
