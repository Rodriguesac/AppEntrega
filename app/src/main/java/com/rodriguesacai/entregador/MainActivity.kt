package com.rodriguesacai.entregador

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private val UpGreen = Color(0xFF087D2B)
private val UpGreenDark = Color(0xFF006B28)
private val UpGreenSoft = Color(0xFFEAF6EA)
private val AppBg = Color(0xFFFAFBF7)
private val CardStroke = Color(0xFFE4E8DE)
private val TextStrong = Color(0xFF171A23)
private val TextMuted = Color(0xFF737985)
private val Warning = Color(0xFFFF6B3D)
private val AppFont = FontFamily.SansSerif

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.White.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = true
        setContent { UpEntregasApp() }
    }
}

@Composable
private fun UpEntregasApp(vm: DriverViewModel = viewModel()) {
    val colors = lightColorScheme(
        primary = UpGreen,
        onPrimary = Color.White,
        secondary = UpGreenDark,
        background = AppBg,
        surface = Color.White,
        onSurface = TextStrong,
        outline = CardStroke
    )
    MaterialTheme(colorScheme = colors, typography = MaterialTheme.typography) {
        val state = vm.state.value
        Surface(modifier = Modifier.fillMaxSize(), color = AppBg) {
            if (state.session != null) {
                DriverMainScreen(state = state, vm = vm)
            } else {
                LoginScreen(state = state, onLogin = vm::login)
            }
        }
    }
}

data class BackendConfig(val projectId: String = "", val apiKey: String = "") {
    val configured: Boolean get() = projectId.isNotBlank() && apiKey.isNotBlank()
}

data class DriverSession(
    val id: String,
    val docName: String?,
    val name: String,
    val email: String?,
    val authToken: String?,
    val status: String = "INDISPONIVEL"
)

data class HomeStats(
    val earningsToday: Double = 0.0,
    val ridesToday: Int = 0,
    val finishedToday: Int = 0,
    val earningsWeek: Double = 0.0,
    val earningsMonth: Double = 0.0,
    val totalRidesMonth: Int = 0,
    val deliveryFees: Double = 0.0,
    val tips: Double = 0.0,
    val discounts: Double = 0.0
)

data class RideItem(
    val id: String,
    val documentName: String,
    val collection: String,
    val status: String,
    val pickup: String,
    val delivery: String,
    val distance: String,
    val price: Double,
    val eta: String,
    val createdLabel: String,
    val assignedToCurrent: Boolean
)

data class NoticeItem(val title: String, val body: String, val time: String)

data class BannerItem(val title: String, val subtitle: String, val button: String = "Ver detalhes")

data class AppState(
    val loading: Boolean = false,
    val loginMessage: String? = null,
    val message: String? = null,
    val config: BackendConfig = BackendConfig(),
    val session: DriverSession? = null,
    val stats: HomeStats = HomeStats(),
    val availableRides: List<RideItem> = emptyList(),
    val activeRides: List<RideItem> = emptyList(),
    val historyRides: List<RideItem> = emptyList(),
    val notices: List<NoticeItem> = emptyList(),
    val banners: List<BannerItem> = emptyList(),
    val lastSyncLabel: String = "Ainda não sincronizado"
)

data class FsDoc(val name: String, val id: String, val collection: String, val fields: Map<String, Any?>)

class DriverViewModel(app: Application) : AndroidViewModel(app) {
    var state = mutableStateOf(AppState(loading = true))
        private set

    private val repo = FirestoreRestRepository(app.applicationContext)

    init {
        viewModelScope.launch {
            val cfg = repo.loadConfig()
            state.value = state.value.copy(
                loading = false,
                config = cfg,
                loginMessage = if (cfg.configured) null else "Firebase não configurado. O app abre, mas só mostra dados reais quando encontrar google-services.json ou up_backend.json."
            )
        }
    }

    fun login(user: String, password: String) {
        viewModelScope.launch {
            val cleanUser = user.trim()
            val cleanPass = password.trim()
            if (cleanUser.isBlank() || cleanPass.isBlank()) {
                state.value = state.value.copy(loginMessage = "Digite CPF/telefone/e-mail e senha.")
                return@launch
            }
            state.value = state.value.copy(loading = true, loginMessage = null)
            val result = repo.login(cleanUser, cleanPass)
            if (result.isSuccess) {
                val session = result.getOrThrow()
                state.value = state.value.copy(loading = false, session = session, message = "Conectado. Buscando dados reais...")
                refreshAll()
            } else {
                state.value = state.value.copy(loading = false, loginMessage = result.exceptionOrNull()?.message ?: "Não foi possível entrar.")
            }
        }
    }

    fun logout() {
        state.value = AppState(config = state.value.config)
    }

    fun refreshAll() {
        val session = state.value.session ?: return
        viewModelScope.launch {
            state.value = state.value.copy(loading = true, message = "Sincronizando dados reais...")
            val snapshot = repo.loadOperationalData(session)
            if (snapshot.isSuccess) {
                val data = snapshot.getOrThrow()
                state.value = state.value.copy(
                    loading = false,
                    stats = data.stats,
                    availableRides = data.available,
                    activeRides = data.active,
                    historyRides = data.history,
                    notices = data.notices,
                    banners = data.banners,
                    lastSyncLabel = data.syncLabel,
                    message = data.message
                )
            } else {
                state.value = state.value.copy(
                    loading = false,
                    message = snapshot.exceptionOrNull()?.message ?: "Não foi possível sincronizar."
                )
            }
        }
    }

    fun toggleAvailability() {
        val session = state.value.session ?: return
        val current = session.status.uppercase(Locale.ROOT)
        val next = if (current.contains("DISP") || current.contains("ONLINE") || current == "DISPONIVEL") "INDISPONIVEL" else "DISPONIVEL"
        viewModelScope.launch {
            state.value = state.value.copy(message = "Atualizando status...")
            val result = repo.updateDriverStatus(session, next)
            state.value = if (result.isSuccess) {
                state.value.copy(session = session.copy(status = next), message = "Status atualizado para ${statusHuman(next)}.")
            } else {
                state.value.copy(message = result.exceptionOrNull()?.message ?: "Não foi possível atualizar status.")
            }
        }
    }

