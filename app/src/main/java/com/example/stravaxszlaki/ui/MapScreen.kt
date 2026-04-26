package com.example.stravaxszlaki.ui

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.stravaxszlaki.Secrets
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.example.stravaxszlaki.api.LocalRoute
import com.example.stravaxszlaki.api.StravaApi
import com.example.stravaxszlaki.loadRoutesLocally
import com.example.stravaxszlaki.saveRoutesLocally
import com.example.stravaxszlaki.map.createBlueDotBitmap
import com.example.stravaxszlaki.map.createDirectionalDotBitmap
import com.example.stravaxszlaki.map.decodePolyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val allRoutes = remember { mutableStateListOf<LocalRoute>() }
    var selectedRouteId by remember { mutableStateOf<Long?>(null) }

    var showRoutes by remember { mutableStateOf(true) }
    var mapLayer by remember { mutableStateOf("Outdoor") }
    var showLayerMenu by remember { mutableStateOf(false) }

    var mapViewReference by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayReference by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            Log.d("GPS", "Zezwolono na lokalizację")
            locationOverlayReference?.enableMyLocation()
            locationOverlayReference?.enableFollowLocation()
        }
    }

    LaunchedEffect(Unit) {
        allRoutes.addAll(loadRoutesLocally(context))
    }
    BottomSheetScaffold(
        sheetPeekHeight = 100.dp,
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Twoje trasy (${allRoutes.size})", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("strava_prefs", Context.MODE_PRIVATE)
                        val token = prefs.getString("access_token", null)
                        if (token == null) return@Button

                        coroutineScope.launch {
                            try {
                                val retrofit = retrofit2.Retrofit.Builder()
                                    .baseUrl("https://www.strava.com/")
                                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                                    .build()
                                val api = retrofit.create(StravaApi::class.java)

                                Log.d("STRAVA_API", "1. Sprawdzam, co już mamy w telefonie...")
                                val alreadySavedIds = allRoutes.map { it.id }.toSet()

                                Log.d("STRAVA_API", "2. Pytam Stravę o listę nowości...")
                                val activities = api.getActivities("Bearer $token", perPage = 200)

                                // Wyłuskujemy tylko te, których jeszcze nie mamy!
                                val newActivitiesToDownload = activities.filter { !alreadySavedIds.contains(it.id) }

                                if (newActivitiesToDownload.isEmpty()) {
                                    Log.d("STRAVA_API", "Brak nowych tras! Jesteś na bieżąco.")
                                    return@launch // Wychodzimy, nie ma co robić!
                                }

                                Log.d("STRAVA_API", "Znaleziono ${newActivitiesToDownload.size} nowych tras. Pobieram szczegóły...")
                                val newLocalRoutes = mutableListOf<LocalRoute>()

                                // Pobieramy szczegóły TYLKO dla tych paru nowych tras
                                for (activity in newActivitiesToDownload) {
                                    // Zabezpieczenie: Jeśli zostało mało zapytań i sypnie 429, przynajmniej zapiszemy ucięte z podsumowania
                                    var poly: String? = activity.map.summary_polyline
                                    try {
                                        val detailed = api.getActivityDetails("Bearer $token", activity.id)
                                        poly = detailed.map.polyline ?: poly
                                    } catch (e: Exception) {
                                        Log.e("STRAVA_API", "Nie udało się dociągnąć nieuciętej trasy ${activity.name}: ${e.message}")
                                    }

                                    if (poly != null) {
                                        newLocalRoutes.add(LocalRoute(
                                            id = activity.id,
                                            name = activity.name,
                                            distance = activity.distance,
                                            points = decodePolyline(poly)
                                        ))
                                    }
                                }

                                Log.d("STRAVA_API", "3. Zapisuję nowe trasy w telefonie...")
                                allRoutes.addAll(0, newLocalRoutes) // Dodajemy na samą górę listy!
                                saveRoutesLocally(context, allRoutes)
                                Log.d("STRAVA_API", "Gotowe!")

                            } catch (e: Exception) {
                                Log.e("STRAVA_API", "Główny błąd: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Odśwież ze Stravy (Load)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(allRoutes) { route ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    selectedRouteId = route.id
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedRouteId == route.id) Color(0xFFE1BEE7) else Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(route.name, fontWeight = FontWeight.Bold)
                                Text("${String.format("%.2f", route.distance / 1000)} km", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { context ->
                    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                    Configuration.getInstance().userAgentValue = context.packageName

                    MapView(context).apply {
                        mapViewReference = this
                        setMultiTouchControls(true)
                        controller.setZoom(14.5)
                        controller.setCenter(GeoPoint(49.2324, 19.9816))

                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)

                        val basicDot = createBlueDotBitmap()
                        val directionalDot = createDirectionalDotBitmap()

                        locationOverlay.setPersonIcon(basicDot)
                        locationOverlay.setDirectionArrow(basicDot, directionalDot)

                        locationOverlay.setPersonHotspot(basicDot.width / 2f, basicDot.height / 2f)

                        overlays.add(locationOverlay)
                        locationOverlayReference = locationOverlay
                    }
                },
                update = { mapView ->
                    val MAPY_CZ_API_KEY = Secrets.MAPY_CZ_KEY
                    val urlPattern = when(mapLayer) {
                        "Satelita" -> "https://api.mapy.cz/v1/maptiles/aerial/256/"
                        "Zwykła" -> "https://api.mapy.cz/v1/maptiles/basic/256/"
                        else -> "https://api.mapy.cz/v1/maptiles/outdoor/256/"
                    }

                    val mapSource = object : OnlineTileSourceBase("MapyCz_$mapLayer", 1, 19, 256, "", arrayOf(urlPattern)) {
                        override fun getTileURLString(pMapTileIndex: Long): String {
                            val z = MapTileIndex.getZoom(pMapTileIndex)
                            val x = MapTileIndex.getX(pMapTileIndex)
                            val y = MapTileIndex.getY(pMapTileIndex)
                            return baseUrl + z + "/" + x + "/" + y + "?apikey=" + MAPY_CZ_API_KEY
                        }
                    }
                    mapView.setTileSource(mapSource)

                    mapView.overlays.removeAll { it is org.osmdroid.views.overlay.Polyline }
                    var highlightedLine: org.osmdroid.views.overlay.Polyline? = null

                    if (showRoutes) {
                        for (route in allRoutes) {
                            val line = org.osmdroid.views.overlay.Polyline(mapView)
                            line.setPoints(route.points)
                            line.infoWindow = null

                            line.setOnClickListener { _, map, _ ->
                                selectedRouteId = route.id
                                map.invalidate()
                                true
                            }

                            if (selectedRouteId == route.id) {
                                line.outlinePaint.color = android.graphics.Color.YELLOW
                                line.outlinePaint.strokeWidth = 20f
                                highlightedLine = line
                            } else {
                                line.outlinePaint.color = android.graphics.Color.argb(100, 110, 20, 180)
                                line.outlinePaint.strokeWidth = 12f
                                mapView.overlays.add(line)
                            }
                            line.outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                            line.outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        }
                        highlightedLine?.let { mapView.overlays.add(it) }
                    }
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            Button(
                onClick = { navController.navigate("login_screen") },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Text("Ekran logowania")
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = { showRoutes = !showRoutes },
                    containerColor = if (showRoutes) Color.White else Color.LightGray
                ) {
                    Icon(if (showRoutes) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Ukryj trasy")
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(visible = showLayerMenu) {
                        Row(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            MapLayerOption("Turystyczna", mapLayer == "Outdoor") { mapLayer = "Outdoor"; showLayerMenu = false }
                            MapLayerOption("Satelita", mapLayer == "Satelita") { mapLayer = "Satelita"; showLayerMenu = false }
                            MapLayerOption("Standard", mapLayer == "Zwykła") { mapLayer = "Zwykła"; showLayerMenu = false }
                        }
                    }

                    FloatingActionButton(
                        onClick = { showLayerMenu = !showLayerMenu },
                        containerColor = Color.White
                    ) {
                        Icon(Icons.Default.Layers, "Warstwy")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                FloatingActionButton(
                    onClick = {
                        locationPermissionRequest.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))

                        locationOverlayReference?.let { overlay ->
                            if (overlay.isMyLocationEnabled) {
                                overlay.enableFollowLocation()
                                overlay.myLocation?.let { myLoc ->
                                    mapViewReference?.controller?.animateTo(myLoc)
                                    mapViewReference?.controller?.setZoom(16.0)
                                }
                            } else {
                                overlay.enableMyLocation()
                            }
                        }
                    },
                    containerColor = Color(0xFFE3F2FD)
                ) {
                    Icon(Icons.Default.MyLocation, "Lokalizacja", tint = Color(0xFF1976D2))
                }
            }
        }
    }
}



@Composable
fun MapLayerOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) Color(0xFFFC4C02) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color.LightGray)
        ) {

        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFFC4C02) else Color.Black
        )
    }
}