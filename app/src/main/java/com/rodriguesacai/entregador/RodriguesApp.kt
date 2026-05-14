package com.rodriguesacai.entregador

import android.app.Application
import android.webkit.WebView
import com.google.firebase.messaging.FirebaseMessaging
import com.rodriguesacai.entregador.service.DriverSessionStore
import com.rodriguesacai.entregador.service.NotificationHelper

class RodriguesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(true)
        NotificationHelper.createChannels(this)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            DriverSessionStore.saveFcmToken(this, token)
            DriverSessionStore.syncFcmTokenToFirestore(this, token)
        }
    }
}
