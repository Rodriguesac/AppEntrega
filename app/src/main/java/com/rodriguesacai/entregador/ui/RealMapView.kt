package com.rodriguesacai.entregador.ui

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.abs

private const val TOMTOM_API_KEY = "tmsKTjnNOPUHNDHOYh2m12VrmwejmK8t"

private val MapPanel = Color(0xFF0E1117)
private val MapPanel2 = Color(0xFF151A22)
private val MapPurple = Color(0xFF7C4DFF)
private val MapGreen = Color(0xFF82C91E)
private val MapText = Color.White
private val MapMuted = Color(0xFFC8CDD6)

private data class RouteMapState(
    val pickup: GeoPoint? = null,
    val dropoff: GeoPoint? = null,
    val route: List<GeoPoint> = emptyList(),
    val loading: Boolean = true,
    val label: String = "Carregando mapa real"
)

@Composable
fun RealDeliveryMap(
    title: String,
    subtitle: String,
    pickupAddress: String,
    dropoffAddress: String,
    pickupLat: Double? = null,
    pickupLng: Double? = null,
    dropoffLat: Double? = null,
    dropoffLng: Double? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var state by remember(pickupAddress, dropoffAddress, pickupLat, pickupLng, dropoffLat, dropoffLng) {
        mutableStateOf(RouteMapState())
    }

    LaunchedEffect(pickupAddress, dropoffAddress, pickupLat, pickupLng, dropoffLat, dropoffLng) {
        state = RouteMapState(loading = true)
        state = withContext(Dispatchers.IO) {
            buildRouteMapState(pickupAddress, dropoffAddress, pickupLat, pickupLng, dropoffLat, dropoffLng)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(245.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(MapPanel2, MapPanel)))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(26.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    minZoomLevel = 4.0
                    maxZoomLevel = 20.0
                    controller.setZoom(14.5)
                    controller.setCenter(GeoPoint(-20.4697, -54.6201))
                    setOnTouchListener { view, event ->
                        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                        } else {
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        false
                    }
                }
            },
            update = { map -> applyRouteToMap(map, state) }
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.04f), Color.Black.copy(alpha = 0.42f))))
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xE60A0C11))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(title, color = MapText, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = MapMuted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xE60A0C11))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MapLegendDot(MapGreen)
            Text("Coleta", color = MapText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            MapLegendDot(MapPurple)
            Text("Entrega", color = MapText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (state.loading) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE60A0C11))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MapGreen, strokeWidth = 3.dp)
                Spacer(Modifier.height(8.dp))
                Text("Carregando mapa", color = MapText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else if (state.pickup == null && state.dropoff == null) {
            Text(
                "Mapa real indisponível para este endereço",
                color = MapText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xE60A0C11))
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun MapLegendDot(color: Color) {
    Box(
        Modifier
            .padding(end = 6.dp)
            .width(10.dp)
            .height(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private fun applyRouteToMap(map: MapView, state: RouteMapState) {
    map.overlays.clear()

    val pickup = state.pickup
    val dropoff = state.dropoff
    val points = state.route.ifEmpty { listOfNotNull(pickup, dropoff) }

    if (points.isNotEmpty()) {
        val center = points.centerPoint()
        map.controller.setCenter(center)
        map.controller.setZoom(points.bestZoom())
    }

    if (points.size >= 2) {
        val line = Polyline().apply {
            setPoints(points)
            outlinePaint.color = android.graphics.Color.rgb(130, 201, 30)
            outlinePaint.strokeWidth = 9f
            outlinePaint.isAntiAlias = true
        }
        map.overlays.add(line)
    }

    if (pickup != null) {
        map.overlays.add(Marker(map).apply {
            position = pickup
            title = "Coleta"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        })
    }

    if (dropoff != null) {
        map.overlays.add(Marker(map).apply {
            position = dropoff
            title = "Entrega"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        })
    }

    map.invalidate()
}

private suspend fun buildRouteMapState(
    pickupAddress: String,
    dropoffAddress: String,
    pickupLat: Double?,
    pickupLng: Double?,
    dropoffLat: Double?,
    dropoffLng: Double?
): RouteMapState {
    val pickup = coordinateOrNull(pickupLat, pickupLng) ?: geocodeTomTom(pickupAddress)
    val dropoff = coordinateOrNull(dropoffLat, dropoffLng) ?: geocodeTomTom(dropoffAddress)
    val route = if (pickup != null && dropoff != null) fetchTomTomRoute(pickup, dropoff) else emptyList()
    val fallbackRoute = route.ifEmpty { listOfNotNull(pickup, dropoff) }
    return RouteMapState(
        pickup = pickup,
        dropoff = dropoff,
        route = fallbackRoute,
        loading = false,
        label = if (route.isNotEmpty()) "Rota real" else "Mapa real"
    )
}

private fun coordinateOrNull(lat: Double?, lng: Double?): GeoPoint? {
    val safeLat = lat ?: return null
    val safeLng = lng ?: return null
    if (safeLat == 0.0 || safeLng == 0.0) return null
    if (abs(safeLat) > 90 || abs(safeLng) > 180) return null
    return GeoPoint(safeLat, safeLng)
}

private fun geocodeTomTom(address: String): GeoPoint? {
    val clean = address.trim()
    if (clean.length < 4) return null
    return runCatching {
        val query = URLEncoder.encode("$clean, Campo Grande, MS, Brasil", "UTF-8")
        val url = "https://api.tomtom.com/search/2/geocode/$query.json?key=$TOMTOM_API_KEY&countrySet=BR&limit=1"
        val json = httpGetJson(url)
        val position = JSONObject(json)
            .optJSONArray("results")
            ?.optJSONObject(0)
            ?.optJSONObject("position")
        val lat = position?.optDouble("lat") ?: return null
        val lon = position.optDouble("lon")
        coordinateOrNull(lat, lon)
    }.getOrNull()
}

private fun fetchTomTomRoute(pickup: GeoPoint, dropoff: GeoPoint): List<GeoPoint> {
    return runCatching {
        val url = "https://api.tomtom.com/routing/1/calculateRoute/${pickup.latitude},${pickup.longitude}:${dropoff.latitude},${dropoff.longitude}/json?key=$TOMTOM_API_KEY&traffic=true&routeType=fastest&travelMode=motorcycle"
        val json = httpGetJson(url)
        val points = JSONObject(json)
            .optJSONArray("routes")
            ?.optJSONObject(0)
            ?.optJSONArray("legs")
            ?.optJSONObject(0)
            ?.optJSONArray("points")
            ?: return emptyList()
        buildList {
            for (i in 0 until points.length()) {
                val point = points.optJSONObject(i) ?: continue
                val lat = point.optDouble("latitude")
                val lon = point.optDouble("longitude")
                coordinateOrNull(lat, lon)?.let { add(it) }
            }
        }
    }.getOrElse { emptyList() }
}

private fun httpGetJson(url: String): String {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 9000
        readTimeout = 12000
        useCaches = true
    }
    return try {
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        stream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun List<GeoPoint>.centerPoint(): GeoPoint {
    val lat = map { it.latitude }.average().takeIf { !it.isNaN() } ?: -20.4697
    val lon = map { it.longitude }.average().takeIf { !it.isNaN() } ?: -54.6201
    return GeoPoint(lat, lon)
}

private fun List<GeoPoint>.bestZoom(): Double {
    if (size < 2) return 15.2
    val latSpread = (maxOf { it.latitude } - minOf { it.latitude }).let { abs(it) }
    val lonSpread = (maxOf { it.longitude } - minOf { it.longitude }).let { abs(it) }
    val spread = maxOf(latSpread, lonSpread)
    return when {
        spread < 0.006 -> 16.2
        spread < 0.018 -> 15.0
        spread < 0.045 -> 13.8
        spread < 0.090 -> 12.8
        else -> 11.7
    }
}
