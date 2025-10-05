package com.example.dateioperationen

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SPLesenActivity : AppCompatActivity() {

    val PREFS_NAME = "BLOCK30"

    lateinit var sw: Switch
    lateinit var et: EditText
    lateinit var sb: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sp)
        val bt: Button = findViewById(R.id.button3)
        bt.setText("Lesen")
        sw = findViewById<Switch>(R.id.switch1)
        et = findViewById<EditText>(R.id.editText)
        sb = findViewById<SeekBar>(R.id.seekBar)
        val ocl: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(view: View?) {
                val sp = getSharedPreferences(PREFS_NAME, 0)
                sw.setChecked(sp.getBoolean("sw", false))
                et.setText(sp.getString("et", "Kein Wert gefunden!"))
                sb.setProgress(sp.getInt("sb", 0))
            }
        }
        bt.setOnClickListener(ocl)
    }
}