    fun acceptRide(ride: RideItem) {
        val session = state.value.session ?: return
        viewModelScope.launch {
            state.value = state.value.copy(message = "Aceitando corrida...")
            val result = repo.patchRide(ride, session, "ACEITA")
            state.value = state.value.copy(message = result.exceptionOrNull()?.message ?: "Corrida aceita.")
            refreshAll()
        }
    }

    fun advanceRide(ride: RideItem) {
        val session = state.value.session ?: return
        val next = nextStatus(ride.status)
        viewModelScope.launch {
            state.value = state.value.copy(message = "Atualizando corrida...")
            val result = repo.patchRide(ride, session, next)
            state.value = state.value.copy(message = result.exceptionOrNull()?.message ?: "Status atualizado: ${statusHuman(next)}.")
            refreshAll()
        }
    }
}

data class OperationalData(
    val stats: HomeStats,
    val available: List<RideItem>,
    val active: List<RideItem>,
    val history: List<RideItem>,
    val notices: List<NoticeItem>,
    val banners: List<BannerItem>,
    val syncLabel: String,
    val message: String
)

class FirestoreRestRepository(private val context: Context) {
    private val driverCollections = listOf("entregadores", "motoboys", "drivers")
    private val rideCollections = listOf("corridas", "pedidos", "rotas", "orders")
    private val noticeCollections = listOf("notificacoes", "avisos")
    private val bannerCollections = listOf("carrosselApp", "banners", "appBanners")

    suspend fun loadConfig(): BackendConfig = withContext(Dispatchers.IO) {
        readGoogleServicesConfig() ?: readBackendConfig() ?: BackendConfig()
    }

    suspend fun login(user: String, password: String): Result<DriverSession> = withContext(Dispatchers.IO) {
        val cfg = loadConfig()
        if (!cfg.configured) return@withContext Result.failure(Exception("Firebase não configurado no app."))

        if (user.contains("@")) {
            val auth = signInEmail(cfg, user, password)
            if (auth != null) {
                val drivers = getDocsFromAny(cfg, driverCollections, auth.idToken)
                val doc = drivers.firstOrNull { doc ->
                    same(doc.valueAny("uid"), auth.localId) || same(doc.id, auth.localId) || same(doc.valueAny("email"), user)
                }
                val name = doc?.bestString("nome", "name", "displayName") ?: auth.email.substringBefore("@").replaceFirstChar { it.uppercase() }
                return@withContext Result.success(
                    DriverSession(id = doc?.id ?: auth.localId, docName = doc?.name, name = name, email = auth.email, authToken = auth.idToken, status = doc?.bestString("status", "situacao") ?: "INDISPONIVEL")
                )
            }
        }

        val docs = getDocsFromAny(cfg, driverCollections, null)
        val clean = digits(user)
        val matched = docs.firstOrNull { doc ->
            val cpf = digits(doc.bestString("cpf", "documento", "cpfEntregador") ?: "")
            val phone = digits(doc.bestString("telefone", "phone", "celular", "whatsapp") ?: "")
            val email = doc.bestString("email") ?: ""
            cpf == clean || phone == clean || email.equals(user, ignoreCase = true)
        }
        if (matched != null) {
            val storedPassword = matched.bestString("senha", "password", "senhaApp", "pin")
            if (storedPassword != null && storedPassword != password) {
                return@withContext Result.failure(Exception("Senha não confere com o cadastro do entregador."))
            }
            val name = matched.bestString("nome", "name", "displayName", "apelido") ?: "Entregador"
            return@withContext Result.success(
                DriverSession(id = matched.id, docName = matched.name, name = name, email = matched.bestString("email"), authToken = null, status = matched.bestString("status", "situacao") ?: "INDISPONIVEL")
            )
        }
        Result.failure(Exception("Não encontrei entregador real com esse CPF/telefone/e-mail nas coleções entregadores/motoboys/drivers."))
    }

