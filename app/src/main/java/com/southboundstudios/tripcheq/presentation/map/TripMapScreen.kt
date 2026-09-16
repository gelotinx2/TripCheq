package com.southboundstudios.tripcheq.presentation.map

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import androidx.core.graphics.toColorInt
import com.southboundstudios.tripcheq.BuildConfig.MAPTILER_KEY

@Composable
fun TripMapScreen(
    encodedPolyline: String?,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

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

                    val mapTilerKey = MAPTILER_KEY
                    val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=$mapTilerKey"

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
            }
        },
        update = { mapView ->
            mapView.getMapAsync { map ->
                map.style?.let { style ->
                    val source = style.getSourceAs<GeoJsonSource>("route-source")

                    if (!encodedPolyline.isNullOrEmpty()) {
                        // 1. Decode the string (precision 5 is standard for Google Routes)
                        val points: List<Point> = PolylineUtils.decode(encodedPolyline, 5)

                        // 2. Convert to GeoJSON Feature
                        val lineString = LineString.fromLngLats(points)
                        val feature = Feature.fromGeometry(lineString)

                        // 3. Update the Map Source
                        source?.setGeoJson(FeatureCollection.fromFeature(feature))

                        // 4. Animate camera to fit the new route
                        if (points.isNotEmpty()) {
                            val bounds = LatLngBounds.Builder()
                                .includes(points.map { LatLng(it.latitude(), it.longitude()) })
                                .build()

                            map.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(bounds, 100),
                                1000
                            )
                        }
                    } else {
                        source?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
                    }
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