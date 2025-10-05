package com.example.dateioperationen

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class DateiExternLesenActivity : AppCompatActivity() {

    val FILENAME = "Block30.txt"

    lateinit var tv: TextView
    lateinit var et: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datei)
        val bt: Button = findViewById(R.id.button6)
        bt.setText("Datei auslesen.")
        tv = findViewById<TextView>(R.id.textView2)
        tv.setText("")
        et = findViewById<EditText>(R.id.editText4)
        et.setVisibility(View.INVISIBLE)
        val ocl: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(view: View?) {
                try {
                    val sdcard: File? = getExternalFilesDir(null)
                    val file = File(sdcard, FILENAME)
                    val fis = FileInputStream(file)
                    val length: Int = fis.available()
                    var result = ""
                    for (i in 0 until length) {
                        result = result + fis.read().toChar()
                    }
                    tv.setText(result)
                    fis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        bt.setOnClickListener(ocl)
    }
}