    suspend fun loadOperationalData(session: DriverSession): Result<OperationalData> = withContext(Dispatchers.IO) {
        try {
            val cfg = loadConfig()
            if (!cfg.configured) return@withContext Result.failure(Exception("Sem Firebase configurado. Nenhum dado falso será exibido."))
            val rawRides = getDocsFromAny(cfg, rideCollections, session.authToken)
            val rideItems = rawRides.mapNotNull { it.toRideItem(session) }
            val available = rideItems.filter { it.isAvailable() }.sortedByDescending { it.createdLabel }.take(30)
            val active = rideItems.filter { it.assignedToCurrent && it.isActive() }.take(30)
            val history = rideItems.filter { it.assignedToCurrent && it.isHistory() }.take(50)
            val stats = buildStats(rawRides, session)
            val notices = getDocsFromAny(cfg, noticeCollections, session.authToken).mapNotNull { it.toNotice() }.take(30)
            val banners = getDocsFromAny(cfg, bannerCollections, session.authToken).mapNotNull { it.toBanner() }.take(8)
            val msg = if (rawRides.isEmpty()) "Conectado, mas nenhuma corrida/pedido real foi encontrado nas coleções corridas, pedidos, rotas ou orders." else "Dados reais sincronizados."
            Result.success(
                OperationalData(
                    stats = stats,
                    available = available,
                    active = active,
                    history = history,
                    notices = notices,
                    banners = banners,
                    syncLabel = "Sincronizado agora",
                    message = msg
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Falha ao ler dados reais: ${e.message}"))
        }
    }

    suspend fun updateDriverStatus(session: DriverSession, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cfg = loadConfig()
        val docName = session.docName ?: return@withContext Result.failure(Exception("Entregador sem documento real para atualizar."))
        patchDocument(cfg, docName, mapOf("status" to status, "disponivel" to (status == "DISPONIVEL"), "online" to (status == "DISPONIVEL"), "atualizadoEm" to Instant.now().toString()), session.authToken)
    }

    suspend fun patchRide(ride: RideItem, session: DriverSession, nextStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cfg = loadConfig()
        patchDocument(
            cfg,
            ride.documentName,
            mapOf(
                "status" to nextStatus,
                "statusEntrega" to nextStatus,
                "statusPedidoCore" to nextStatus,
                "entregadorId" to session.id,
                "entregadorUid" to session.id,
                "motoboyId" to session.id,
                "entregadorNome" to session.name,
                "atualizadoEm" to Instant.now().toString()
            ),
            session.authToken
        )
    }

    private fun readGoogleServicesConfig(): BackendConfig? {
        val text = readAssetOrNull("google-services.json") ?: return null
        val json = JSONObject(text)
        val projectInfo = json.optJSONObject("project_info") ?: return null
        val projectId = projectInfo.optString("project_id", "")
        val clients = json.optJSONArray("client")
        var apiKey = ""
        if (clients != null && clients.length() > 0) {
            val apiKeys = clients.optJSONObject(0)?.optJSONArray("api_key")
            if (apiKeys != null && apiKeys.length() > 0) apiKey = apiKeys.optJSONObject(0)?.optString("current_key", "") ?: ""
        }
        return BackendConfig(projectId = projectId, apiKey = apiKey)
    }

    private fun readBackendConfig(): BackendConfig? {
        val text = readAssetOrNull("up_backend.json") ?: return null
        val json = JSONObject(text)
        return BackendConfig(json.optString("project_id", ""), json.optString("api_key", ""))
    }

    private fun readAssetOrNull(name: String): String? = try {
        context.assets.open(name).use { input -> BufferedReader(InputStreamReader(input)).readText() }
    } catch (_: Exception) { null }

    private data class AuthResult(val idToken: String, val localId: String, val email: String)

    private fun signInEmail(cfg: BackendConfig, email: String, password: String): AuthResult? {
        return try {
            val body = JSONObject()
                .put("email", email)
                .put("password", password)
                .put("returnSecureToken", true)
                .toString()
            val response = request("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${cfg.apiKey}", "POST", body, null)
            val json = JSONObject(response)
            AuthResult(json.getString("idToken"), json.getString("localId"), json.getString("email"))
        } catch (_: Exception) { null }
    }

    private fun getDocsFromAny(cfg: BackendConfig, collections: List<String>, token: String?): List<FsDoc> {
        val all = mutableListOf<FsDoc>()
        collections.forEach { collection ->
            try { all += listCollection(cfg, collection, token) } catch (_: Exception) { }
        }
        return all
    }

    private fun listCollection(cfg: BackendConfig, collection: String, token: String?): List<FsDoc> {
        val url = "https://firestore.googleapis.com/v1/projects/${cfg.projectId}/databases/(default)/documents/$collection?pageSize=80&key=${cfg.apiKey}"
        val response = request(url, "GET", null, token)
        val json = JSONObject(response)
        val docs = json.optJSONArray("documents") ?: JSONArray()
        return (0 until docs.length()).mapNotNull { i -> docs.optJSONObject(i)?.toFsDoc(collection) }
    }

    private fun patchDocument(cfg: BackendConfig, documentName: String, fields: Map<String, Any?>, token: String?): Result<Unit> {
        return try {
            val mask = fields.keys.joinToString(separator = "") { "&updateMask.fieldPaths=$it" }
            val url = "https://firestore.googleapis.com/v1/$documentName?key=${cfg.apiKey}$mask"
            val body = JSONObject().put("fields", JSONObject().apply { fields.forEach { (k, v) -> put(k, fsValue(v)) } }).toString()
            request(url, "PATCH", body, token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Firebase recusou a atualização: ${e.message}"))
        }
    }

    private fun request(urlText: String, method: String, body: String?, token: String?): String {
        val conn = (URL(urlText).openConnection() as HttpURLConnection)
        conn.requestMethod = method
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        if (!token.isNullOrBlank()) conn.setRequestProperty("Authorization", "Bearer $token")
        if (body != null) {
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw Exception("HTTP $code $text")
        return text
    }
}

private fun JSONObject.toFsDoc(collection: String): FsDoc {
    val name = getString("name")
    val fields = optJSONObject("fields")?.toFieldsMap() ?: emptyMap()
    return FsDoc(name = name, id = name.substringAfterLast('/'), collection = collection, fields = fields)
}

private fun JSONObject.toFieldsMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val keys = keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = parseFsValue(getJSONObject(key))
    }
    return map
}

private fun parseFsValue(obj: JSONObject): Any? = when {
    obj.has("stringValue") -> obj.optString("stringValue")
    obj.has("integerValue") -> obj.optString("integerValue").toLongOrNull()
    obj.has("doubleValue") -> obj.optDouble("doubleValue")
    obj.has("booleanValue") -> obj.optBoolean("booleanValue")
    obj.has("timestampValue") -> obj.optString("timestampValue")
    obj.has("mapValue") -> obj.optJSONObject("mapValue")?.optJSONObject("fields")?.toFieldsMap() ?: emptyMap<String, Any?>()
    obj.has("arrayValue") -> {
        val arr = obj.optJSONObject("arrayValue")?.optJSONArray("values") ?: JSONArray()
        (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let { v -> parseFsValue(v) } }
    }
    else -> null
}

private fun fsValue(value: Any?): JSONObject = when (value) {
    is Boolean -> JSONObject().put("booleanValue", value)
    is Int -> JSONObject().put("integerValue", value)
    is Long -> JSONObject().put("integerValue", value.toString())
    is Float -> JSONObject().put("doubleValue", value.toDouble())
    is Double -> JSONObject().put("doubleValue", value)
    else -> JSONObject().put("stringValue", value?.toString() ?: "")
}

private fun FsDoc.valueAny(key: String): Any? = fields.entries.firstOrNull { it.key.equals(key, true) }?.value
private fun FsDoc.bestString(vararg keys: String): String? = keys.firstNotNullOfOrNull { key -> valueAny(key)?.toString()?.takeIf { it.isNotBlank() } }
private fun FsDoc.bestNumber(vararg keys: String): Double = keys.firstNotNullOfOrNull { key -> toDoubleOrNull(valueAny(key)) } ?: 0.0

private fun FsDoc.toRideItem(session: DriverSession): RideItem? {
    val status = bestString("statusEntrega", "statusPedidoCore", "status", "situacao") ?: "SEM_STATUS"
    val assigned = listOf("entregadorId", "entregadorUid", "driverId", "motoboyId", "uidEntregador").any { same(valueAny(it), session.id) } ||
            listOf("entregadorNome", "motoboyNome").any { same(valueAny(it), session.name) }
    val pickup = bestString("enderecoLoja", "lojaEndereco", "origemEndereco", "coletaEndereco", "lojaNome", "origem", "coleta") ?: mapAddress(valueAny("loja")) ?: "Coleta não informada"
    val delivery = bestString("enderecoEntrega", "entregaEndereco", "destinoEndereco", "enderecoCompleto", "clienteEndereco", "destino", "entrega") ?: mapAddress(valueAny("endereco")) ?: "Entrega não informada"
    val price = bestNumber("valorEntregador", "valorEntrega", "taxaEntrega", "frete", "valorCorrida", "valor")
    val distance = bestString("distanciaTexto", "distancia", "km", "distanciaKm") ?: bestNumber("distanciaKm", "km").takeIf { it > 0 }?.let { String.format(Locale("pt", "BR"), "%.1f km", it) } ?: "-- km"
    val eta = bestString("tempoEstimado", "tempo", "eta", "estimativa") ?: bestNumber("tempoMin", "minutos").takeIf { it > 0 }?.let { "${it.toInt()} min" } ?: "-- min"
    val created = bestString("criadoEm", "createdAt", "dataHora", "data_hora", "atualizadoEm")
    return RideItem(
        id = id,
        documentName = name,
        collection = collection,
        status = status,
        pickup = pickup,
        delivery = delivery,
        distance = distance,
        price = price,
        eta = eta,
        createdLabel = humanDate(created),
        assignedToCurrent = assigned
    )
}

private fun FsDoc.toNotice(): NoticeItem? {
    val title = bestString("titulo", "title", "nome") ?: return null
    val body = bestString("mensagem", "body", "descricao", "texto") ?: ""
    val time = humanDate(bestString("criadoEm", "createdAt", "data", "vigenciaInicio"))
    return NoticeItem(title, body, time)
}

private fun FsDoc.toBanner(): BannerItem? {
    val title = bestString("titulo", "title", "nome") ?: return null
    val subtitle = bestString("subtitulo", "subtitle", "descricao", "texto") ?: ""
    val button = bestString("botao", "button", "cta") ?: "Ver detalhes"
    return BannerItem(title, subtitle, button)
}

private fun buildStats(raw: List<FsDoc>, session: DriverSession): HomeStats {
    val assigned = raw.filter { doc ->
        listOf("entregadorId", "entregadorUid", "driverId", "motoboyId", "uidEntregador").any { same(doc.valueAny(it), session.id) } ||
                listOf("entregadorNome", "motoboyNome").any { same(doc.valueAny(it), session.name) }
    }
    val today = LocalDate.now()
    val weekStart = today.minusDays(6)
    val monthStart = today.withDayOfMonth(1)
    val finished = assigned.filter { normalizeStatus(it.bestString("statusEntrega", "statusPedidoCore", "status", "situacao")).let { s -> s in finishedStatuses } }
    fun dateOf(doc: FsDoc) = parseDate(doc.bestString("finalizadoEm", "entregueEm", "atualizadoEm", "criadoEm", "createdAt", "dataHora", "data_hora"))
    fun valueOf(doc: FsDoc) = doc.bestNumber("valorEntregador", "valorEntrega", "taxaEntrega", "frete", "valorCorrida", "valor")
    val finishedToday = finished.filter { dateOf(it) == today }
    val finishedWeek = finished.filter { dateOf(it)?.let { d -> !d.isBefore(weekStart) && !d.isAfter(today) } == true }
    val finishedMonth = finished.filter { dateOf(it)?.let { d -> !d.isBefore(monthStart) && !d.isAfter(today) } == true }
    return HomeStats(
        earningsToday = finishedToday.sumOf { valueOf(it) },
        ridesToday = assigned.count { dateOf(it) == today },
        finishedToday = finishedToday.size,
        earningsWeek = finishedWeek.sumOf { valueOf(it) },
        earningsMonth = finishedMonth.sumOf { valueOf(it) },
        totalRidesMonth = finishedMonth.size,
        deliveryFees = finishedToday.sumOf { valueOf(it) },
        tips = finishedToday.sumOf { it.bestNumber("gorjeta", "tip") },
        discounts = finishedToday.sumOf { it.bestNumber("desconto", "descontos") }
    )
}

private val availableStatuses = setOf("PENDENTE", "DISPONIVEL", "AGUARDANDO_ENTREGADOR", "NOVA_CORRIDA", "OFERTA", "CRIADO", "PRONTO", "PEDIDO_PRONTO", "AGUARDANDO_DESPACHO")
private val activeStatuses = setOf("ACEITA", "ACEITO", "A_CAMINHO_COLETA", "INDO_COLETA", "NA_COLETA", "PEDIDO_RETIRADO", "EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE", "ENTREGADOR_NO_LOCAL", "OCORRENCIA")
private val finishedStatuses = setOf("FINALIZADA", "FINALIZADO", "ENTREGUE", "CONCLUIDO", "CONCLUIDA")
private val historyStatuses = finishedStatuses + setOf("RECUSADA", "RECUSADO", "EXPIRADA", "EXPIRADO", "CANCELADA", "CANCELADO")

private fun RideItem.isAvailable(): Boolean = normalizeStatus(status) in availableStatuses && !assignedToCurrent
private fun RideItem.isActive(): Boolean = normalizeStatus(status) in activeStatuses
private fun RideItem.isHistory(): Boolean = normalizeStatus(status) in historyStatuses

private fun nextStatus(status: String): String = when (normalizeStatus(status)) {
    "ACEITA", "ACEITO", "A_CAMINHO_COLETA", "INDO_COLETA" -> "NA_COLETA"
    "NA_COLETA" -> "PEDIDO_RETIRADO"
    "PEDIDO_RETIRADO" -> "EM_ROTA"
    "EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE" -> "ENTREGADOR_NO_LOCAL"
    "ENTREGADOR_NO_LOCAL" -> "FINALIZADA"
    else -> "EM_ROTA"
}

private fun statusHuman(status: String): String = when (normalizeStatus(status)) {
    "DISPONIVEL" -> "Disponível"
    "INDISPONIVEL" -> "Indisponível"
    "ACEITA", "ACEITO" -> "Aceita"
    "NA_COLETA" -> "Na coleta"
    "PEDIDO_RETIRADO" -> "Pedido retirado"
    "EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE" -> "Em rota"
    "ENTREGADOR_NO_LOCAL" -> "Chegou no cliente"
    "FINALIZADA", "ENTREGUE", "CONCLUIDO" -> "Finalizada"
    "RECUSADA" -> "Recusada"
    "EXPIRADA" -> "Expirada"
    else -> status.replace('_', ' ').lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }
}

private fun actionLabel(status: String): String = when (normalizeStatus(status)) {
    "ACEITA", "ACEITO", "A_CAMINHO_COLETA", "INDO_COLETA" -> "Cheguei na coleta"
    "NA_COLETA" -> "Retirei pedido"
    "PEDIDO_RETIRADO" -> "Iniciar entrega"
    "EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE" -> "Cheguei no cliente"
    "ENTREGADOR_NO_LOCAL" -> "Finalizar entrega"
    else -> "Atualizar"
}

private fun normalizeStatus(value: String?): String = (value ?: "").trim().uppercase(Locale.ROOT)
    .replace("Á", "A").replace("À", "A").replace("Ã", "A").replace("Â", "A")
    .replace("É", "E").replace("Ê", "E").replace("Í", "I").replace("Ó", "O")
    .replace("Õ", "O").replace("Ô", "O").replace("Ú", "U").replace("Ç", "C")
    .replace(" ", "_").replace("-", "_")

private fun digits(value: String): String = value.filter { it.isDigit() }
private fun same(a: Any?, b: Any?): Boolean = a?.toString()?.trim()?.equals(b?.toString()?.trim(), ignoreCase = true) == true
private fun toDoubleOrNull(value: Any?): Double? = when (value) {
    is Double -> value
    is Float -> value.toDouble()
    is Int -> value.toDouble()
    is Long -> value.toDouble()
    is String -> value.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()
    else -> null
}

@Suppress("UNCHECKED_CAST")
private fun mapAddress(value: Any?): String? {
    val map = value as? Map<String, Any?> ?: return null
    val rua = map["rua"] ?: map["logradouro"] ?: map["endereco"] ?: map["bairro"]
    val numero = map["numero"] ?: map["n"]
    return listOfNotNull(rua?.toString(), numero?.toString()).filter { it.isNotBlank() }.joinToString(", ").takeIf { it.isNotBlank() }
}

private fun parseDate(value: String?): LocalDate? = try {
    if (value.isNullOrBlank()) null else Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDate()
} catch (_: Exception) { null }

private fun humanDate(value: String?): String {
    val date = parseDate(value) ?: return "--"
    val today = LocalDate.now()
    return when (date) {
        today -> "Hoje"
        today.minusDays(1) -> "Ontem"
        else -> "%02d/%02d".format(date.dayOfMonth, date.monthValue)
    }
}

private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

@Composable
private fun LoginScreen(state: AppState, onLogin: (String, String) -> Unit) {
    var document by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoUp(size = 92)
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.login_illustration),
                    contentDescription = null,
                    modifier = Modifier.size(160.dp).clip(RoundedCornerShape(30.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text("Bem-vindo", color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 29.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Acesse sua conta real para receber corridas.", color = TextMuted, fontFamily = AppFont, lineHeight = 22.sp, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(36.dp))
            FieldLabel("CPF, telefone ou e-mail")
            OutlinedTextField(
                value = document,
                onValueChange = { document = it },
                modifier = Modifier.fillMaxWidth().height(62.dp),
                leadingIcon = { Icon(Icons.Rounded.Person, null, tint = TextMuted) },
                placeholder = { Text("Digite CPF, telefone ou e-mail", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(fontFamily = AppFont, fontSize = 15.sp),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(20.dp))
            FieldLabel("Senha")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth().height(62.dp),
                leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = TextMuted) },
                trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = TextMuted) } },
                placeholder = { Text("Digite sua senha", color = TextMuted) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(fontFamily = AppFont, fontSize = 15.sp),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Esqueci minha senha", color = UpGreen, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.align(Alignment.End).padding(vertical = 8.dp))
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { onLogin(document, password) },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UpGreen)
            ) { Text(if (state.loading) "Conectando..." else "Entrar", fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.5.dp, UpGreen), colors = ButtonDefaults.outlinedButtonColors(contentColor = UpGreen)) {
                Text("Solicitar cadastro", fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            state.loginMessage?.let {
                Spacer(Modifier.height(16.dp))
                InfoBox(it, warning = !state.config.configured)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 34.dp, bottom = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Security, null, tint = TextMuted, modifier = Modifier.size(19.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cadastro sujeito à aprovação", color = TextMuted, fontFamily = AppFont, fontSize = 13.sp)
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = UpGreen,
    unfocusedBorderColor = CardStroke,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = UpGreen,
    focusedTextColor = TextStrong,
    unfocusedTextColor = TextStrong
)

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, color = TextStrong, fontFamily = AppFont, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    Inicio("Início", Icons.Rounded.Home),
    Corridas("Corridas", Icons.Rounded.Work),
    Carteira("Carteira", Icons.Rounded.AccountBalanceWallet),
    Notificacoes("Notificações", Icons.Rounded.NotificationsNone),
    Mais("Mais", Icons.Rounded.Menu)
}

@Composable
private fun DriverMainScreen(state: AppState, vm: DriverViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.Inicio) }
    Scaffold(containerColor = AppBg, bottomBar = { DriverBottomBar(selected = selectedTab, onSelect = { selectedTab = it }) }) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                AppTab.Inicio -> HomeScreen(state, vm, onNavigate = { selectedTab = it })
                AppTab.Corridas -> RidesScreen(state, vm)
                AppTab.Carteira -> WalletScreen(state, vm)
                AppTab.Notificacoes -> NotificationsScreen(state, vm)
                AppTab.Mais -> MoreScreen(state, vm)
            }
        }
    }
}

