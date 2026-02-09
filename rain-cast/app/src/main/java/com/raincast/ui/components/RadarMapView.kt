package com.raincast.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.raincast.data.local.entities.RadarFrameEntity
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

@Composable
fun RadarMapView(
    latitude: Double,
    longitude: Double,
    currentFrame: RadarFrameEntity?,
    radarHost: String,
    radarOpacity: Float = 0.7f,
    onMapReady: ((MapView) -> Unit)? = null,
    onMapClick: ((Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(8.0)
                controller.setCenter(GeoPoint(latitude, longitude))
                minZoomLevel = 4.0
                maxZoomLevel = 13.0

                // Add user location marker
                val marker = Marker(this)
                marker.position = GeoPoint(latitude, longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.title = "My Location"
                overlays.add(marker)

                mapView = this
                onMapReady?.invoke(this)
            }
        },
        update = { map ->
            // Update center if location changed significantly
            val currentCenter = map.mapCenter
            val dist = GeoPoint(latitude, longitude).distanceToAsDouble(
                GeoPoint(currentCenter.latitude, currentCenter.longitude)
            )
            if (dist > 1000) {
                map.controller.animateTo(GeoPoint(latitude, longitude))
            }

            // Update location marker
            val markers = map.overlays.filterIsInstance<Marker>()
            markers.firstOrNull()?.position = GeoPoint(latitude, longitude)

            // Update radar overlay
            updateRadarOverlay(map, currentFrame, radarHost, radarOpacity)
        }
    )
}

private fun updateRadarOverlay(
    mapView: MapView,
    frame: RadarFrameEntity?,
    host: String,
    opacity: Float
) {
    // Remove existing radar overlays
    mapView.overlays.removeAll { it is TilesOverlay && it != mapView.overlayManager.tilesOverlay }

    if (frame == null) return

    val tileSource = object : OnlineTileSourceBase(
        "RainViewer",
        4, 7, 256, ".png",
        arrayOf("${frame.host}${frame.path}/256/")
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val zoom = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "${frame.host}${frame.path}/256/$zoom/$x/$y/2/1_0.png"
        }
    }

    val tileProvider = MapTileProviderBasic(mapView.context, tileSource)
    val radarOverlay = TilesOverlay(tileProvider, mapView.context)
    radarOverlay.loadingBackgroundColor = android.graphics.Color.TRANSPARENT
    radarOverlay.loadingLineColor = android.graphics.Color.TRANSPARENT
    radarOverlay.setColorFilter(null)

    // Set opacity via paint alpha
    val alpha = (opacity * 255).toInt()
    radarOverlay.setTransparency((255 - alpha) / 255f)

    mapView.overlays.add(1.coerceAtMost(mapView.overlays.size), radarOverlay)
    mapView.invalidate()
}
