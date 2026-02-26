package com.example.mycalculator.utils

import android.R
import com.example.mycalculator.dataclass.DataLocation
import com.example.mycalculator.dataclass.CellInfoLteData
import com.example.mycalculator.dataclass.CellInfoGSMData

import android.telephony.CellInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val log_tag = "SAVEFUNC"
fun saveToJson(context: Context, newLocation: DataLocation) {
    try {
        val file = File(context.filesDir, "locations.json")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("[]")
        }

        val date = Date(newLocation.ms)
        val formatter = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
        val datetime = formatter.format(date)

        val jsonArray = JSONArray(file.readText())
        val locationObject = JSONObject().apply {
            put("latitude", newLocation.lat)
            put("longitude", newLocation.lon)
            put("altitude", newLocation.alt)
            put("timeMS", newLocation.ms)
            put("date", datetime)
        }
        jsonArray.put(locationObject)
        FileWriter(file).use {
            it.write(jsonArray.toString(4))
        }
        Log.d(log_tag, "Локация записана в JSON: ${newLocation.lat}, ${newLocation.lon}, ${newLocation.alt}")

    } catch (e: Exception) {
        Log.e(log_tag, "Ошибка сохранения во внешнее хранилище: ${e.message}")
        e.printStackTrace()
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun convertToJson(context: Context, newLocation: DataLocation, imei: String?, cell: List<CellInfo>?): String {
    val date = Date(newLocation.ms)
    val formatter = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
    val datetime = formatter.format(date)

    var lteJsonArray = JSONArray()
    var gsmJsonArray = JSONArray()
    var isReg: Boolean = false
    var cidIsReg: Int = 0

    if (cell != null) {
        for (it in cell) {
                if (it is CellInfoLte) {
                   val cellObject = JSONObject().apply {
                       if(it.isRegistered) {
                           isReg = it.isRegistered
                           cidIsReg = it.cellIdentity.ci
                       }
                        put("ci", it.cellIdentity.ci)
                        put("pci", it.cellIdentity.pci)
                        put("bandwidth", it.cellIdentity.bandwidth)
                        put("earfcn", it.cellIdentity.earfcn)
                        put("mcc", it.cellIdentity.mccString ?: "")
                        put("mnc", it.cellIdentity.mncString ?: "")
                        put("tac", it.cellIdentity.tac)
                        put("asuLevel", it.cellSignalStrength.asuLevel)
                        put("cqi", it.cellSignalStrength.cqi)
                        put("rsrp", it.cellSignalStrength.rsrp)
                        put("rsrq", it.cellSignalStrength.rsrq)
                        put("rssi", it.cellSignalStrength.rssi)
                        put("rssnr", it.cellSignalStrength.rssnr)
                        put("dbm", it.cellSignalStrength.dbm)
                        put("timingAdvance", it.cellSignalStrength.timingAdvance)
                    }
                    lteJsonArray.put(cellObject)
                }
                if (it is CellInfoGsm) {
                    val cellObject = JSONObject().apply {
                        put("cid", it.cellIdentity.cid)
                        put("bsic", it.cellIdentity.bsic)
                        put("arfcn", it.cellIdentity.arfcn)
                        put("lac", it.cellIdentity.lac)
                        put("mccString", it.cellIdentity.mccString)
                        put("mncString", it.cellIdentity.mncString)
                        put("psc", it.cellIdentity.psc.toString())
                        put("dbm", it.cellSignalStrength.dbm)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            put("rssi", it.cellSignalStrength.rssi)
                        }
                        put("timingAdvance", it.cellSignalStrength.timingAdvance)
                    }
                    gsmJsonArray.put(cellObject)
                }
        }
    }

    val locationObject = JSONObject().apply {
        put("imei", imei)
        put("latitude", newLocation.lat)
        put("longitude", newLocation.lon)
        put("altitude", newLocation.alt)
        put("accuracy", newLocation.accuracy)
        put("timeMS", newLocation.ms)
        put("cidIsReg", cidIsReg)
        put("IsReg", isReg)
        put("date", datetime)
    }

    val finalJson = JSONObject().apply {
        put("locationInfo", locationObject)
        put("cellGSM", gsmJsonArray)
        put("cellLte", lteJsonArray)
    }

    Log.d(log_tag, "Локация записана в JSON: ${newLocation.lat}, ${newLocation.lon}, ${newLocation.alt}")
    return finalJson.toString()
}

fun shareJson(context: Context){

    val file = File(context.filesDir, "locations.json")
    var uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "application/json"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Получить json"))

}

fun shareJsonServer(context: Context): String{

    val file = File(context.filesDir, "locations.json")
    val data = file.readText()
    return data

}