@Composable
private fun DriverBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    NavigationBar(modifier = Modifier.fillMaxWidth().navigationBarsPadding(), tonalElevation = 10.dp, containerColor = Color.White) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, tab.label, modifier = Modifier.size(24.dp)) },
                label = { Text(tab.label, fontFamily = AppFont, fontSize = 11.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = UpGreen, selectedTextColor = UpGreen, indicatorColor = UpGreenSoft, unselectedIconColor = Color(0xFF7B828B), unselectedTextColor = Color(0xFF7B828B))
            )
        }
    }
}

@Composable
private fun HomeScreen(state: AppState, vm: DriverViewModel, onNavigate: (AppTab) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(AppBg).padding(WindowInsets.statusBars.asPaddingValues()).verticalScroll(rememberScrollState()).padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 22.dp)) {
        DriverHeader(state, onRefresh = vm::refreshAll, onNotifications = { onNavigate(AppTab.Notificacoes) }, onMessages = { onNavigate(AppTab.Mais) })
        Spacer(Modifier.height(20.dp))
        StatusButton(state, onToggle = vm::toggleAvailability)
        Spacer(Modifier.height(16.dp))
        state.message?.let { InfoBox(it, warning = it.contains("não", true) || it.contains("sem", true)); Spacer(Modifier.height(12.dp)) }
        EarningsCard(state.stats)
        Spacer(Modifier.height(18.dp))
        HomeCarousel(state.banners)
        Spacer(Modifier.height(22.dp))
        QuickActionsGrid(onNavigate)
    }
}

