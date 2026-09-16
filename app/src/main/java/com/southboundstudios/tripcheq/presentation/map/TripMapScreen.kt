package com.southboundstudios.tripcheq.presentation.map

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import androidx.core.graphics.toColorInt

@Composable
fun TripMapScreen(
    encodedPolyline: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) { MapLibre.getInstance(context) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                onCreate(Bundle())
                getMapAsync { map ->
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(14.4250, 121.0000))
                        .zoom(14.0)
                        .build()

                    map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
                        style.addSource(GeoJsonSource("route-source"))
                        style.addLayer(
                            LineLayer("route-layer", "route-source").withProperties(
                                lineColor("#3b82f6".toColorInt()),
                                lineWidth(5f),
                                lineCap("round"),
                                lineJoin("round"),
                            )
                        )
                    }
                }
            }
        },
        update = { mapView ->
            mapView.getMapAsync { map ->
                map.style?.let { style ->
                    val source = style.getSourceAs<GeoJsonSource>("route-source")
                    // Will need Mapbox/Maplibre PolylineUtils to decode and build a Feature here
                }
            }

            lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            })
        }
    )
}