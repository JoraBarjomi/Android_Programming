package com.example.mycalculator.services

import com.example.mycalculator.utils.saveToJson
import com.example.mycalculator.utils.shareJson

import android.Manifest
import android.content.Intent
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.mycalculator.ui.Location
import com.example.mycalculator.dataclass.DataLocation
import com.example.mycalculator.utils.PermissionLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationUtilites (private val activity: Location){

    var log_tag = "MAIN_LOCATION"
    var isRunning = false
    private lateinit var permissionsRequest: PermissionLocation

    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest

    init {
        permissionsRequest = PermissionLocation(activity)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                try {
                    for (location in p0.locations) {
                        val newLocation = DataLocation(location.latitude, location.longitude,location.altitude, location.time)
                        activity.Latitude.text = "${newLocation.lat}"
                        activity.Longitude.text = "${newLocation.lon}"
                        activity.Altitude.text = "${newLocation.alt}"
                        activity.Time.text = "${newLocation.ms}"
                        saveToJson(activity, newLocation)
                        Log.e(log_tag, "Новая локация: $${newLocation.lat}, ${newLocation.lon}, ${newLocation.alt}")
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
            Log.e(log_tag, "Запустил сервис в фоне!")
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
            isRunning = false
            activity.ButtonStartService.isEnabled = true
            activity.ButtonStopService.isEnabled = false
        }
    }

}