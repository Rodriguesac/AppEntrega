package com.rodriguesacai.entregador

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rodriguesacai.entregador.service.DriverSessionStore

class UrgentRideActivity : Activity() {
    private lateinit var rideId: String
    private lateinit var value: String
    private lateinit var distance: String
    private lateinit var pickup: String
    private lateinit var dropoff: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        window.statusBarColor = Color.parseColor("#062D1B")
        window.navigationBarColor = Color.parseColor("#05080A")

        rideId = intent.getStringExtra("rideId") ?: "sem-id"
        value = intent.getStringExtra("value") ?: "R$ --"
        distance = intent.getStringExtra("distance") ?: "-- km"
        pickup = intent.getStringExtra("pickup") ?: "Rodrigues Açaí e Cia"
        dropoff = intent.getStringExtra("dropoff") ?: "Endereço do cliente liberado após aceite"

        setContentView(buildLayout())
    }

    private fun buildLayout(): ScrollView {
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.parseColor("#07120D"))
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }
        scroll.addView(root)

        root.addView(label("NOVA ROTA", "#82C91E", 13, true))
        root.addView(spacer(18))
        root.addView(text("Aceitar a rota?", "#FFFFFF", 34, true))
        root.addView(text(value, "#FFFFFF", 56, true))
        root.addView(text("$distance • pedido em tempo real", "#D7EFE2", 17, true))
        root.addView(spacer(22))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(Color.WHITE)
        }
        root.addView(card, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        card.addView(textDark("COLETA", 11, true, "#078244"))
        card.addView(textDark(pickup, 18, true, "#171219"))
        card.addView(spacer(12))
        card.addView(textDark("ENTREGA", 11, true, "#E90045"))
        card.addView(textDark(dropoff, 18, true, "#171219"))
        card.addView(spacer(18))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val reject = Button(this).apply {
            text = "Rejeitar"
            textSize = 16f
            setTextColor(Color.parseColor("#E90045"))
            setOnClickListener { answerRide("REJEITADA") }
        }
        val accept = Button(this).apply {
            text = "Aceitar"
            textSize = 18f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#078244"))
            setOnClickListener { answerRide("ACEITA") }
        }
        row.addView(reject, LinearLayout.LayoutParams(0, dp(58), 1f).apply { setMargins(0, 0, dp(10), 0) })
        row.addView(accept, LinearLayout.LayoutParams(0, dp(58), 1.45f))
        card.addView(row)
        card.addView(spacer(14))
        card.addView(textDark("ID: $rideId", 12, false, "#86798E"))

        root.addView(spacer(18))
        val open = Button(this).apply {
            text = "Abrir app"
            setOnClickListener {
                startActivity(Intent(this@UrgentRideActivity, MainActivity::class.java))
                finish()
            }
        }
        root.addView(open, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)))
        return scroll
    }

    private fun answerRide(status: String) {
        val driverId = DriverSessionStore.getDriverId(this)
        val driverName = DriverSessionStore.getDriverName(this)
        val data = mutableMapOf<String, Any>(
            "status" to status,
            "statusEntregador" to status,
            "respondidoEm" to Timestamp.now(),
            "atualizadoEm" to Timestamp.now(),
            "origemResposta" to "android-fullscreen"
        )
        if (!driverId.isNullOrBlank()) {
            data["entregadorId"] = driverId
            data["entregadorNome"] = driverName
        }

        val db = FirebaseFirestore.getInstance()
        listOf("rotas_entrega", "pedidos").forEach { collection ->
            runCatching { db.collection(collection).document(rideId).set(data, SetOptions.merge()) }
        }
        if (!driverId.isNullOrBlank()) {
            db.collection("historicoEntregador").document("${driverId}_${rideId}_${System.currentTimeMillis()}").set(
                data + mapOf(
                    "rotaId" to rideId,
                    "valorTexto" to value,
                    "distanciaTexto" to distance,
                    "coleta" to pickup,
                    "entrega" to dropoff,
                    "criadoEm" to Timestamp.now()
                ),
                SetOptions.merge()
            )
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun text(value: String, color: String, sp: Int, bold: Boolean): TextView = TextView(this).apply {
        text = value
        setTextColor(Color.parseColor(color))
        textSize = sp.toFloat()
        if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        includeFontPadding = true
    }

    private fun label(value: String, color: String, sp: Int, bold: Boolean): TextView = text(value, color, sp, bold).apply {
        setPadding(dp(14), dp(8), dp(14), dp(8))
        setBackgroundColor(Color.parseColor("#083C25"))
    }

    private fun textDark(value: String, sp: Int, bold: Boolean, color: String): TextView = TextView(this).apply {
        text = value
        setTextColor(Color.parseColor(color))
        textSize = sp.toFloat()
        if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun spacer(heightDp: Int): TextView = TextView(this).apply { height = dp(heightDp) }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
