package com.example.mycalculator.services

import android.provider.Settings
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import android.os.*
import androidx.annotation.RequiresApi
import com.example.mycalculator.R
import com.example.mycalculator.ui.Location
import com.example.mycalculator.dataclass.DataLocation
import com.example.mycalculator.utils.PermissionLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.MapKit
import com.yandex.runtime.image.ImageProvider
import com.example.mycalculator.utils.ClientZMQ
import com.example.mycalculator.utils.convertToJson

class LocationUtilites (private val activity: Location, private val map: com.yandex.mapkit.mapview.MapView){

    var log_tag = "MAIN_LOCATION"
    var isRunning = false
    private lateinit var permissionsRequest: PermissionLocation

    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest
    private var placemark: com.yandex.mapkit.map.PlacemarkMapObject? = null
    lateinit var Client: ClientZMQ
    var isConnected = false
    lateinit var imageProvider: ImageProvider
    private lateinit var telephonyManager: TelephonyManager

//    var addr = "tcp://192.168.0.130:12345"
//    var addr = "tcp://172.20.10.8:12345"
    var addr = "tcp://"

    init {
        permissionsRequest = PermissionLocation(activity)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        imageProvider = ImageProvider.fromResource(activity, R.drawable.ic_location_notification3)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L)
            .build()

        locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.Q)
            @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                try {
                    for (location in p0.locations) {
                        val newLocation = DataLocation(location.latitude, location.longitude,location.altitude, location.time, location.accuracy)
                        activity.Latitude.text = "${newLocation.lat}"
                        activity.Longitude.text = "${newLocation.lon}"
                        activity.Altitude.text = "${newLocation.alt}"
                        activity.Time.text = "${newLocation.ms}"

                        if (isConnected) {
                            telephonyManager = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                            val cell = try {
                                telephonyManager.allCellInfo
                            } catch (e: SecurityException) {
                                emptyList()
                            }

                            val imei = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
                            var dataJson = convertToJson(activity, newLocation, imei, cell)
                            Log.d(log_tag, "Sending data: $dataJson")
                            var replyFromServer = Client.SendData(dataJson)
                            Log.d(log_tag, "Reply from server: $replyFromServer")
                        } else {
                            Log.d(log_tag, "Client is not connected: $isConnected")
                        }

                        Log.e(log_tag, "New location: ${newLocation.lat}, ${newLocation.lon}, ${newLocation.alt}")

                        if (placemark == null) {
                            placemark = map.map.mapObjects.addPlacemark(Point(newLocation.lat, newLocation.lon)).apply {
                                setIcon(imageProvider)
                                opacity = 1f
                            }
                        } else {
                            placemark?.geometry = Point(newLocation.lat, newLocation.lon)
                        }
                        map.map.move(
                            CameraPosition(
                                Point(newLocation.lat, newLocation.lon),
                                /* zoom = */ 17.0f,
                                /* azimuth = */ 150.0f,
                                /* tilt = */ 30.0f
                            ), Animation(Animation.Type.SMOOTH, 3f), null
                        )
                    }
                } catch (e: Exception) {
                    Log.e(log_tag, "Ошибка обработки локации: ${e.message}")
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.POST_NOTIFICATIONS])
    fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            if (addr != "tcp://"){
                Client = ClientZMQ(addr)
            }
            Log.d(log_tag, "Обновления локации запущены")
        } catch (e: SecurityException) {
            Log.e(log_tag, "Нет разрешения на доступ к локации: ${e.message}")
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.POST_NOTIFICATIONS])
    fun startBackgroundService(){
        if (!isRunning){
            if(permissionsRequest.checkAllPermissons()){
                val serviceIntent = Intent(activity, LocationService::class.java)
                activity.startForegroundService(serviceIntent)
            }
            Log.d(log_tag, "Запустил сервис в фоне!")
            isConnected = Client.SetConnection()
            isRunning = true
            activity.ButtonStartService.isEnabled = false
            activity.ButtonStopService.isEnabled = true
        }
    }

    fun stopBackgroundService(){
        if (isRunning){
            val serviceIntent = Intent(activity, LocationService::class.java)
            activity.stopService(serviceIntent)
            Log.e(log_tag, "Остановил сервис в фоне!")
            Client.CloseConnection()
            Log.d(log_tag, "Close connection: $isConnected")
            isRunning = false
            activity.ButtonStartService.isEnabled = true
            activity.ButtonStopService.isEnabled = false
        }
    }

    fun setIpAddress(newaddr : String) {
        if (addr == "tcp://") {
            addr += newaddr
        } else {
            addr = "tcp://" + newaddr
        }
    }

}