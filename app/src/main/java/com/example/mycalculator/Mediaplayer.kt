package com.example.mycalculator

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.media.MediaMetadataRetriever

import java.io.File
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.media.MediaPlayer
import android.widget.Toast

import android.net.Uri

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.ImageButton
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class Mediaplayer : AppCompatActivity() {

    private var log_tag : String = "GOSHA_LOG_TAG"

    private var mediaPlayer: MediaPlayer? = null
    private var musicPaths: List<String> = emptyList()
    private var currentTrackIndex: Int = 0
    private var currentMusicPath: String? = null
    private var metadataRetriever = MediaMetadataRetriever()

    private var isPlaying = false
    private var isLiked = false
    private var isNext = false
    private var isPrev = false

    private lateinit var btnplay: ImageButton
    private lateinit var btnnext: ImageButton
    private lateinit var btnprev: ImageButton
    private lateinit var btnlike: ImageButton
    private lateinit var btnunlike: ImageButton
    private lateinit var nameSong: TextView
    private lateinit var authorSong: TextView
    private lateinit var imageSong: ImageView
    private lateinit var curTimeSong: TextView
    private lateinit var maxTimeSong: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mediaplayer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nameSong = findViewById(R.id.nameSong)
        authorSong = findViewById(R.id.authorSong)
        imageSong = findViewById(R.id.songImage)
        curTimeSong = findViewById(R.id.currTime)
        maxTimeSong = findViewById(R.id.maxTime)

        btnplay = findViewById(R.id.btnplay)
        btnnext = findViewById(R.id.btnnext)
        btnprev = findViewById(R.id.btnprev)
        btnlike = findViewById(R.id.like)
        btnunlike = findViewById(R.id.unlike)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted){
                findMusicFile()
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            } else{
                //Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
            }
        }
        requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        requestPermissionLauncher.launch(READ_MEDIA_AUDIO )

    }

    private fun findMusicFile(){

        val musicPath: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path

        val directory : File = File(musicPath)

        if(directory.isDirectory){
            val musicFiles = directory.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".mp3", ignoreCase = true) }
                ?: emptyList()

            if(musicFiles.isNotEmpty()){
                musicPaths = musicFiles.map{ it.absolutePath }
                currentTrackIndex = 0
                musicPaths.forEach {
                    Log.d(log_tag, "Найден файл: $it")
                }
                currentMusicPath = musicPaths[currentTrackIndex]
                Log.d(log_tag, "Взял первую песню: $currentMusicPath")
            } else{
                //Toast.makeText(this, "Аудиофайл MP3 не найден в папке Music.", Toast.LENGTH_LONG).show()
                Log.d(log_tag, "Аудиофайл MP3 не найден")
            }
        } else{
            Log.d(log_tag, "Папка $musicPath не найдена или это не папка!")
        }
    }

    fun getTrackInfo(uri: Uri): Triple<String?, String?, android.graphics.Bitmap?> {
        metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(applicationContext, uri)

        val name = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val imageBytes = metadataRetriever.embeddedPicture
        val bitmap = imageBytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) }

        return Triple(name, artist, bitmap)
    }
    fun getTrackDuration(uri: Uri): String? {
        metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(applicationContext, uri)

        val durationStr = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toIntOrNull() ?: 0

        val totalSec = durationMs / 1000;
        val min = totalSec / 60
        val sec = totalSec % 60

        val time =  String.format("%02d:%02d", min, sec)

        return time
    }

    fun getTrackTime(uri: Uri): String? {

        val totalMs = mediaPlayer?.currentPosition ?: 0
        val sec = totalMs / 1000
        val min = sec / 60
        val restSec = sec % 60
        val time =  String.format("%02d:%02d", min, restSec)

        return time
    }

    //TODO seekbarVolume
    //TODO seekbarMusic

    private var timeJob: Job? = null
    private fun playSong(index: Int){
        if(musicPaths.isEmpty()) return

        currentTrackIndex = index.coerceIn(0, musicPaths.size - 1)
        currentMusicPath = musicPaths[currentTrackIndex]

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        timeJob?.cancel()

        if(mediaPlayer == null){
            try {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(currentMusicPath)
                mediaPlayer?.prepare()

                val uriCurMusicPath = Uri.fromFile(File(currentMusicPath))
                val songInfo = getTrackInfo(uriCurMusicPath)
                nameSong.text = songInfo.first
                authorSong.text = songInfo.second
                if (songInfo.second != null){
                    imageSong.setImageBitmap(songInfo.third)
                }
                maxTimeSong.text = getTrackDuration(uriCurMusicPath)

                mediaPlayer?.start()
                timeJob = lifecycleScope.launch {
                    while (isActive && mediaPlayer != null && mediaPlayer!!.isPlaying){
                        curTimeSong.text = getTrackTime(uriCurMusicPath)
                        delay(1000)
                    }
                }
                isPlaying = true
                btnplay.setImageResource(R.drawable.pause_btn)
                //Toast.makeText(this, "НАЧАЛО ВОСПРОИЗВЕДЕНИЯ", Toast.LENGTH_LONG).show()
                Log.d(log_tag, "Плеер запущен")
                mediaPlayer?.setOnCompletionListener{
                    playSong((currentTrackIndex + 1) % musicPaths.size)
                    Log.d(log_tag, "Песня кончилась")
                }

            } catch (e : Exception){
                Log.d(log_tag, "Ошибка воспроизведения ${e.message}!")
                Toast.makeText(this, "Ошбика воспоизведения ", Toast.LENGTH_LONG).show()
                mediaPlayer = null
                isPlaying = false
                timeJob?.cancel()
                btnplay.setImageResource(R.drawable.play_btn)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        btnplay.setOnClickListener {
            if(mediaPlayer == null){
                playSong(currentTrackIndex)
            } else if( mediaPlayer?.isPlaying == true){
                mediaPlayer?.pause()
                isPlaying = false
                btnplay.setImageResource(R.drawable.play_btn)
                Log.d(log_tag, "PAUSA")
            } else{
                mediaPlayer?.start()
                isPlaying = true
                btnplay.setImageResource(R.drawable.pause_btn)
                Log.d(log_tag, "Продолжение воспроизвдеения")
            }

            val new_icon = if(isPlaying){
                R.drawable.pause_btn
            } else{
                R.drawable.play_btn
            }
            btnplay.setImageResource(new_icon)

        }
        btnlike.setOnClickListener {
            isLiked = !isLiked
            val new_icon = if(isLiked){
                R.drawable.heart
            } else{
                R.drawable.heart_empty
            }
            btnlike.setImageResource(new_icon)
        }
        btnnext.setOnClickListener {
            isNext = !isNext
            val new_icon = if(isNext){
                R.drawable.control_btn_disable
            } else {
                R.drawable.control_btn_disable
            }
            btnnext.setImageResource(new_icon)
            if(isNext){
                if (musicPaths.isNotEmpty()){
                    val nextInd = (currentTrackIndex + 1) % musicPaths.size
                    playSong(nextInd)
                }
                btnnext.postDelayed({
                   isNext = false
                   btnnext.setImageResource(R.drawable.control_btn)
                }, 500);
            }
        }
        btnprev.setOnClickListener {
            isPrev = !isPrev
            val new_icon = if(isPrev){
                R.drawable.control_btn_disable

            } else {
                R.drawable.control_btn_disable
            }
            btnprev.setImageResource(new_icon)
            btnprev.setImageResource(new_icon)
            if(isPrev){
                if (musicPaths.isNotEmpty()){
                    val prevInd = if (currentTrackIndex - 1 < 0) musicPaths.size - 1
                    else currentTrackIndex - 1
                    playSong(prevInd)
                }
                btnnext.postDelayed({
                    isPrev = false
                    btnprev.setImageResource(R.drawable.control_btn)
                }, 500);
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        timeJob?.cancel()
        Toast.makeText(this, "Плеер остановлен!", Toast.LENGTH_LONG).show()
    }
    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        timeJob?.cancel()
        Toast.makeText(this, "Плеер выключен!", Toast.LENGTH_LONG).show()
    }
}