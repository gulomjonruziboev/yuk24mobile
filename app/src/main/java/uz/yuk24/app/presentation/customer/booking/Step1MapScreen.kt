package uz.yuk24.app.presentation.customer.booking

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import uz.yuk24.app.R
import uz.yuk24.app.domain.model.LatLng
import uz.yuk24.app.domain.model.LocationPoint
import uz.yuk24.app.presentation.common.components.PrimaryCtaButton
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary

private const val TASHKENT_LAT = 41.2995
private const val TASHKENT_LNG = 69.2401

private enum class TapTarget { PICKUP, DELIVERY }

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun Step1MapScreen(
    viewModel: BookingViewModel,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Tracks which pin was placed/touched most recently so taps after both pins
    // exist update the right one. Defaults to PICKUP so the first tap on a
    // fresh screen sets pickup.
    var lastTouched by remember { mutableStateOf(TapTarget.PICKUP) }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(TASHKENT_LAT, TASHKENT_LNG))
        }
    }

    val pickupMarker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_pin_red)
            isDraggable = true
            title = "Pickup"
        }
    }
    val deliveryMarker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_pin_green)
            isDraggable = true
            title = "Delivery"
        }
    }
    val polyline = remember {
        Polyline(mapView).apply {
            outlinePaint.color = android.graphics.Color.parseColor("#2563EB")
            outlinePaint.strokeWidth = 8f
        }
    }

    // Map tap → first-empty-pin-wins, then last-placed-wins.
    // Single tap on the empty map: sets pickup if pickup is null, otherwise
    // sets delivery if delivery is null, otherwise updates whichever pin was
    // placed/touched most recently.
    val mapEventsOverlay = remember {
        MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                val current = viewModel.state.value
                when {
                    current.pickup == null -> {
                        viewModel.setPickupAt(p.latitude, p.longitude)
                        lastTouched = TapTarget.PICKUP
                    }
                    current.delivery == null -> {
                        viewModel.setDeliveryAt(p.latitude, p.longitude)
                        lastTouched = TapTarget.DELIVERY
                    }
                    else -> when (lastTouched) {
                        TapTarget.PICKUP -> viewModel.setPickupAt(p.latitude, p.longitude)
                        TapTarget.DELIVERY -> viewModel.setDeliveryAt(p.latitude, p.longitude)
                    }
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean = false
        })
    }

    LaunchedEffect(Unit) {
        if (!mapView.overlays.contains(mapEventsOverlay)) {
            mapView.overlays.add(0, mapEventsOverlay)
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    // GPS auto-pickup with fallback for an empty location cache.
    LaunchedEffect(locationPermission.status) {
        if (!locationPermission.status.isGranted) {
            if (!locationPermission.status.shouldShowRationale) {
                locationPermission.launchPermissionRequest()
            }
            return@LaunchedEffect
        }
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            requestLocation(client) { loc ->
                val gp = GeoPoint(loc.latitude, loc.longitude)
                mapView.controller.animateTo(gp)
                if (viewModel.state.value.pickup == null) {
                    viewModel.setPickupAt(loc.latitude, loc.longitude)
                    lastTouched = TapTarget.PICKUP
                }
            }
        } catch (_: SecurityException) { /* ignored */ }
    }

    LaunchedEffect(state.pickup) {
        state.pickup?.let { p ->
            pickupMarker.position = GeoPoint(p.lat, p.lng)
            if (!mapView.overlays.contains(pickupMarker)) mapView.overlays.add(pickupMarker)
            mapView.invalidate()
        }
        updatePolyline(mapView, polyline, state.pickup, state.delivery, routeGeometry)
    }
    LaunchedEffect(state.delivery) {
        state.delivery?.let { p ->
            deliveryMarker.position = GeoPoint(p.lat, p.lng)
            if (!mapView.overlays.contains(deliveryMarker)) mapView.overlays.add(deliveryMarker)
            mapView.invalidate()
        }
        updatePolyline(mapView, polyline, state.pickup, state.delivery, routeGeometry)
    }
    LaunchedEffect(routeGeometry) {
        updatePolyline(mapView, polyline, state.pickup, state.delivery, routeGeometry)
    }

    DisposableEffect(pickupMarker, deliveryMarker) {
        pickupMarker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker?) {}
            override fun onMarkerDragEnd(marker: Marker?) {
                marker ?: return
                viewModel.setPickupAt(marker.position.latitude, marker.position.longitude)
                lastTouched = TapTarget.PICKUP
            }
            override fun onMarkerDragStart(marker: Marker?) {}
        })
        deliveryMarker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker?) {}
            override fun onMarkerDragEnd(marker: Marker?) {
                marker ?: return
                viewModel.setDeliveryAt(marker.position.latitude, marker.position.longitude)
                lastTouched = TapTarget.DELIVERY
            }
            override fun onMarkerDragStart(marker: Marker?) {}
        })
        onDispose { }
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceWhite)) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )
            if (!locationPermission.status.isGranted) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = stringResource(R.string.grant_location_permission),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            IconButton(
                onClick = {
                    if (locationPermission.status.isGranted) {
                        try {
                            val client = LocationServices.getFusedLocationProviderClient(context)
                            requestLocation(client) { loc ->
                                val gp = GeoPoint(loc.latitude, loc.longitude)
                                viewModel.setPickupAt(loc.latitude, loc.longitude)
                                lastTouched = TapTarget.PICKUP
                                mapView.controller.animateTo(gp)
                            }
                        } catch (_: SecurityException) { /* ignored */ }
                    } else {
                        locationPermission.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(SurfaceWhite, RoundedCornerShape(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = stringResource(R.string.use_current_location),
                    tint = Primary
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PinRow(
                    emoji = "🔴",
                    label = stringResource(R.string.pickup_label),
                    valueText = state.pickup?.label,
                    placeholder = stringResource(R.string.pickup_auto_hint)
                )
                Spacer(Modifier.height(8.dp))
                PinRow(
                    emoji = "🟢",
                    label = stringResource(R.string.delivery_label),
                    valueText = state.delivery?.label,
                    placeholder = stringResource(R.string.delivery_tap_hint)
                )

                Spacer(Modifier.height(12.dp))
                PrimaryCtaButton(
                    text = stringResource(R.string.continue_action),
                    onClick = onContinue,
                    enabled = state.hasRoute
                )
            }
        }
    }
}

@Composable
private fun PinRow(
    emoji: String,
    label: String,
    valueText: String?,
    placeholder: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = valueText ?: placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = if (valueText == null) TextSecondary else TextPrimary
            )
        }
    }
}

