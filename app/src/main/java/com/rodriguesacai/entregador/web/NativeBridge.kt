package com.rodriguesacai.entregador.web

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.firebase.messaging.FirebaseMessaging
import com.rodriguesacai.entregador.MainActivity
import com.rodriguesacai.entregador.service.DriverSessionStore
import com.rodriguesacai.entregador.service.NotificationHelper
import org.json.JSONObject

class NativeBridge(
    private val activity: MainActivity,
    private val webView: WebView
) {
    @JavascriptInterface
    fun getNativeInfo(): String {
        return JSONObject()
            .put("platform", "android")
            .put("app", "Rodrigues Entregador")
            .put("version", "2.0.0-painelup")
            .put("webBase", "PainelUP")
            .toString()
    }

    @JavascriptInterface
    fun saveDriverSession(sessionJson: String) {
        DriverSessionStore.saveDriverSession(activity, sessionJson)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            DriverSessionStore.saveFcmToken(activity, token)
            DriverSessionStore.syncFcmTokenToFirestore(activity, token)
        }
    }

    @JavascriptInterface
    fun requestNotificationPermission() {
        activity.runOnUiThread {
            // A Activity já solicita a permissão quando necessário no Android 13+.
        }
    }

    @JavascriptInterface
    fun openBatterySettings() {
        activity.runOnUiThread { activity.openBatterySettings() }
    }

    @JavascriptInterface
    fun openNavigation(addressOrUrl: String?) {
        val raw = addressOrUrl?.trim().orEmpty()
        val url = when {
            raw.startsWith("geo:") || raw.startsWith("google.navigation:") || raw.startsWith("waze:") -> raw
            raw.isNotBlank() -> "google.navigation:q=${raw.replace(" ", "+") }"
            else -> "google.navigation:q=Rodrigues+Açaí+e+Cia"
        }
        activity.runOnUiThread { activity.openExternal(url) }
    }

    @JavascriptInterface
    fun showUrgentOffer(json: String?) {
        val data = runCatching { JSONObject(json ?: "{}") }.getOrDefault(JSONObject())
        NotificationHelper.urgentRideNotification(
            context = activity,
            rideId = data.optString("rideId", data.optString("id", "sem-id")),
            value = data.optString("value", data.optString("valor", "R$ --")),
            distance = data.optString("distance", data.optString("distancia", "-- km")),
            pickup = data.optString("pickup", data.optString("coleta", "Rodrigues Açaí e Cia")),
            dropoff = data.optString("dropoff", data.optString("entrega", "Endereço do cliente liberado após aceite"))
        )
    }

    @JavascriptInterface
    fun emitToWeb(event: String, payloadJson: String?) {
        val safeEvent = JSONObject.quote(event)
        val payload = payloadJson?.takeIf { it.trim().startsWith("{") } ?: "{}"
        activity.runOnUiThread {
            webView.evaluateJavascript("window.dispatchEvent(new CustomEvent($safeEvent,{detail:$payload}));", null)
        }
    }
}
