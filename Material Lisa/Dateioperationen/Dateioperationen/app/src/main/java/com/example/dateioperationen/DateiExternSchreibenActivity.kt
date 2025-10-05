package com.example.dateioperationen

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DateiExternSchreibenActivity : AppCompatActivity() {

    var mExternalStorageAvailable = false
    var mExternalStorageWriteable = false
    var state: String = Environment.getExternalStorageState()

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
                checkState()
                if (mExternalStorageWriteable) {
                    try {
                        val sdcard: File? = getExternalFilesDir(null)
                        val file = File(sdcard, FILENAME)
                        val fos = FileOutputStream(file)
                        fos.write(tv.getText().toString().toByteArray())
                        fos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        bt.setOnClickListener(ocl)
    }

    private fun checkState() {
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageWriteable = true
            mExternalStorageAvailable = mExternalStorageWriteable
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true
            mExternalStorageWriteable = false
        } else {
            mExternalStorageWriteable = false
            mExternalStorageAvailable = mExternalStorageWriteable
        }
    }
}