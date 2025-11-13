package com.example.mycalculator

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileWriter

class LocationService : Service() {

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
        Log.d(log_tag, "Service создан")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val lat = location.latitude
                    val lon = location.longitude
                    val alt = location.altitude
                    val ms = location.time
                    val date = Date(ms)
                    val formatter = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
                    val time = formatter.format(date)
                    Log.d(log_tag, "Новая локация: $lat, $lon, $alt")
                    saveToJson(lat, lon, alt, time)
                    updateNotification(lat, lon, alt)
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(log_tag, "Service запущен")
        createNotificationChannel()
        val notification = createNotification(0.0, 0.0, 0.0)
        startForeground(NOTIFICATION_ID, notification)
        startLocationUpdates()
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
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun createNotification(lat: Double, lon: Double, alt: Double): Notification {
        val notificationIntent = Intent(this, Location::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = Notification.Builder(this, CHANNEL_ID)

        return builder.apply {
            setContentTitle("Координаты:")
            setContentText("Lat: %.4f, Lon: %.4f, Alt: %.1f м".format(lat, lon, alt))
            setSmallIcon(android.R.drawable.ic_menu_mylocation)
            setContentIntent(pendingIntent)
            setOngoing(true)
        }.build()
    }

    private fun updateNotification(lat: Double, lon: Double, alt: Double) {
        val notification = createNotification(lat, lon, alt)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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

    private fun saveToJson(lat: Double, lon: Double, alt: Double, time: String) {
        try {
            val papka = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!papka.exists()) {
                papka.mkdirs()
            }
            val file = File(papka, "locations.json")
            if (!file.exists()) {
                file.createNewFile()
                file.writeText("[]")
            }
            val jsonArray = JSONArray(file.readText())
            val locationObject = JSONObject().apply {
                put("latitude", lat)
                put("longitude", lon)
                put("altitude", alt)
                put("time", time)
            }
            jsonArray.put(locationObject)
            FileWriter(file).use {
                it.write(jsonArray.toString(4))
            }
            Log.d(log_tag, "Локация записана в JSON: $lat, $lon, $alt")
        } catch (e: Exception) {
            Log.e(log_tag, "Ошибка при записи в JSON: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(log_tag, "Service остановлен")
    }
}