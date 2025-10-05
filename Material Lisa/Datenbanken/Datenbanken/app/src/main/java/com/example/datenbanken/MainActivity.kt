package com.example.datenbanken

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.navigationi.R

class MainActivity : AppCompatActivity() {

    private lateinit var openHandler: OpenHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonGut = findViewById<ImageButton>(R.id.gut)
        buttonGut.setOnClickListener { e: View? ->
            imageButtonClicked(
                OpenHandler.MOOD_FINE
            )
        }

        val buttonOk = findViewById<ImageButton>(R.id.ok)
        buttonOk.setOnClickListener { e: View? ->
            imageButtonClicked(
                OpenHandler.MOOD_OK
            )
        }

        val buttonSchlecht = findViewById<ImageButton>(R.id.schlecht)
        buttonSchlecht.setOnClickListener { e: View? ->
            imageButtonClicked(
                OpenHandler.MOOD_BAD
            )
        }

        val buttonAuswertung: Button = findViewById(R.id.auswertung)

        buttonAuswertung.setOnClickListener { v ->
            val intent = Intent(this, HistoryListActivity::class.java)
            startActivity(intent)
        }

        openHandler = OpenHandler(this)
    }

    override fun onPause() {
        super.onPause()
        openHandler.close()
    }

    private fun imageButtonClicked(mood: Int) {
        openHandler.insert(mood, System.currentTimeMillis())
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
    }
}