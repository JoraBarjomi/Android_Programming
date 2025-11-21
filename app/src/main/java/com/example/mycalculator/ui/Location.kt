package com.example.mycalculator.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mycalculator.R
import com.example.mycalculator.services.LocationUtilites
import com.example.mycalculator.utils.PermissionLocation
import com.example.mycalculator.utils.shareJson
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.example.mycalculator.BuildConfig
import com.yandex.mapkit.MapKit
import com.yandex.runtime.image.ImageProvider

class Location : AppCompatActivity() {
    private var log_tag = "MAIN_LOCATION"

    private lateinit var permissionsRequest: PermissionLocation
    private lateinit var locationUtils: LocationUtilites

    lateinit var Longitude: TextView
    lateinit var Latitude: TextView
    lateinit var Altitude: TextView
    lateinit var Time: TextView
    lateinit var ButtonStartService: Button
    lateinit var ButtonStopService: Button
    lateinit var ButtonShareJson: Button
    lateinit var Map: com.yandex.mapkit.mapview.MapView

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)

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
        ButtonShareJson = findViewById(R.id.btnShareJson)
        Map = findViewById(R.id.mapview)

        Log.e(log_tag, "Запрашиваю разрешения!")
        permissionsRequest = PermissionLocation(this)
        permissionsRequest.givePermissons()
    }

    override fun onStart() {
        super.onStart()
        Map.onStart()
        MapKitFactory.getInstance().onStart()

        locationUtils = LocationUtilites(this, Map)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.POST_NOTIFICATIONS])
    override fun onResume() {
        super.onResume()
        ButtonStartService.setOnClickListener {
            if (permissionsRequest.checkAllPermissons()){
                Log.e(log_tag, "Запускаю LocationUpdates")
                locationUtils.startLocationUpdates()
                locationUtils.startBackgroundService()
                ButtonShareJson.isEnabled = true
            }
        }
        ButtonStopService.setOnClickListener {
            locationUtils.stopBackgroundService()
        }
        ButtonShareJson.setOnClickListener {
            shareJson(this@Location)
        }
    }

    override fun onPause() {
        super.onPause()
        locationUtils.stopLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
    }
}