/**
 * Tries the cached `lastLocation` first; if it's null (fresh install, emulator
 * without a seeded location, device without a recent fix), falls back to an
 * active `getCurrentLocation` request. Callbacks fire on the main thread.
 */
@SuppressLint("MissingPermission")
private fun requestLocation(
    client: FusedLocationProviderClient,
    onLocation: (Location) -> Unit
) {
    client.lastLocation.addOnSuccessListener { cached: Location? ->
        if (cached != null) {
            onLocation(cached)
        } else {
            val cts = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { fresh: Location? -> fresh?.let(onLocation) }
        }
    }
}

/**
 * Renders the pickup → delivery polyline.
 *
 * If [geometry] from backend `POST /api/route` is available we draw the full
 * road-following path; otherwise we fall back to a straight pickup→delivery
 * line while the route loads or when geometry is unavailable.
 *
 * Camera behaviour: with a real route we zoom to fit the whole path, with the
 * straight-line fallback we just centre on the midpoint.
 */
private fun updatePolyline(
    mapView: MapView,
    polyline: Polyline,
    pickup: LocationPoint?,
    delivery: LocationPoint?,
    geometry: List<LatLng>
) {
    if (pickup == null || delivery == null) {
        mapView.overlays.remove(polyline)
        mapView.invalidate()
        return
    }

    val points: List<GeoPoint> = if (geometry.size >= 2) {
        geometry.map { GeoPoint(it.lat, it.lng) }
    } else {
        listOf(
            GeoPoint(pickup.lat, pickup.lng),
            GeoPoint(delivery.lat, delivery.lng)
        )
    }
    polyline.setPoints(points)
    if (!mapView.overlays.contains(polyline)) mapView.overlays.add(0, polyline)

    if (geometry.size >= 2) {
        val box = BoundingBox.fromGeoPointsSafe(points)
        // 80px padding keeps the route off the edges and below the bottom card
        mapView.zoomToBoundingBox(box, true, 80)
    } else {
        val centerLat = (pickup.lat + delivery.lat) / 2
        val centerLng = (pickup.lng + delivery.lng) / 2
        mapView.controller.animateTo(GeoPoint(centerLat, centerLng))
    }
    mapView.invalidate()
}
