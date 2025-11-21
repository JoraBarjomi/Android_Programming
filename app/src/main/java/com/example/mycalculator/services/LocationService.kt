package com.example.mycalculator.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.mycalculator.R
import com.example.mycalculator.ui.Location
import com.example.mycalculator.dataclass.DataLocation
import com.example.mycalculator.utils.saveToJson
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService: Service() {

    companion object {
        private const val CHANNEL_ID = "location_channel"
        private const val NOTIFICATION_ID = 1
    }

    private val log_tag = "LOCATION_SERVICE"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(log_tag, "Проверка всех разрешений service")
        if (checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission( Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission( Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ){
            Toast.makeText(this, "Сервис имеет разрешения!", Toast.LENGTH_SHORT).show()
        }

        Log.d(log_tag, "Service создан")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                try {
                    result.lastLocation?.let { location ->
                        val newLocation = DataLocation(
                            location.latitude,
                            location.longitude,
                            location.altitude,
                            location.time
                        )
                        Log.d(log_tag, "Новая локация: ${newLocation.lat}, ${newLocation.lon}, ${newLocation.alt}")
                        saveToJson(this@LocationService, newLocation)
                        updateNotification(newLocation.lat, newLocation.lon, newLocation.alt)
                    }
                } catch (e: Exception) {
                    Log.e(log_tag, "Ошибка обработки локации: ${e.message}")
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.POST_NOTIFICATIONS])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(log_tag, "Service запущен")
        createNotificationChannel()
        val notification = createNotification(0.0, 0.0, 0.0)
        startForeground(NOTIFICATION_ID, notification)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED){
            startLocationUpdates()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Отслеживание местоположения",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Фоновое отслеживание координат"
        }
        val manager = this.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun createNotification(lat: Double, lon: Double, alt: Double): Notification {
        val notificationIntent = Intent(this, Location::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)

        return builder.apply {
            setContentTitle("Координаты:")
            setContentText("Lat: %.4f, Lon: %.4f, Alt: %.2f м".format(lat, lon, alt))
            setContentIntent(pendingIntent)
            setOngoing(true)
        }.build()
    }

    private fun updateNotification(lat: Double, lon: Double, alt: Double) {
        val notification = createNotification(lat, lon, alt)
        val manager = this.getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.POST_NOTIFICATIONS])
    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(log_tag, "Обновления локации запущены")
        } catch (e: SecurityException) {
            Log.e(log_tag, "Нет разрешения на доступ к локации: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(log_tag, "Service остановлен")
    }
}