@Composable
private fun DriverHeader(state: AppState, onRefresh: () -> Unit, onNotifications: () -> Unit, onMessages: () -> Unit) {
    val name = state.session?.name ?: "Entregador"
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(R.drawable.avatar_diego), contentDescription = "Foto do entregador", modifier = Modifier.size(68.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Olá, ${name.substringBefore(' ')}", color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Spacer(Modifier.height(5.dp))
            Text(state.lastSyncLabel, color = TextMuted, fontFamily = AppFont, fontSize = 13.sp)
        }
        IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, "Atualizar", tint = TextStrong, modifier = Modifier.size(25.dp)) }
        Box(modifier = Modifier.clickable(onClick = onNotifications)) {
            Icon(Icons.Rounded.NotificationsNone, "Notificações", tint = TextStrong, modifier = Modifier.size(29.dp))
            if (state.notices.isNotEmpty()) Box(modifier = Modifier.size(8.dp).align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp).clip(CircleShape).background(UpGreen))
        }
        Spacer(modifier = Modifier.width(18.dp))
        Icon(Icons.Rounded.ChatBubbleOutline, "Mensagens", tint = TextStrong, modifier = Modifier.size(28.dp).clickable(onClick = onMessages))
    }
}

@Composable
private fun StatusButton(state: AppState, onToggle: () -> Unit) {
    val status = state.session?.status ?: "INDISPONIVEL"
    val isAvailable = normalizeStatus(status) == "DISPONIVEL"
    val color = if (isAvailable) UpGreen else Color(0xFF2F3438)
    Box(modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(20.dp)).background(Brush.horizontalGradient(listOf(color, if (isAvailable) Color(0xFF008435) else Color(0xFF4A4F55), color))).clickable(onClick = onToggle), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Wifi, null, tint = Color.White, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(12.dp))
            Text(statusHuman(status), color = Color.White, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }
        Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp).size(28.dp))
    }
}

