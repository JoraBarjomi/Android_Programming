package com.example.mycalculator.utils

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

    var lte = mutableListOf<CellInfoLteData>()
    var gsm = mutableListOf<CellInfoGSMData>()

    if (cell != null) {
        for (it in cell) {
            when (it) {
                is CellInfoLte -> (
                    lte.add(CellInfoLteData(
                        it.cellIdentity.ci,
                        it.cellIdentity.pci,
                        it.isRegistered,
                        it.cellIdentity.bandwidth,
                        it.cellIdentity.earfcn,
                        it.cellIdentity.mccString,
                        it.cellIdentity.mncString,
                        it.cellIdentity.tac,
                        it.cellSignalStrength.asuLevel,
                        it.cellSignalStrength.cqi,
                        it.cellSignalStrength.rsrp,
                        it.cellSignalStrength.rsrq,
                        it.cellSignalStrength.rssi,
                        it.cellSignalStrength.rssnr,
                        it.cellSignalStrength.dbm,
                        it.cellSignalStrength.timingAdvance))
                )
                is CellInfoGsm -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    gsm.add(CellInfoGSMData(
                        it.cellIdentity.cid,
                        it.cellIdentity.bsic,
                        it.cellIdentity.arfcn,
                        it.cellIdentity.lac,
                        it.cellIdentity.mccString,
                        it.cellIdentity.mncString,
                        it.cellIdentity.psc.toString(),
                        it.cellSignalStrength.dbm,
                        it.cellSignalStrength.rssi,
                        it.cellSignalStrength.timingAdvance))
                }
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
        put("date", datetime)
        put("cellGSM", gsm)
        put("cellLte", lte)
    }

    Log.d(log_tag, "Локация записана в JSON: ${newLocation.lat}, ${newLocation.lon}, ${newLocation.alt}")
    return locationObject.toString()
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