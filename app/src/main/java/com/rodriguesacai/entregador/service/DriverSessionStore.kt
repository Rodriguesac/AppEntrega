package com.rodriguesacai.entregador.service

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

object DriverSessionStore {
    private const val PREF = "rod_driver_session"
    private const val KEY_DRIVER_ID = "driverId"
    private const val KEY_DRIVER_CPF = "driverCpf"
    private const val KEY_DRIVER_NAME = "driverName"
    private const val KEY_FCM_TOKEN = "fcmToken"

    fun saveDriverSession(context: Context, sessionJson: String) {
        val obj = runCatching { JSONObject(sessionJson) }.getOrNull() ?: return
        val cpf = onlyDigits(
            obj.optString("cpfLimpo", obj.optString("cpf", obj.optString("documento", "")))
        )
        val driverId = listOf(
            obj.optString("id"), obj.optString("uid"), obj.optString("cpfLimpo"), obj.optString("cpf"), obj.optString("documento")
        ).firstOrNull { it.isNotBlank() }?.let { onlyDigits(it).ifBlank { it } } ?: cpf
        val name = obj.optString("nomeCompleto", obj.optString("nome", obj.optString("apelido", "Entregador")))

        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(KEY_DRIVER_ID, driverId)
            .putString(KEY_DRIVER_CPF, cpf)
            .putString(KEY_DRIVER_NAME, name)
            .apply()
    }

    fun saveFcmToken(context: Context, token: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }

    fun syncFcmTokenToFirestore(context: Context, token: String? = null) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val driverId = prefs.getString(KEY_DRIVER_ID, null)?.takeIf { it.isNotBlank() } ?: return
        val currentToken = token ?: prefs.getString(KEY_FCM_TOKEN, null)?.takeIf { it.isNotBlank() } ?: return
        val cpf = prefs.getString(KEY_DRIVER_CPF, "").orEmpty()
        val name = prefs.getString(KEY_DRIVER_NAME, "Entregador").orEmpty()

        val data = mapOf(
            "fcmToken" to currentToken,
            "tokenFCM" to currentToken,
            "appPlatform" to "android",
            "appOrigem" to "painelup-hibrido",
            "appVersion" to "2.0.0-painelup",
            "ultimoDispositivoEm" to Timestamp.now(),
            "cpfLimpo" to cpf,
            "nome" to name
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("entregadores").document(driverId).set(data, com.google.firebase.firestore.SetOptions.merge())
        db.collection("rastreioEntregador").document(driverId).set(
            mapOf(
                "entregadorId" to driverId,
                "cpfLimpo" to cpf,
                "nome" to name,
                "fcmToken" to currentToken,
                "appVersion" to "2.0.0-painelup",
                "atualizadoEm" to Timestamp.now()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun getDriverId(context: Context): String? {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_DRIVER_ID, null)
    }

    fun getDriverName(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_DRIVER_NAME, "Entregador") ?: "Entregador"
    }

    private fun onlyDigits(value: String): String = value.filter { it.isDigit() }
}
