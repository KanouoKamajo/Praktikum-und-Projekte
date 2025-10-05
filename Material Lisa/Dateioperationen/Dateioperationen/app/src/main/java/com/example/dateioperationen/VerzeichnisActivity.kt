package com.example.dateioperationen

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class VerzeichnisActivity : AppCompatActivity() {

    lateinit var tv: TextView
    lateinit var et: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verzeichnis)
        et = findViewById<EditText>(R.id.editText2)
        val bt1: Button = findViewById(R.id.button11)
        val ocl1: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(view: View?) {
                val folder = File(filesDir.toString() + "/" + et.getText().toString())
                if (!folder.exists()) folder.mkdir()
                listfiles()
            }
        }
        bt1.setOnClickListener(ocl1)
        val bt2: Button = findViewById(R.id.button13)
        val ocl2: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(view: View?) {
                deletefiles()
                listfiles()
            }
        }
        bt2.setOnClickListener(ocl2)
        tv = findViewById<TextView>(R.id.textView4)
        listfiles()
    }

    private fun listfiles() {
        tv!!.text = ""
        for (file1 in filesDir.listFiles()) {
            tv!!.text = """
            ${tv!!.text.toString() + file1.toString()}
            
            """.trimIndent()
        }
    }

    private fun deletefiles() {
        for (file1 in filesDir.listFiles()) {
            file1.delete()
        }
    }
}