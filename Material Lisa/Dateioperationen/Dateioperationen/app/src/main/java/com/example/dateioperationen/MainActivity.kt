package com.example.dateioperationen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun spschreiben(v: View?) {
        val intent = Intent(this, SPSchreibenActivity::class.java)
        startActivity(intent)
    }

    fun splesen(v: View?) {
        val intent = Intent(this, SPLesenActivity::class.java)
        startActivity(intent)
    }

    fun dateischreiben(v: View?) {
        val intent = Intent(this, DateiSchreibenActivity::class.java)
        startActivity(intent)
    }

    fun dateilesen(v: View?) {
        val intent = Intent(this, DateiLesenActivity::class.java)
        startActivity(intent)
    }

    fun dateiexternschreiben(v: View?) {
        val intent = Intent(this, DateiExternSchreibenActivity::class.java)
        startActivity(intent)
    }

    fun dateiexternlesen(v: View?) {
        val intent = Intent(this, DateiExternLesenActivity::class.java)
        startActivity(intent)
    }

    fun verzeichnis(v: View?) {
        val intent = Intent(this, VerzeichnisActivity::class.java)
        startActivity(intent)
    }
}