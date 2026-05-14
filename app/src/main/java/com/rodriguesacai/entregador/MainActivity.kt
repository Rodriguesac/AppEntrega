package com.rodriguesacai.entregador

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import com.google.firebase.messaging.FirebaseMessaging
import com.rodriguesacai.entregador.service.DriverSessionStore
import com.rodriguesacai.entregador.web.NativeBridge
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var assetLoader: WebViewAssetLoader
    private var pendingGeoOrigin: String? = null
    private var pendingGeoCallback: GeolocationPermissions.Callback? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#4B0082")
        window.navigationBarColor = android.graphics.Color.parseColor("#08050D")

        assetLoader = WebViewAssetLoader.Builder()
            .setDomain("appassets.androidplatform.net")
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView = WebView(this)
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContentView(webView)

        setupWebView()
        requestNotificationPermissionIfNeeded()
        refreshFcmTokenForWeb()

        webView.loadUrl("https://appassets.androidplatform.net/assets/public/index.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        WebView.setWebContentsDebuggingEnabled(true)
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setGeolocationEnabled(true)
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "$userAgentString RodriguesEntregador/2.0 AndroidWebView"
        }

        webView.addJavascriptInterface(NativeBridge(this, webView), "RodriguesNative")
        webView.addJavascriptInterface(NativeBridge(this, webView), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith("https://appassets.androidplatform.net/")) return false
                if (url.startsWith("geo:") || url.startsWith("google.navigation:") || url.startsWith("waze:") || url.startsWith("tel:") || url.startsWith("mailto:")) {
                    openExternal(url)
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                injectNativeBridgeHelpers()
                refreshFcmTokenForWeb()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                if (hasLocationPermission()) {
                    callback.invoke(origin, true, false)
                    return
                }
                pendingGeoOrigin = origin
                pendingGeoCallback = callback
                if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQ_LOCATION)
                } else {
                    callback.invoke(origin, true, false)
                }
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: WebChromeClient.FileChooserParams
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback
                return runCatching {
                    startActivityForResult(fileChooserParams.createIntent(), REQ_FILE_CHOOSER)
                    true
                }.getOrElse {
                    this@MainActivity.filePathCallback = null
                    false
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < 23) true else {
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ_NOTIFICATIONS)
        }
    }

    private fun refreshFcmTokenForWeb() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            DriverSessionStore.saveFcmToken(this, token)
            DriverSessionStore.syncFcmTokenToFirestore(this, token)
            val payload = JSONObject().put("token", token).put("platform", "android").toString()
            runOnUiThread {
                webView.evaluateJavascript(
                    "window.__RODRIGUES_NATIVE_FCM_TOKEN=${JSONObject.quote(token)};" +
                        "window.dispatchEvent(new CustomEvent('rodNativeFcmToken',{detail:$payload}));",
                    null
                )
            }
        }
    }

    private fun injectNativeBridgeHelpers() {
        val script = """
            (function(){
              if(window.__ROD_NATIVE_BRIDGE_READY) return;
              window.__ROD_NATIVE_BRIDGE_READY = true;
              function sendSession(){
                try {
                  var keys=['@PainelUP:entregador','@up_entregador:sessao','@UP_ENTREGADOR:session','@RodriguesEntregador:sessao'];
                  for(var i=0;i<keys.length;i++){
                    var v=window.localStorage && window.localStorage.getItem(keys[i]);
                    if(v && window.RodriguesNative && window.RodriguesNative.saveDriverSession){
                      window.RodriguesNative.saveDriverSession(v);
                      break;
                    }
                  }
                } catch(e) {}
              }
              sendSession();
              setInterval(sendSession, 4000);
              var oldSet = Storage.prototype.setItem;
              Storage.prototype.setItem = function(k,v){
                oldSet.apply(this, arguments);
                try { if(String(k).toLowerCase().indexOf('entregador')>=0) sendSession(); } catch(e) {}
              };
              window.RodriguesNativeReady = true;
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    fun openExternal(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    fun openBatterySettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION) {
            val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            pendingGeoCallback?.invoke(pendingGeoOrigin ?: "https://appassets.androidplatform.net", granted, false)
            pendingGeoCallback = null
            pendingGeoOrigin = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_FILE_CHOOSER) {
            val result = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            filePathCallback?.onReceiveValue(result)
            filePathCallback = null
        }
    }

    companion object {
        private const val REQ_LOCATION = 991
        private const val REQ_NOTIFICATIONS = 992
        private const val REQ_FILE_CHOOSER = 993
    }
}