@Composable
private fun EarningsCard(stats: HomeStats) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth().height(112.dp), shape = 24) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ganhos de hoje", color = TextMuted, fontSize = 15.sp, fontFamily = AppFont)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.Visibility, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(money(stats.earningsToday), color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 31.sp, maxLines = 1)
            }
            Spacer(Modifier.width(14.dp))
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(CardStroke))
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.width(116.dp)) {
                MetricLine(Icons.Rounded.Work, stats.ridesToday.toString(), "Corridas")
                Spacer(Modifier.height(14.dp))
                MetricLine(Icons.Rounded.CheckCircleOutline, stats.finishedToday.toString(), "Finalizadas")
            }
        }
    }
}

@Composable
private fun MetricLine(icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(value, color = TextStrong, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, fontFamily = AppFont)
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextMuted, fontSize = 12.sp, fontFamily = AppFont)
    }
}

@Composable
private fun HomeCarousel(banners: List<BannerItem>) {
    val banner = banners.firstOrNull()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().height(196.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF073D25))) {
            Image(painter = painterResource(R.drawable.banner_novidades), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (banner == null) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xAA073D25)))
                Column(modifier = Modifier.padding(22.dp).align(Alignment.CenterStart)) {
                    Text("SEM BANNERS", color = Color(0xFF7DD321), fontFamily = AppFont, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Carrossel vazio", color = Color.White, fontFamily = AppFont, fontWeight = FontWeight.Black, fontSize = 27.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Cadastre banners reais no gestor.", color = Color.White.copy(alpha = 0.9f), fontFamily = AppFont, fontSize = 14.sp)
                }
            } else {
                Column(modifier = Modifier.padding(22.dp).align(Alignment.CenterStart).widthIn(max = 210.dp)) {
                    Text("NOVIDADES", color = Color(0xFF7DD321), fontFamily = AppFont, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(banner.title, color = Color.White, fontFamily = AppFont, fontWeight = FontWeight.Black, fontSize = 25.sp, lineHeight = 28.sp, maxLines = 2)
                    Spacer(Modifier.height(8.dp))
                    Text(banner.subtitle, color = Color.White.copy(alpha = 0.9f), fontFamily = AppFont, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(22.dp).height(8.dp).clip(RoundedCornerShape(20.dp)).background(UpGreen))
            Spacer(Modifier.width(8.dp)); Dot(false); Spacer(Modifier.width(8.dp)); Dot(false)
        }
    }
}

