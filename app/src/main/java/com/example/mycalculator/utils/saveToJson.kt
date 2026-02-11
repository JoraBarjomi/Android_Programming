package com.example.mycalculator.utils

import com.example.mycalculator.dataclass.DataLocation

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
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
        //Log.d(log_tag, "Путь к файлу: ${file.absolutePath}")

    } catch (e: Exception) {
        Log.e(log_tag, "Ошибка сохранения во внешнее хранилище: ${e.message}")
        e.printStackTrace()
    }
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