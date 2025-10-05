package com.example.dateioperationen

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SPSchreibenActivity : AppCompatActivity() {

    val PREFS_NAME = "BLOCK30"

    lateinit var sw: Switch
    lateinit var et: EditText
    lateinit var sb: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sp)
        val bt: Button = findViewById(R.id.button3)
        bt.setText("Schreiben")
        sw = findViewById<Switch>(R.id.switch1)
        et = findViewById<EditText>(R.id.editText)
        sb = findViewById<SeekBar>(R.id.seekBar)
        val ocl: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(view: View?) {
                val sp = getSharedPreferences(PREFS_NAME, 0)
                val sp_editor = sp.edit()
                sp_editor.putBoolean("sw", sw.isChecked())
                sp_editor.putString("et", et.getText().toString())
                sp_editor.putInt("sb", sb.getProgress())
                sp_editor.commit()
                val toast = Toast.makeText(applicationContext, "Gespeichert!", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        bt.setOnClickListener(ocl)
    }
}