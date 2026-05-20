package com.rodriguesacai.entregador

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var repo: FirebaseRepo
    private lateinit var prefs: SharedPreferences
    private var profile: DriverProfile? = null
    private var rides: List<RideItem> = emptyList()
    private var banners: List<BannerItem> = emptyList()
    private var notices: List<NoticeItem> = emptyList()
    private var updateInfo: AppUpdateInfo? = null
    private var selectedRide: RideItem? = null
    private var lastError: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = FirebaseRepo(this)
        prefs = getSharedPreferences("up_driver", Context.MODE_PRIVATE)
        NotificationCenter.createChannels(this)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= 23) window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        loadSessionOrLogin()
    }

    override fun onDestroy() {
        super.onDestroy()
        repo.closeListeners()
    }

    private fun loadSessionOrLogin() {
        val id = prefs.getString("driverId", "").orEmpty()
        val col = prefs.getString("driverCollection", "").orEmpty()
        if (id.isBlank() || col.isBlank()) {
            showLogin()
            return
        }
        showLoading("Carregando sua conta...")
        FirebaseFirestore.getInstance().collection(col).document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) startDriver(doc.driverFrom(col)) else showLogin()
            }
            .addOnFailureListener { showLogin("Sessão expirada. Entre novamente.") }
    }

    private fun startDriver(driver: DriverProfile) {
        profile = driver
        prefs.edit().putString("driverId", driver.id).putString("driverCollection", driver.collection).apply()
        repo.closeListeners()
        repo.listenProfile(driver, { updated -> profile = updated; showHome() }, { lastError = it })
        repo.listenRides(driver, { list -> rides = list; refreshCurrent() }, { lastError = it; refreshCurrent() })
        repo.listenBanners({ list -> banners = list; refreshCurrent() }, { lastError = it })
        repo.listenNotices(driver, { list -> notices = list; refreshCurrent() }, { lastError = it })
        repo.listenUpdateInfo { info -> updateInfo = info }
        if (!driver.approved) showAnalysis() else if (driver.needsPasswordSetup) showCreatePassword() else showHome()
    }

    private fun refreshCurrent() {
        val tag = prefs.getString("screen", "home") ?: "home"
        when (tag) {
            "home" -> showHome(false)
            "rides" -> showRides(false)
            "wallet" -> showWallet(false)
            "notices" -> showNotices(false)
            "more" -> showMore(false)
        }
    }

    private fun showLoading(message: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(0xFFF6F8F7.toInt())
        }
        root.addView(label("up", 44f, 0xFF18A85A.toInt(), bold()).apply { gravity = Gravity.CENTER })
        root.addView(label(message, 16f, 0xFF6A7470.toInt()).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, 0) })
        setContentView(root)
    }

    private fun showLogin(message: String = "") {
        prefs.edit().putString("screen", "login").apply()
        val root = ScrollView(this).apply { setBackgroundColor(0xFFF6F8F7.toInt()) }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(34), dp(24), dp(24))
        }
        root.addView(col)
        val logo = label("up\nentregas", 34f, 0xFF18A85A.toInt(), bold()).apply { gravity = Gravity.CENTER; setLineSpacing(0f, 1f); setPadding(0, dp(10), 0, dp(22)) }
        col.addView(logo, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        col.addView(label("Bem-vindo", 28f, 0xFF112018.toInt(), bold()))
        col.addView(label("Acesse sua conta para receber corridas reais da operação.", 15f, 0xFF66736D.toInt()))
        col.addSpace(18)
        val card = card(18, 26)
        val inputId = input("CPF, telefone ou e-mail")
        val inputPass = input("Senha", true)
        card.addView(label("CPF ou telefone", 13f, 0xFF66736D.toInt(), medium()))
        card.addView(inputId)
        card.addSpace(12)
        card.addView(label("Senha", 13f, 0xFF66736D.toInt(), medium()))
        card.addView(inputPass)
        card.addView(TextView(this).apply {
            text = "Esqueci minha senha"
            setTextColor(0xFF18A85A.toInt())
            textSize = 13f
            gravity = Gravity.RIGHT
            setPadding(0, dp(10), 0, dp(6))
            setOnClickListener { toast("Solicite redefinição pelo gestor da loja.") }
        })
        card.addView(actionButton("Entrar", true) {
            showLoading("Validando acesso...")
            repo.login(inputId.text.toString(), inputPass.text.toString()) { driver, error ->
                runOnUiThread {
                    if (driver != null) startDriver(driver) else showLogin(error ?: "Não foi possível entrar.")
                }
            }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)))
        card.addView(actionButton("Solicitar cadastro", false) { showSignup() }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { setMargins(0, dp(10), 0, 0) })
        col.addView(card)
        if (message.isNotBlank()) col.addView(statusBox(message, false))
        col.addSpace(16)
        col.addView(label("Cadastro sujeito à aprovação da operação.", 13f, 0xFF7C8782.toInt()).apply { gravity = Gravity.CENTER })
        setContentView(root)
    }

    private fun showSignup() {
        prefs.edit().putString("screen", "signup").apply()
        val root = baseScroll()
        val col = root.getChildAt(0) as LinearLayout
        col.addView(topTitle("Solicitar cadastro", "Envie seus dados para aprovação do gestor."))
        val c = card(18, 24)
        val name = input("Nome completo")
        val phone = input("Telefone / WhatsApp")
        val cpf = input("CPF")
        c.addView(label("Nome", 13f, 0xFF66736D.toInt(), medium())); c.addView(name); c.addSpace(10)
        c.addView(label("Telefone", 13f, 0xFF66736D.toInt(), medium())); c.addView(phone); c.addSpace(10)
        c.addView(label("CPF", 13f, 0xFF66736D.toInt(), medium())); c.addView(cpf); c.addSpace(16)
        c.addView(actionButton("Enviar para análise", true) {
            repo.createSignup(name.text.toString(), phone.text.toString(), cpf.text.toString()) { ok, msg -> runOnUiThread { if (ok) showAnalysis(msg) else toast(msg) } }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)))
        c.addView(actionButton("Voltar", false) { showLogin() }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { setMargins(0, dp(10), 0, 0) })
        col.addView(c)
        setContentView(root)
    }

    private fun showAnalysis(message: String = "Seu cadastro está em análise.") {
        val root = baseScroll()
        val col = root.getChildAt(0) as LinearLayout
        col.addView(topTitle("Cadastro em análise", "Aguarde a aprovação do gestor para acessar corridas."))
        col.addView(statusBox(message, true))
        col.addView(actionButton("Sair", false) { prefs.edit().clear().apply(); showLogin() }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { setMargins(0, dp(18), 0, 0) })
        setContentView(root)
    }

    private fun showCreatePassword() {
        val p = profile ?: return showLogin()
        val root = baseScroll()
        val col = root.getChildAt(0) as LinearLayout
        col.addView(topTitle("Criar senha", "Primeiro acesso detectado. Crie sua senha para continuar."))
        val c = card(18, 24)
        val pass = input("Nova senha", true)
        c.addView(label("Senha", 13f, 0xFF66736D.toInt(), medium()))
        c.addView(pass)
        c.addSpace(16)
        c.addView(actionButton("Salvar senha", true) {
            repo.savePassword(p, pass.text.toString()) { ok, msg -> runOnUiThread { toast(msg); if (ok) startDriver(p.copy(needsPasswordSetup = false)) } }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)))
        col.addView(c)
        setContentView(root)
    }

    private fun showHome(save: Boolean = true) {
        if (save) prefs.edit().putString("screen", "home").apply()
        val p = profile ?: return showLogin()
        val root = shell("Início")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(homeHeader(p))
        col.addView(statusButton(p))
        col.addView(earningsCard())
        col.addView(bannerCard())
        col.addView(quickGrid())
        val active = rides.firstOrNull { isActiveAssigned(it, p) }
        if (active != null) col.addView(activeRideCard(active))
        if (lastError.isNotBlank()) col.addView(statusBox("Aviso de sincronização: $lastError", false))
        setContentView(root)
    }

    private fun showRides(save: Boolean = true) {
        if (save) prefs.edit().putString("screen", "rides").apply()
        val p = profile ?: return showLogin()
        val root = shell("Corridas")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Corridas", "Ofertas, andamento e histórico real do Firebase."))
        val available = rides.filter { isAvailable(it) }
        val active = rides.filter { isActiveAssigned(it, p) }
        val history = rides.filter { isHistory(it) }
        section(col, "Disponíveis", available, "Nenhuma corrida disponível.") { rideOfferCard(it) }
        section(col, "Em andamento", active, "Nenhuma corrida em andamento.") { activeRideCard(it) }
        section(col, "Histórico", history, "Nenhum histórico encontrado.") { historyRideCard(it) }
        setContentView(root)
    }

    private fun showWallet(save: Boolean = true) {
        if (save) prefs.edit().putString("screen", "wallet").apply()
        val p = profile ?: return showLogin()
        val finished = rides.filter { isFinished(it) && it.assignedDriverId in listOf(p.id, p.uid) }
        val todayTotal = finished.sumOf { it.value }
        val root = shell("Carteira")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Ganhos e repasse", "Valores calculados pelas corridas finalizadas encontradas no Firebase."))
        val c = card(18, 26)
        c.addView(label("Saldo encontrado", 14f, 0xFF66736D.toInt(), medium()))
        c.addView(label(money(todayTotal), 34f, 0xFF112018.toInt(), bold()))
        c.addView(label("${finished.size} corridas finalizadas", 14f, 0xFF66736D.toInt()))
        c.addDivider()
        c.addView(label("Pix", 13f, 0xFF66736D.toInt(), medium()))
        val pix = input("Chave Pix").apply { setText(p.pix) }
        c.addView(pix)
        c.addSpace(10)
        c.addView(label("Banco", 13f, 0xFF66736D.toInt(), medium()))
        val bank = input("Banco").apply { setText(p.bank) }
        c.addView(bank)
        c.addSpace(10)
        c.addView(label("Tipo de repasse", 13f, 0xFF66736D.toInt(), medium()))
        val type = input("Ex: semanal, diário, combinado").apply { setText(p.payoutType) }
        c.addView(type)
        c.addSpace(14)
        c.addView(actionButton("Salvar recebimento", true) {
            repo.savePayout(p, pix.text.toString(), bank.text.toString(), type.text.toString()) { _, msg -> runOnUiThread { toast(msg) } }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)))
        col.addView(c)
        setContentView(root)
    }

    private fun showNotices(save: Boolean = true) {
        if (save) prefs.edit().putString("screen", "notices").apply()
        val root = shell("Notificações")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Notificações", "Avisos reais enviados pela operação."))
        if (notices.isEmpty()) col.addView(emptyCard("Nenhuma notificação encontrada."))
        notices.forEach { n ->
            val c = card(14, 20)
            c.addView(label(n.title, 17f, 0xFF112018.toInt(), bold()))
            c.addView(label(n.body.ifBlank { "Sem descrição." }, 14f, 0xFF66736D.toInt()))
            c.addView(label(shortDate(n.createdAt), 12f, 0xFF9AA5A0.toInt()))
            col.addView(c, margin())
        }
        setContentView(root)
    }

    private fun showMore(save: Boolean = true) {
        if (save) prefs.edit().putString("screen", "more").apply()
        val p = profile ?: return showLogin()
        val root = shell("Mais")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Mais", "Perfil, permissões, navegação e suporte."))
        col.addView(menuItem("Perfil e conta") { showProfile() })
        col.addView(menuItem("Permissões do app") { showPermissions() })
        col.addView(menuItem("Atualização do app") { showUpdateScreen() })
        col.addView(menuItem("Navegação padrão") { chooseNavigation() })
        col.addView(menuItem("Suporte") { toast("Fale com a operação da loja pelo canal interno definido no gestor.") })
        col.addView(menuItem("Sair") { prefs.edit().clear().apply(); repo.closeListeners(); showLogin() })
        setContentView(root)
    }

    private fun showProfile() {
        val p = profile ?: return showLogin()
        val root = shell("Perfil")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Perfil", "Dados reais do cadastro do entregador."))
        val c = card(18, 26)
        c.addView(label(p.name, 22f, 0xFF112018.toInt(), bold()))
        c.addView(label(if (p.approved) "Verificado profissional" else "Cadastro pendente", 14f, 0xFF18A85A.toInt(), medium()))
        c.addDivider()
        c.addView(label("Telefone: ${p.phone.ifBlank { "não cadastrado" }}", 14f, 0xFF66736D.toInt()))
        c.addView(label("E-mail: ${p.email.ifBlank { "não cadastrado" }}", 14f, 0xFF66736D.toInt()))
        c.addSpace(12)
        c.addView(actionButton("Solicitar alteração de telefone/e-mail", false) {
            askText("Solicitar alteração", "Digite o novo telefone ou e-mail") { value ->
                repo.requestProfileChange(p, "contato", value) { _, msg -> runOnUiThread { toast(msg) } }
            }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)))
        col.addView(c)
        setContentView(root)
    }

    private fun showPermissions() {
        val root = shell("Permissões")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Permissões", "Configure uma vez para receber corrida urgente mesmo com o celular bloqueado."))
        col.addView(permissionCard("Notificações", hasNotifications(), "Permitir notificações") { requestNotifications() })
        col.addView(permissionCard("Localização", hasLocation(), "Permitir localização") { requestLocation() })
        col.addView(permissionCard("Bateria sem restrição", !isBatteryRestricted(), "Abrir ajuste de bateria") { openBatterySettings() })
        setContentView(root)
    }

    private fun showUpdateScreen() {
        val info = updateInfo
        val root = shell("Atualização")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Atualização do app", "Controle de versão vindo do Firebase, quando cadastrado."))
        val c = card(18, 26)
        c.addView(label("Versão instalada", 13f, 0xFF66736D.toInt(), medium()))
        c.addView(label("8.0.0", 22f, 0xFF112018.toInt(), bold()))
        c.addDivider()
        if (info == null || info.latestVersion.isBlank()) {
            c.addView(label("Nenhuma configuração de atualização encontrada no Firebase.", 14f, 0xFF66736D.toInt()))
        } else {
            c.addView(label("Última versão: ${info.latestVersion}", 16f, 0xFF112018.toInt(), bold()))
            c.addView(label(info.message.ifBlank { "Sem mensagem do gestor." }, 14f, 0xFF66736D.toInt()))
            if (info.mandatory) c.addView(statusBox("Atualização obrigatória configurada.", false))
            if (info.url.isNotBlank()) c.addView(actionButton("Abrir atualização", true) { openUrl(info.url) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { setMargins(0, dp(14), 0, 0) })
        }
        col.addView(c)
        setContentView(root)
    }

    private fun showRideDetail(ride: RideItem) {
        selectedRide = ride
        val root = shell("Corrida")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Pedido ${ride.orderNumber}", humanStatus(ride.status)))
        val c = card(18, 26)
        c.addView(label("Valor", 13f, 0xFF66736D.toInt(), medium()))
        c.addView(label(money(ride.value), 30f, 0xFF18A85A.toInt(), bold()))
        c.addDivider()
        c.addView(label("Coleta", 13f, 0xFF66736D.toInt(), medium()))
        c.addView(label(ride.pickup.ifBlank { "Endereço de coleta não informado." }, 15f, 0xFF112018.toInt()))
        c.addSpace(10)
        c.addView(label("Entrega", 13f, 0xFF66736D.toInt(), medium()))
        c.addView(label(ride.dropoff.ifBlank { "Endereço de entrega não liberado/informado." }, 15f, 0xFF112018.toInt()))
        c.addSpace(10)
        c.addView(label("Cliente: ${ride.customerName.ifBlank { "não informado" }}", 14f, 0xFF66736D.toInt()))
        c.addView(label("Pagamento: ${ride.payment.ifBlank { "não informado" }}", 14f, 0xFF66736D.toInt()))
        c.addSpace(14)
        c.addView(actionButton("Abrir navegação", true) { openNavigation(ride) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)))
        if (!isFinished(ride)) {
            c.addView(actionButton(nextActionLabel(ride.status), true) { advanceOrFinish(ride) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { setMargins(0, dp(10), 0, 0) })
            c.addView(actionButton("Registrar ocorrência", false) { showOccurrence(ride) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { setMargins(0, dp(10), 0, 0) })
        }
        col.addView(c)
        setContentView(root)
    }

    private fun showOccurrence(ride: RideItem) {
        val p = profile ?: return
        val reasons = listOf("Cliente não atende", "Endereço divergente", "Local inseguro", "Pagamento pendente", "Pedido danificado", "Cliente ausente", "Aguardando cliente", "Outro motivo")
        val root = shell("Ocorrência")
        val col = root.findViewWithTag<LinearLayout>("content")
        col.addView(topTitle("Registrar ocorrência", "A corrida fica em aberto para solução da operação."))
        reasons.forEach { reason ->
            col.addView(menuItem(reason) {
                repo.registerOccurrence(p, ride, reason) { _, msg -> runOnUiThread { toast(msg); showRideDetail(ride.copy(status = "OCORRENCIA")) } }
            })
        }
        setContentView(root)
    }

    private fun advanceOrFinish(ride: RideItem) {
        if (nextStatus(ride.status) == "FINALIZADA") repo.finishRide(ride) { _, msg -> runOnUiThread { toast(msg); showRides() } }
        else repo.advanceRide(ride) { _, msg -> runOnUiThread { toast(msg); showRides() } }
    }

    private fun homeHeader(p: DriverProfile): View {
        val row = horizontal(this, Gravity.CENTER_VERTICAL)
        row.setPadding(0, 0, 0, dp(14))
        val photo = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(0xFFE4F8EC.toInt(), dp(28).toFloat())
            setImageResource(R.drawable.ic_launcher_foreground)
        }
        if (p.photoUrl.isNotBlank()) loadImage(photo, p.photoUrl)
        row.addView(photo, LinearLayout.LayoutParams(dp(58), dp(58)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0) }
        texts.addView(label("Olá, ${p.name.split(" ").firstOrNull().orEmpty().ifBlank { "Entregador" }}", 24f, 0xFF112018.toInt(), bold()))
        texts.addView(label("Pronto para receber corridas", 14f, 0xFF66736D.toInt()))
        row.addView(texts, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(iconCircle("!", 0xFF18A85A.toInt()) { showNotices() })
        row.addView(iconCircle("•", 0xFF18A85A.toInt()) { toast("Chat interno depende do módulo gestor.") })
        return row
    }

    private fun statusButton(p: DriverProfile): View {
        val restricted = isBatteryLow() || !hasLocation() || !isOnlineNetwork()
        val text = when {
            restricted -> "Restrição"
            p.online -> "Disponível"
            else -> "Indisponível"
        }
        val color = when {
            restricted -> 0xFFE34B4B.toInt()
            p.online -> 0xFF18A85A.toInt()
            else -> 0xFF202623.toInt()
        }
        return TextView(this).apply {
            this.text = "  $text  ›"
            textSize = 17f
            setTextColor(Color.WHITE)
            typeface = bold()
            gravity = Gravity.CENTER
            background = rounded(color, dp(22).toFloat())
            setPadding(dp(20), 0, dp(20), 0)
            setOnClickListener {
                if (restricted) showPermissions()
                else repo.setOnline(p, !p.online) { _, msg -> runOnUiThread { toast(msg) } }
            }
        }.also { it.layoutParams = margin(height = 58) }
    }

    private fun earningsCard(): View {
        val p = profile ?: return View(this)
        val finished = rides.filter { isFinished(it) && it.assignedDriverId in listOf(p.id, p.uid) }
        val total = finished.sumOf { it.value }
        val c = card(18, 26)
        val row = horizontal(this)
        val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        left.addView(label("Ganhos encontrados", 14f, 0xFF66736D.toInt(), medium()))
        left.addView(label(money(total), 30f, 0xFF112018.toInt(), bold()))
        row.addView(left, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val right = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.RIGHT }
        right.addView(label("${rides.size} Corridas", 14f, 0xFF112018.toInt(), medium()).apply { gravity = Gravity.RIGHT })
        right.addView(label("${finished.size} Finalizadas", 14f, 0xFF112018.toInt(), medium()).apply { gravity = Gravity.RIGHT })
        row.addView(right)
        c.addView(row)
        return c.also { it.layoutParams = margin() }
    }

    private fun bannerCard(): View {
        val c = card(18, 26)
        if (banners.isEmpty()) {
            c.addView(label("Carrossel do app", 13f, 0xFF18A85A.toInt(), bold()))
            c.addView(label("Nenhum banner cadastrado", 21f, 0xFF112018.toInt(), bold()))
            c.addView(label("Cadastre banners no painel gestor para aparecer aqui.", 14f, 0xFF66736D.toInt()))
        } else {
            val b = banners.first()
            c.background = rounded(0xFF0B7F43.toInt(), dp(26).toFloat())
            c.addView(label("NOVIDADES", 12f, Color.WHITE, bold()))
            c.addView(label(b.title, 24f, Color.WHITE, bold()))
            c.addView(label(b.subtitle.ifBlank { "Atualização da operação." }, 14f, 0xFFE4FFF0.toInt()))
            if (b.action.isNotBlank()) c.addView(actionButton("Saiba mais", false) { openUrl(b.action) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)).apply { setMargins(0, dp(16), 0, 0) })
        }
        return c.also { it.layoutParams = margin() }
    }

    private fun quickGrid(): View {
        val grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val r1 = horizontal(this); val r2 = horizontal(this)
        r1.addView(quick("Histórico", "Ver corridas") { showRides() }, LinearLayout.LayoutParams(0, dp(96), 1f).apply { setMargins(0,0,dp(6),dp(6)) })
        r1.addView(quick("Ganhos", "Resumo financeiro") { showWallet() }, LinearLayout.LayoutParams(0, dp(96), 1f).apply { setMargins(dp(6),0,0,dp(6)) })
        r2.addView(quick("Mapa", "Ver região") { selectedRide?.let { showRideDetail(it) } ?: toast("Abra uma corrida para ver rota.") }, LinearLayout.LayoutParams(0, dp(96), 1f).apply { setMargins(0,dp(6),dp(6),0) })
        r2.addView(quick("Suporte", "Fale conosco") { toast("Suporte interno será aberto pelo gestor.") }, LinearLayout.LayoutParams(0, dp(96), 1f).apply { setMargins(dp(6),dp(6),0,0) })
        grid.addView(r1); grid.addView(r2)
        return grid.also { it.layoutParams = margin() }
    }

    private fun quick(title: String, sub: String, action: () -> Unit): View = card(14, 20).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(label(title, 16f, 0xFF112018.toInt(), bold()))
        addView(label(sub, 12f, 0xFF66736D.toInt()))
        setOnClickListener { action() }
    }

    private fun rideOfferCard(ride: RideItem): View {
        val c = rideBaseCard(ride)
        val p = profile ?: return c
        c.addView(actionButton("Aceitar", true) { repo.acceptRide(p, ride) { _, msg -> runOnUiThread { toast(msg); showRides() } } }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { setMargins(0, dp(12), 0, 0) })
        c.addView(actionButton("Recusar", false) { askText("Recusar corrida", "Motivo opcional") { reason -> repo.rejectRide(p, ride, reason) { _, msg -> runOnUiThread { toast(msg); showRides() } } } }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { setMargins(0, dp(10), 0, 0) })
        return c
    }

    private fun activeRideCard(ride: RideItem): View {
        val c = rideBaseCard(ride)
        c.addView(actionButton("Abrir corrida", true) { showRideDetail(ride) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { setMargins(0, dp(12), 0, 0) })
        return c
    }

    private fun historyRideCard(ride: RideItem): View {
        val c = rideBaseCard(ride)
        c.setOnClickListener { showRideDetail(ride) }
        return c
    }

    private fun rideBaseCard(ride: RideItem): LinearLayout {
        val c = card(16, 22)
        val row = horizontal(this)
        row.addView(label("Pedido ${ride.orderNumber}", 18f, 0xFF112018.toInt(), bold()), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(label(money(ride.value), 18f, 0xFF18A85A.toInt(), bold()))
        c.addView(row)
        c.addView(label(humanStatus(ride.status), 13f, 0xFF18A85A.toInt(), medium()))
        c.addDivider()
        c.addView(label("Coleta: ${ride.pickup.ifBlank { "não informada" }}", 14f, 0xFF45515C.toInt()))
        c.addView(label("Entrega: ${ride.dropoff.ifBlank { ride.neighborhood.ifBlank { "não liberada/informada" } }}", 14f, 0xFF45515C.toInt()))
        val meta = listOf(ride.distance, ride.duration, shortDate(ride.createdAt)).filter { it.isNotBlank() }.joinToString(" • ")
        c.addView(label(meta, 12f, 0xFF8C9792.toInt()))
        return c.also { it.layoutParams = margin() }
    }

    private fun <T> section(col: LinearLayout, title: String, items: List<T>, empty: String, builder: (T) -> View) {
        col.addView(label(title, 20f, 0xFF112018.toInt(), bold()).apply { setPadding(0, dp(18), 0, dp(8)) })
        if (items.isEmpty()) col.addView(emptyCard(empty)) else items.forEach { col.addView(builder(it)) }
    }

    private fun shell(active: String): FrameLayout {
        val root = FrameLayout(this).apply { setBackgroundColor(0xFFF6F8F7.toInt()) }
        val vertical = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { fillViewport = false }
        val content = LinearLayout(this).apply {
            tag = "content"
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(92))
        }
        scroll.addView(content)
        vertical.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        vertical.addView(bottomNav(active), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(72)))
        root.addView(vertical)
        return root
    }

    private fun bottomNav(active: String): View {
        val row = horizontal(this, Gravity.CENTER).apply {
            setPadding(dp(8), dp(6), dp(8), dp(8))
            background = rounded(Color.WHITE, 0f, 0xFFE7ECEA.toInt(), dp(1))
        }
        listOf("Início" to "home", "Corridas" to "rides", "Carteira" to "wallet", "Avisos" to "notices", "Mais" to "more").forEach { (label, key) ->
            val selected = when (active) { "Início" -> key == "home"; "Corridas" -> key == "rides"; "Carteira" -> key == "wallet"; "Notificações" -> key == "notices"; "Mais" -> key == "more"; else -> false }
            row.addView(TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 12f
                typeface = if (selected) bold() else medium()
                setTextColor(if (selected) 0xFF18A85A.toInt() else 0xFF66736D.toInt())
                setOnClickListener { when (key) { "home" -> showHome(); "rides" -> showRides(); "wallet" -> showWallet(); "notices" -> showNotices(); "more" -> showMore() } }
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }
        return row
    }

    private fun baseScroll(): ScrollView {
        val root = ScrollView(this).apply { setBackgroundColor(0xFFF6F8F7.toInt()) }
        root.addView(LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(26), dp(22), dp(26)) })
        return root
    }

    private fun topTitle(title: String, sub: String): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(16))
        addView(label(title, 28f, 0xFF112018.toInt(), bold()))
        addView(label(sub, 14f, 0xFF66736D.toInt()))
    }

    private fun input(hint: String, password: Boolean = false): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 15f
        setSingleLine(true)
        setPadding(dp(14), 0, dp(14), 0)
        background = rounded(0xFFF9FBFA.toInt(), dp(16).toFloat(), 0xFFE3EAE6.toInt(), dp(1))
        if (password) inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { setMargins(0, dp(6), 0, 0) }
    }

    private fun emptyCard(text: String): View = card(16, 22).apply { addView(label(text, 14f, 0xFF66736D.toInt())) }.also { it.layoutParams = margin() }

    private fun statusBox(text: String, ok: Boolean): View = card(14, 18).apply {
        background = rounded(if (ok) 0xFFEAF8EF.toInt() else 0xFFFFF4E4.toInt(), dp(18).toFloat())
        addView(label(text, 14f, if (ok) 0xFF0B7F43.toInt() else 0xFF8A5A00.toInt(), medium()))
    }.also { it.layoutParams = margin() }

    private fun menuItem(text: String, onClick: () -> Unit): View = card(16, 20).apply {
        addView(label(text, 16f, 0xFF112018.toInt(), medium()))
        setOnClickListener { onClick() }
    }.also { it.layoutParams = margin() }

    private fun permissionCard(title: String, ok: Boolean, button: String, action: () -> Unit): View = card(16, 22).apply {
        addView(label(title, 18f, 0xFF112018.toInt(), bold()))
        addView(label(if (ok) "Ativado" else "Pendente", 14f, if (ok) 0xFF18A85A.toInt() else 0xFFE34B4B.toInt(), medium()))
        if (!ok) addView(actionButton(button, true, action), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { setMargins(0, dp(12), 0, 0) })
    }.also { it.layoutParams = margin() }

    private fun iconCircle(text: String, color: Int, action: () -> Unit): TextView = TextView(this).apply {
        this.text = text
        textSize = 18f
        gravity = Gravity.CENTER
        setTextColor(color)
        typeface = bold()
        background = rounded(0xFFEAF8EF.toInt(), dp(22).toFloat())
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply { setMargins(dp(6), 0, 0, 0) }
    }

    private fun margin(height: Int = ViewGroup.LayoutParams.WRAP_CONTENT): LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height).apply { setMargins(0, dp(8), 0, dp(8)) }

    private fun isAvailable(ride: RideItem): Boolean {
        val s = ride.status.uppercase(Locale.ROOT)
        return ride.assignedDriverId.isBlank() && listOf("PENDENTE", "DISPON", "OFERTA", "NOVA", "ABERTA", "AGUARDANDO").any { s.contains(it) }
    }

    private fun isActiveAssigned(ride: RideItem, p: DriverProfile): Boolean = ride.assignedDriverId in listOf(p.id, p.uid) && !isHistory(ride)
    private fun isHistory(ride: RideItem): Boolean = listOf("FINAL", "ENTREGUE", "RECUS", "EXPIR", "OCORR").any { ride.status.uppercase(Locale.ROOT).contains(it) }
    private fun isFinished(ride: RideItem): Boolean = listOf("FINAL", "ENTREGUE").any { ride.status.uppercase(Locale.ROOT).contains(it) }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    private fun askText(title: String, hint: String, callback: (String) -> Unit) {
        val input = EditText(this).apply { this.hint = hint; setSingleLine(false) }
        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Enviar") { _, _ -> callback(input.text.toString()) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun chooseNavigation() {
        val options = arrayOf("Padrão do celular", "Google Maps", "Waze")
        android.app.AlertDialog.Builder(this)
            .setTitle("Navegação padrão")
            .setItems(options) { _, which -> prefs.edit().putString("nav", options[which]).apply(); toast("Navegação: ${options[which]}") }
            .show()
    }

    private fun openNavigation(ride: RideItem) {
        val target = ride.dropoff.ifBlank { ride.pickup }
        if (target.isBlank()) { toast("Endereço não informado."); return }
        val encoded = Uri.encode(target)
        val nav = prefs.getString("nav", "Padrão do celular") ?: "Padrão do celular"
        val uri = when (nav) {
            "Waze" -> Uri.parse("https://waze.com/ul?q=$encoded&navigate=yes")
            "Google Maps" -> Uri.parse("google.navigation:q=$encoded")
            else -> Uri.parse("geo:0,0?q=$encoded")
        }
        try { startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (_: Exception) { openUrl("https://www.google.com/maps/search/?api=1&query=$encoded") }
    }

    private fun openUrl(url: String) {
        val fixed = if (url.startsWith("http")) url else "https://$url"
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fixed))) } catch (_: Exception) { toast("Não foi possível abrir o link.") }
    }

    private fun hasLocation(): Boolean = if (Build.VERSION.SDK_INT < 23) true else checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    private fun hasNotifications(): Boolean = if (Build.VERSION.SDK_INT < 33) true else checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    private fun requestLocation() { if (Build.VERSION.SDK_INT >= 23) requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 20) }
    private fun requestNotifications() { if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 21) }
    private fun openBatterySettings() { try { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) { startActivity(Intent(Settings.ACTION_SETTINGS)) } }

    private fun isBatteryLow(): Boolean {
        val manager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return false
        return manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) in 1..9
    }

    private fun isBatteryRestricted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return !pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun isOnlineNetwork(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun loadImage(view: ImageView, url: String) {
        thread {
            try {
                val stream = URL(url).openStream()
                val bmp = BitmapFactory.decodeStream(stream)
                runOnUiThread { view.setImageBitmap(bmp) }
            } catch (_: Exception) { }
        }
    }
}