@Composable
private fun Dot(active: Boolean) { Box(modifier = Modifier.size(if (active) 9.dp else 8.dp).clip(CircleShape).background(if (active) UpGreen else Color(0xFFD0D5D2))) }

@Composable
private fun QuickActionsGrid(onNavigate: (AppTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction(Icons.Rounded.History, "Histórico", "Ver corridas", Modifier.weight(1f)) { onNavigate(AppTab.Corridas) }
        QuickAction(Icons.Rounded.AccountBalanceWallet, "Ganhos", "Resumo\nfinanceiro", Modifier.weight(1f)) { onNavigate(AppTab.Carteira) }
        QuickAction(Icons.Rounded.Place, "Mapa", "Ver região", Modifier.weight(1f)) { onNavigate(AppTab.Corridas) }
        QuickAction(Icons.Rounded.HeadsetMic, "Suporte", "Fale conosco", Modifier.weight(1f)) { onNavigate(AppTab.Mais) }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedWhiteCard(modifier = modifier.height(128.dp).clickable(onClick = onClick), shape = 18) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = UpGreen, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(9.dp))
            Text(title, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, textAlign = TextAlign.Center, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextMuted, fontFamily = AppFont, fontSize = 11.5.sp, textAlign = TextAlign.Center, lineHeight = 15.sp, maxLines = 2)
        }
    }
}

@Composable
private fun RidesScreen(state: AppState, vm: DriverViewModel) {
    var selected by remember { mutableIntStateOf(0) }
    val rides = when (selected) { 0 -> state.availableRides; 1 -> state.activeRides; else -> state.historyRides }
    Column(modifier = Modifier.fillMaxSize().background(AppBg).padding(WindowInsets.statusBars.asPaddingValues()).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp)) {
        ScreenTitle("Corridas", trailing = { Row { IconButton(onClick = vm::refreshAll) { Icon(Icons.Rounded.Refresh, null, tint = TextStrong) }; Icon(Icons.Rounded.FilterAlt, null, tint = TextStrong, modifier = Modifier.size(27.dp)) } })
        Spacer(Modifier.height(20.dp))
        SegmentTabs(listOf("Disponíveis", "Em andamento", "Histórico"), selectedIndex = selected, onSelect = { selected = it })
        Spacer(Modifier.height(18.dp))
        if (selected == 0) {
            InfoStrip("As corridas abaixo vêm do Firebase. Se não aparecer nada, não há oferta real disponível agora.")
            Spacer(Modifier.height(14.dp))
        }
        if (rides.isEmpty()) EmptyState(if (selected == 0) "Nenhuma corrida real disponível" else if (selected == 1) "Nenhuma corrida em andamento" else "Nenhum histórico real encontrado")
        rides.forEach { ride ->
            RideCard(ride = ride, mode = selected, onAccept = { vm.acceptRide(ride) }, onAdvance = { vm.advanceRide(ride) })
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun SegmentTabs(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(index) }) {
                Text(label, color = if (index == selectedIndex) UpGreen else TextStrong, fontFamily = AppFont, fontWeight = if (index == selectedIndex) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.height(2.dp).width(90.dp).clip(RoundedCornerShape(10.dp)).background(if (index == selectedIndex) UpGreen else Color.Transparent))
            }
        }
    }
}

@Composable
private fun RideCard(ride: RideItem, mode: Int, onAccept: () -> Unit, onAdvance: () -> Unit) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth(), shape = 20) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(tagColor(ride.status)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                    Text(statusHuman(ride.status), color = UpGreenDark, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(ride.distance, color = TextMuted, fontFamily = AppFont, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    LocationLine(UpGreen, ride.pickup)
                    Spacer(Modifier.height(12.dp))
                    LocationLine(Warning, ride.delivery)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(money(ride.price), color = UpGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(ride.createdLabel, color = TextStrong, fontFamily = AppFont, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AccessTime, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Estimativa: ${ride.eta}", color = TextMuted, fontFamily = AppFont, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                if (mode == 0) {
                    Button(onClick = onAccept, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = UpGreenSoft, contentColor = UpGreen)) { Text("Aceitar", fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) }
                } else if (mode == 1) {
                    Button(onClick = onAdvance, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = UpGreen, contentColor = Color.White)) { Text(actionLabel(ride.status), fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) }
                }
            }
        }
    }
}

private fun tagColor(status: String): Color = when (normalizeStatus(status)) {
    in finishedStatuses -> UpGreenSoft
    "EXPIRADA", "RECUSADA", "CANCELADA" -> Color(0xFFFFE8E8)
    "ENTREGADOR_NO_LOCAL" -> Color(0xFFFFF2DD)
    else -> UpGreenSoft
}

