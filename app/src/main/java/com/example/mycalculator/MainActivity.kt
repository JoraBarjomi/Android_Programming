package com.example.mycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import com.example.mycalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var line = "0"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.one.setOnClickListener {
            if(line == "0"){
                line = "1"
            }
            else{
                if(line.length < 15){
                    line+="1"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = calculate(line)
        }
        binding.two.setOnClickListener {
            if(line == "0"){
                line = "2"
            }
            else{
                if(line.length < 15){
                    line+="2"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = calculate(line)
        }
        binding.three.setOnClickListener {
            if(line == "0"){
                line = "3"
            }
            else{
                if(line.length < 15){
                    line+="3"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = calculate(line)
        }
        binding.four.setOnClickListener {
            if(line == "0"){
                line = "4"
            }
            else{
                if(line.length < 15){
                    line+="4"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = calculate(line)
        }
        binding.five.setOnClickListener {
            if(line == "0"){
                line = "5"
            }
            else{
                if(line.length < 15){
                    line+="5"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + calculate(line)
        }
        binding.six.setOnClickListener {
            if(line == "0"){
                line = "6"
            }
            else{
                if(line.length < 15){
                    line+="6"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + calculate(line)
        }
        binding.seven.setOnClickListener {
            if(line == "0"){
                line = "7"
            }
            else{
                if(line.length < 15){
                    line+="7"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + calculate(line)
        }
        binding.eight.setOnClickListener {
            if(line == "0"){
                line = "8"
            }
            else{
                if(line.length < 15){
                    line+="8"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + calculate(line)
        }
        binding.nine.setOnClickListener {
            if(line == "0"){
                line = "9"
            }
            else{
                if(line.length < 15){
                    line+="9"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + calculate(line)
        }
        binding.zero.setOnClickListener {
            if(line == "0" || line.isEmpty()){
                line = "0"
            } else{
                if(line.length < 15){
                    line+="0"
                }
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + calculate(line)
        }
        binding.point.setOnClickListener {
            if(!line.contains(".") && line.isNotEmpty() && line.last() != '.' && line.length < 15){
                line +="."
            }
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + calculate(line)
        }

        binding.plus.setOnClickListener {
            if(!line.isEmpty()){
                if(line.last() != '+' && line.last() != '-' && line.last() != '/' && line.last() != '*'){
                    if(line.length > 1 && line[line.length - 1].isDigit()){
                        line+="+"
                    } else if (line.length == 1 && line.last().isDigit()){
                        line+="+"
                    }
                }
            }
            binding.outputWindow.text = line
        }
        binding.minus.setOnClickListener {
            if(!line.isEmpty()){
                if(line.last() != '+' && line.last() != '-' && line.last() != '/' && line.last() != '*'){
                    if(line.length > 1 && line[line.length - 1].isDigit()){
                        line+="-"
                    } else if (line.length == 1 && line.last().isDigit()){
                        line+="-"
                    }
                }
            }
            binding.outputWindow.text = line
        }
        binding.multiply.setOnClickListener {
            if(!line.isEmpty()){
                if(line.last() != '+' && line.last() != '-' && line.last() != '/' && line.last() != '*'){
                    if(line.length > 1 && line[line.length - 1].isDigit()){
                        line+="*"
                    } else if (line.length == 1 && line.last().isDigit()){
                        line+="*"
                    }
                }
            }
            binding.outputWindow.text = line
        }
        binding.devide.setOnClickListener {
            if(!line.isEmpty()){
                if(line.last() != '+' && line.last() != '-' && line.last() != '/' && line.last() != '*'){
                    if(line.length > 1 && line[line.length - 1].isDigit()){
                        line+="/"
                    } else if (line.length == 1 && line.last().isDigit()){
                        line+="/"
                    }
                }
            }
            binding.outputWindow.text = line
        }
        binding.delete.setOnClickListener {
            line = line.dropLast(1)
            if(line.isEmpty()) line = "0"
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + line
        }
        binding.ac.setOnClickListener {
            line = "0"
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + line
        }
        binding.equal.setOnClickListener {
            line = calculate(line)
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + line
        }
        binding.procent.setOnClickListener {
            line = procentage(line);
            binding.outputWindow.text = line
            binding.outputWindow2.text = "=" + line
        }
    }

    external fun calculate(input : String) : String

    external fun procentage(input : String) : String

    companion object {
        init {
            System.loadLibrary("mycalculator")
        }
    }
}