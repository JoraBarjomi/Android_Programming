package com.example.mycalculator.utils

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mycalculator.ui.Location
import com.example.mycalculator.ui.Mediaplayer

class PermissionLocation(private val activity: AppCompatActivity) {
    val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val finePermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarsePermission = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val phoneStatePermission = permissions[Manifest.permission.READ_PHONE_STATE] ?: false

        if (finePermission && coarsePermission && phoneStatePermission) {
            Toast.makeText(activity, "Локация получена", Toast.LENGTH_SHORT).show()
            backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    val backgroundLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(activity, "Фоновая локация получена", Toast.LENGTH_SHORT).show()
        }
        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    val notifLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(activity, "Уведомления получены", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAllPermissons(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PERMISSION_GRANTED
    }

    fun givePermissons(){
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE

            )
        )
    }
}

class PermissionMediaplayer(private val activity: Mediaplayer) {

    val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val storagePermission = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        val audioPermission = permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: false

        if (storagePermission && audioPermission) {
            Toast.makeText(activity, "Локация получена", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAllPermissons(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) == PERMISSION_GRANTED
    }

    fun givePermissons(){
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        )
    }
}