@Composable
private fun LocationLine(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextStrong, fontFamily = AppFont, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WalletScreen(state: AppState, vm: DriverViewModel) {
    var visible by remember { mutableStateOf(true) }
    val stats = state.stats
    Column(modifier = Modifier.fillMaxSize().background(AppBg).padding(WindowInsets.statusBars.asPaddingValues()).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp)) {
        ScreenTitle("Carteira", trailing = { Row { IconButton(onClick = vm::refreshAll) { Icon(Icons.Rounded.Refresh, null, tint = TextStrong) }; IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, null, tint = TextStrong) } } })
        Spacer(Modifier.height(18.dp))
        ElevatedWhiteCard(modifier = Modifier.fillMaxWidth(), shape = 26) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Ganhos disponíveis", color = TextMuted, fontFamily = AppFont, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(if (visible) money(stats.earningsToday) else "••••••", color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 38.sp)
                Spacer(Modifier.height(12.dp))
                Text("Valores calculados pelas corridas finalizadas encontradas no Firebase.", color = TextMuted, fontFamily = AppFont, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SmallBalance("Hoje", if (visible) money(stats.earningsToday) else "••••", Modifier.weight(1f))
            SmallBalance("Semana", if (visible) money(stats.earningsWeek) else "••••", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SmallBalance("Mês", if (visible) money(stats.earningsMonth) else "••••", Modifier.weight(1f))
            SmallBalance("Corridas", stats.totalRidesMonth.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Text("Resumo real", color = TextStrong, fontFamily = AppFont, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        FinanceRow("Taxas de entrega", if (visible) money(stats.deliveryFees) else "••••")
        FinanceRow("Gorjetas", if (visible) money(stats.tips) else "••••")
        FinanceRow("Descontos", if (visible) money(stats.discounts) else "••••")
    }
}

@Composable
private fun SmallBalance(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedWhiteCard(modifier = modifier.height(96.dp), shape = 18) { Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) { Text(title, color = TextMuted, fontFamily = AppFont, fontSize = 13.sp); Spacer(Modifier.height(8.dp)); Text(value, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) } }
}

@Composable
private fun FinanceRow(title: String, value: String) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = 16) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, color = TextStrong, fontFamily = AppFont, fontSize = 15.sp, modifier = Modifier.weight(1f)); Text(value, color = UpGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp) } }
}

@Composable
private fun NotificationsScreen(state: AppState, vm: DriverViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(AppBg).padding(WindowInsets.statusBars.asPaddingValues()).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp)) {
        ScreenTitle("Notificações", trailing = { IconButton(onClick = vm::refreshAll) { Icon(Icons.Rounded.Refresh, null, tint = TextStrong) } })
        Spacer(Modifier.height(18.dp))
        if (state.notices.isEmpty()) EmptyState("Nenhuma notificação real cadastrada")
        state.notices.forEach { NotificationItem(it.title, it.body, it.time) }
    }
}

@Composable
private fun NotificationItem(title: String, body: String, time: String) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = 18) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(UpGreenSoft), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Notifications, null, tint = UpGreen) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) { Text(title, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp); Spacer(Modifier.height(4.dp)); Text(body, color = TextMuted, fontFamily = AppFont, fontSize = 12.sp, lineHeight = 16.sp) }
            Text(time, color = TextMuted, fontFamily = AppFont, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MoreScreen(state: AppState, vm: DriverViewModel) {
    val session = state.session
    Column(modifier = Modifier.fillMaxSize().background(AppBg).padding(WindowInsets.statusBars.asPaddingValues()).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp)) {
        ScreenTitle("Mais")
        Spacer(Modifier.height(18.dp))
        ElevatedWhiteCard(modifier = Modifier.fillMaxWidth(), shape = 24) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(R.drawable.avatar_diego), contentDescription = null, modifier = Modifier.size(62.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) { Text(session?.name ?: "Entregador", color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp); Text(statusHuman(session?.status ?: "INDISPONIVEL"), color = UpGreen, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted)
            }
        }
        Spacer(Modifier.height(18.dp))
        MenuRow(Icons.Rounded.Settings, "Configurações", "Preferências do app")
        MenuRow(Icons.Rounded.Place, "Navegação", "Google Maps, Waze ou padrão")
        MenuRow(Icons.Rounded.Security, "Permissões", "Localização e alerta urgente")
        MenuRow(Icons.Rounded.HeadsetMic, "Suporte", "Fale com a operação")
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = vm::logout, modifier = Modifier.fillMaxWidth().height(54.dp), border = BorderStroke(1.dp, Color(0xFFFFB4A9)), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Rounded.Logout, null, tint = Color(0xFFE24537)); Spacer(Modifier.width(8.dp)); Text("Sair", color = Color(0xFFE24537), fontFamily = AppFont, fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, subtitle: String) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = 18) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(UpGreenSoft), contentAlignment = Alignment.Center) { Icon(icon, null, tint = UpGreen, modifier = Modifier.size(23.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) { Text(title, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp); Text(subtitle, color = TextMuted, fontFamily = AppFont, fontSize = 12.sp) }
            Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted)
        }
    }
}

@Composable
private fun InfoBox(text: String, warning: Boolean) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth(), shape = 16, container = if (warning) Color(0xFFFFF8E6) else UpGreenSoft) {
        Text(text, color = if (warning) Color(0xFF815A00) else UpGreenDark, fontFamily = AppFont, fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(14.dp))
    }
}

@Composable
private fun InfoStrip(text: String) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth(), shape = 18, container = UpGreenSoft) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Wifi, null, tint = UpGreen, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, color = TextStrong, fontFamily = AppFont, fontSize = 13.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ChevronRight, null, tint = UpGreen)
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth(), shape = 22) {
        Column(modifier = Modifier.fillMaxWidth().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Work, null, tint = TextMuted, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(10.dp))
            Text(text, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text("Nada inventado será exibido aqui.", color = TextMuted, fontFamily = AppFont, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ScreenTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
private fun ElevatedWhiteCard(modifier: Modifier = Modifier, shape: Int = 20, container: Color = Color.White, content: @Composable () -> Unit) {
    Card(modifier = modifier.border(1.dp, CardStroke, RoundedCornerShape(shape.dp)), shape = RoundedCornerShape(shape.dp), colors = CardDefaults.cardColors(containerColor = container), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) { content() }
}

@Composable
private fun LogoUp(size: Int) {
    Column {
        Text("up", color = UpGreen, fontFamily = AppFont, fontWeight = FontWeight.Black, fontSize = (size / 2.2f).sp, lineHeight = (size / 2.5f).sp)
        Text("entregas", color = UpGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = (size / 6f).sp)
    }
}
