package com.example.mycalculator

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import java.util.*
import java.io.File
import java.io.FileWriter
import org.json.JSONArray
import org.json.JSONObject
import android.os.Looper
import android.os.Environment
import android.util.Log
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationResult
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Locale

class Location : AppCompatActivity() {
    var isRunning = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private lateinit var Longitude: TextView
    private lateinit var Latitude: TextView
    private lateinit var Altitude: TextView
    private lateinit var Time: TextView
    private lateinit var ButtonStartService: Button
    private lateinit var ButtonStopService: Button

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val finePermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarsePermission = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (finePermission && coarsePermission) {
            Toast.makeText(this, "Основные разрешения получены", Toast.LENGTH_SHORT).show()
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            Toast.makeText(this, "Нужны разрешения на местоположение!", Toast.LENGTH_LONG).show()
        }
    }

    private val backgroundLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(this, "Фоновая локация получена", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Разреши фоновое отслеживание", Toast.LENGTH_LONG).show()
        }
        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private val notifLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(this, "Уведомления получены", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Разреши уведомления", Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(this, "Все разрешения настроены!", Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Longitude = findViewById(R.id.longitude)
        Latitude = findViewById(R.id.latitude)
        Altitude = findViewById(R.id.altitude)
        Time = findViewById(R.id.clock)
        ButtonStartService = findViewById(R.id.btnStartService)
        ButtonStopService = findViewById(R.id.btnStopService)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for (location in p0.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val alt = location.altitude
                    val ms = location.time
                    val date = Date(ms)
                    val formatter = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
                    val time = formatter.format(date)
                    Latitude.text = "$lat"
                    Longitude.text = "$lon"
                    Altitude.text = "$alt"
                    Time.text = "$time"
                    saveToJson(lat, lon, alt, time)
                }
            }
        }
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startBackgroundService(){
        if (!isRunning){
            val serviceIntent = Intent(this, LocationService::class.java)
            startForegroundService(serviceIntent)
            Toast.makeText(this, "Process in background", Toast.LENGTH_SHORT).show()
            isRunning = true
            ButtonStartService.isEnabled = false
            ButtonStopService.isEnabled = true
        }
    }

    private fun stopBackgroundService(){
        if (isRunning){
            val serviceIntent = Intent(this, LocationService::class.java)
            stopService(serviceIntent)
            Toast.makeText(this, "Process is stopped", Toast.LENGTH_SHORT).show()
            isRunning = false
            ButtonStartService.isEnabled = true
            ButtonStopService.isEnabled = false
        }
    }

    private fun saveToJson(lat: Double, lon: Double, alt: Double, time: String) {
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
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        ButtonStartService.setOnClickListener {
            startBackgroundService()
        }
        ButtonStopService.setOnClickListener {
            stopBackgroundService()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }
}