package com.example.mycalculator.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mycalculator.R
import com.example.mycalculator.services.LocationUtilites
import com.example.mycalculator.utils.PermissionLocation
import com.example.mycalculator.utils.ClientZMQ
import com.yandex.mapkit.MapKitFactory
import com.example.mycalculator.BuildConfig
import com.example.mycalculator.utils.saveToJson
import com.example.mycalculator.utils.shareJson
import com.example.mycalculator.services.LocationService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Location : AppCompatActivity() {
    private var log_tag = "MAIN_LOCATION"

    private lateinit var permissionsRequest: PermissionLocation
    private lateinit var locationUtils: LocationUtilites

    lateinit var ConnectionStatus: TextView
    lateinit var Longitude: TextView
    lateinit var Latitude: TextView
    lateinit var Altitude: TextView
    lateinit var Time: TextView
    lateinit var ButtonStartService: Button
    lateinit var ButtonStopService: Button
    lateinit var ButtonShareJson: Button
    lateinit var FrameIpAddr: EditText
    lateinit var ButtonSaveIp: Button
    lateinit var Footer: FrameLayout
    lateinit var Map: com.yandex.mapkit.mapview.MapView

    private var locationService: LocationService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }
    }
    fun bindService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_location)
        bindService(this)

        Footer = findViewById(R.id.bottom_sheet)
        BottomSheetBehavior.from(Footer).apply {
            peekHeight=350
            this.state= BottomSheetBehavior.STATE_COLLAPSED
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ConnectionStatus = findViewById(R.id.serverStatus)
        Longitude = findViewById(R.id.longitude)
        Latitude = findViewById(R.id.latitude)
        Altitude = findViewById(R.id.altitude)
        Time = findViewById(R.id.clock)
        ButtonStartService = findViewById(R.id.btnStartService)
        ButtonStopService = findViewById(R.id.btnStopService)
        FrameIpAddr = findViewById(R.id.frameIpAddr)
        ButtonSaveIp = findViewById(R.id.btnSaveIp)
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
        val intent = Intent(this, LocationService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        ButtonStartService.isEnabled = false
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.POST_NOTIFICATIONS])
    override fun onResume() {
        super.onResume()

        ButtonStartService.setOnClickListener {
            if (permissionsRequest.checkAllPermissons()){
                Log.e(log_tag, "Запускаю LocationUpdates")
                locationUtils.startLocationUpdates()
                locationUtils.startBackgroundService()
                ConnectionStatus.text = "Connected"
                ConnectionStatus.setTextColor(Color.GREEN)
            }
        }
        ButtonStopService.setOnClickListener {
            locationUtils.stopBackgroundService()
            if (bound) {
                ConnectionStatus.text = locationService?.replyFromServer
            }
            ConnectionStatus.setTextColor(Color.RED)
        }

        ButtonSaveIp.setOnClickListener {
            ButtonStartService.isEnabled = true
            var addr = FrameIpAddr.text.toString()
            if (bound) {
                locationService?.setIpAddress(addr)
            }
        }
        ButtonShareJson.setOnClickListener {
            shareJson(this)
            if (bound) {
                ConnectionStatus.text = locationService?.replyFromServer
                ConnectionStatus.setTextColor(Color.GREEN)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Map.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStop() {
        super.onStop()
        locationUtils.stopBackgroundService()
        locationUtils.stopLocationUpdates()
    }
}