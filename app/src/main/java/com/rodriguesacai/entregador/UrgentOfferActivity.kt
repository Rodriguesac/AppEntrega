package com.rodriguesacai.entregador
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodriguesacai.entregador.ui.RodriguesDriverApp
class UrgentOfferActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setShowWhenLocked(true); setTurnScreenOn(true); setContent { RodriguesDriverApp() } }
}
