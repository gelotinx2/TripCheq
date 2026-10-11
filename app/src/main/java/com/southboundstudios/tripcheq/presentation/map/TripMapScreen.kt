package com.southboundstudios.tripcheq.presentation.map

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.southboundstudios.tripcheq.BuildConfig.MAPTILER_KEY
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.utils.PolylineUtils

@Composable
fun TripMapScreen(
    encodedPolyline: String?,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(lifecycleOwner, mapViewInstance) {
        val mapView = mapViewInstance
        if (mapView == null) return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                onCreate(Bundle())
                getMapAsync { map ->
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(14.4250, 121.0000))
                        .zoom(12.0)
                        .build()

                    val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=$MAPTILER_KEY"

                    map.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
                        style.addSource(GeoJsonSource("route-source"))
                        style.addLayer(
                            LineLayer("route-layer", "route-source").withProperties(
                                lineColor("#3b82f6".toColorInt()),
                                lineWidth(6f),
                                lineCap("round"),
                                lineJoin("round")
                            )
                        )
                    }
                }
                mapViewInstance = this
            }
        },
        update = { mapView ->
            mapView.getMapAsync { map ->
                map.style?.let { style ->
                    val source = style.getSourceAs<GeoJsonSource>("route-source")

                    if (!encodedPolyline.isNullOrEmpty()) {
                        val points: List<Point> = parseGeometryPoints(encodedPolyline)

                        if (points.isNotEmpty()) {
                            val lineString = LineString.fromLngLats(points)
                            val feature = Feature.fromGeometry(lineString)
                            source?.setGeoJson(FeatureCollection.fromFeature(feature))

                            val boundsBuilder = LatLngBounds.Builder()
                            points.forEach { pt ->
                                boundsBuilder.include(LatLng(pt.latitude(), pt.longitude()))
                            }

                            map.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120),
                                1000
                            )
                        } else {
                            source?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
                        }
                    } else {
                        source?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
                    }
                }
            }
        }
    )
}

/**
 * Handles both Google-encoded polyline strings and PostGIS WKT (LINESTRING(lng lat, ...))
 */
private fun parseGeometryPoints(geometryStr: String): List<Point> {
    val trimmed = geometryStr.trim()
    return try {
        when {
            trimmed.startsWith("MULTILINESTRING", ignoreCase = true) -> {
                // Example: MULTILINESTRING((lng lat, lng lat), (lng lat, lng lat))
                val coordRegex = Regex("""(-?\d+\.?\d*)\s+(-?\d+\.?\d*)""")
                coordRegex.findAll(trimmed).map { match ->
                    val (lng, lat) = match.destructured
                    Point.fromLngLat(lng.toDouble(), lat.toDouble())
                }.toList()
            }
            trimmed.startsWith("LINESTRING", ignoreCase = true) -> {
                val content = trimmed.substringAfter("(").substringBefore(")")
                content.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { pair ->
                        val parts = pair.split("\\s+".toRegex())
                        Point.fromLngLat(parts[0].toDouble(), parts[1].toDouble())
                    }
            }
            else -> {
                PolylineUtils.decode(trimmed, 5)
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}
