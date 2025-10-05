package com.example.dateioperationen

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.FileOutputStream
import java.io.IOException

class DateiSchreibenActivity : AppCompatActivity() {

    val FILENAME = "Block30.txt"

    lateinit var tv: TextView
    lateinit var et: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datei)
        val bt: Button = findViewById(R.id.button6)
        bt.setText("Hinzufügen und Speichern")
        tv = findViewById<TextView>(R.id.textView2)
        tv.setText("")
        et = findViewById<EditText>(R.id.editText4)
        val ocl: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(view: View?) {
                tv.setText(tv.text.toString() + "\n" + et.text.toString())
                et.setText("")
                try {
                    val fos: FileOutputStream = openFileOutput(FILENAME, MODE_PRIVATE)
                    fos.write(tv.getText().toString().toByteArray())
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        bt.setOnClickListener(ocl)
    }
}