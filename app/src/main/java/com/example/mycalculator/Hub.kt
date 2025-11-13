package com.example.mycalculator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mycalculator.databinding.ActivityHubBinding


class Hub : AppCompatActivity() {

    private lateinit var binding: ActivityHubBinding

    private lateinit var textHub: TextView
    private lateinit var btncalc: Button
    private lateinit var btnmedia: Button
    private lateinit var btnlocation: Button
    private lateinit var btn4: Button
    private lateinit var btn5: Button
    private lateinit var btn6: Button
    private lateinit var btn7: Button
    private lateinit var btn8: Button
    private lateinit var themebtn: SeekBar
    private lateinit var chkBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        textHub = findViewById<TextView>(R.id.hub)
        btncalc = findViewById<Button>(R.id.calculator)
        btnmedia = findViewById<Button>(R.id.mediaplayer)
        btnlocation = findViewById<Button>(R.id.location)
        btn4 = findViewById<Button>(R.id.btn4)
        btn5 = findViewById<Button>(R.id.btn5)
        btn6 = findViewById<Button>(R.id.btn6)
        btn7 = findViewById<Button>(R.id.btn7)
        btn8 = findViewById<Button>(R.id.btn8)
        themebtn = findViewById<SeekBar>(R.id.seekBar)
        chkBox = findViewById<CheckBox>(R.id.chkBox)
    }

    override fun onResume() {
        super.onResume()
        binding.calculator.setOnClickListener({
            val randomIntent = Intent(this, MainActivity::class.java)
            startActivity(randomIntent)
        });
        binding.mediaplayer.setOnClickListener({
            val randomIntent = Intent(this, Mediaplayer::class.java)
            startActivity(randomIntent)
        });
        binding.location.setOnClickListener({
           val randomIntent = Intent(this, Location::class.java)
           startActivity(randomIntent)
        });

        themebtn.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                btncalc.setBackgroundColor(rndColor)
                rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                btnmedia.setBackgroundColor(rndColor)
                rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                btnlocation.setBackgroundColor(rndColor)
                rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                btn4.setBackgroundColor(rndColor)
                rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                btn5.setBackgroundColor(rndColor)
                rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                btn6.setBackgroundColor(rndColor)
                rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                btn7.setBackgroundColor(rndColor)
                rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                btn8.setBackgroundColor(rndColor)
                rndColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                textHub.setTextColor(rndColor)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })
        chkBox.setOnClickListener {
            if (chkBox.isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        btn8.setOnClickListener {
            val url = "https://ru.wikipedia.org/wiki/%D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F:%D0%A1%D0%BB%D1%83%D1%87%D0%B0%D0%B9%D0%BD%D0%B0%D1%8F_%D1%81%D1%82%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D0%B0"
            val browerIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
            startActivity(browerIntent)
        }
    }
}