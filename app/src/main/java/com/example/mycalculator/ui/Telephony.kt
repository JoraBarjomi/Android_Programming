package com.example.mycalculator.ui

import android.Manifest
import android.content.Context
import android.telephony.TelephonyManager
import com.example.mycalculator.utils.PermissionLocation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mycalculator.R
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission

class Telephony : AppCompatActivity() {

    private lateinit var permissionsRequest: PermissionLocation
    lateinit var Latitude: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_telephony)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Latitude = findViewById(R.id.latitude)

        permissionsRequest = PermissionLocation(this)
        permissionsRequest.givePermissons()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onResume() {
        super.onResume()

        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (permissionsRequest.checkAllPermissons()){
            val cellInfoList = telephonyManager.allCellInfo
            Latitude.text = "$cellInfoList"
        }
    }
}