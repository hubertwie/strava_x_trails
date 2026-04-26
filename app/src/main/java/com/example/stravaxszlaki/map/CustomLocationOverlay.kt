package com.example.stravaxszlaki.map

import android.content.Context
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class CustomLocationOverlay(
    val context: Context,
    provider: GpsMyLocationProvider,
    mapView: org.osmdroid.views.MapView,
    val onTrackingStateChanged: (Boolean) -> Unit
) : MyLocationNewOverlay(provider, mapView), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var currentAzimuth = 0f

    init {
        mCirclePaint.color = android.graphics.Color.argb(40, 33, 150, 243)
        mCirclePaint.style = android.graphics.Paint.Style.FILL
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)

        // ZABEZPIECZENIE PRZED "SKAKANIEM":
        // Ignorujemy odczyty z GPS o dokładności gorszej niż 50 metrów
        provider.locationUpdateMinDistance = 5f // Aktualizuj tylko jeśli przeszedłeś min 5m
        provider.locationUpdateMinTime = 1000L  // Aktualizuj max co sekunde
    }

    override fun enableMyLocation(): Boolean {
        val result = super.enableMyLocation()
        onTrackingStateChanged(true)
        return result
    }

    override fun disableMyLocation() {
        super.disableMyLocation()
        onTrackingStateChanged(false)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360f
            currentAzimuth = azimuth
            mMapView.postInvalidate()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun draw(c: Canvas, pProjection: org.osmdroid.views.Projection) {
        val location = myLocationProvider.lastKnownLocation
        if (location != null && location.accuracy < 50f) { // Rysuj tylko jeśli dokładność jest lepsza niż 50m
            location.bearing = currentAzimuth
            location.speed = 2f
        }
        super.draw(c, pProjection)
    }

    override fun onDetach(mapView: org.osmdroid.views.MapView?) {
        sensorManager.unregisterListener(this)
        super.onDetach(mapView)
    }
}