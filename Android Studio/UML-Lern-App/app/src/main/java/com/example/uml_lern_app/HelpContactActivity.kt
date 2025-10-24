package com.example.uml_lern_app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityHelpContactBinding

class HelpContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpContactBinding

    private val supportEmail = "support@uml-app.example"
    private val supportPhone = "+49123456789"
    private val websiteUrl  = "https://uml-app.example"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Werte in die TextViews setzen (IDs existieren im XML)
        binding.tvEmailValue.text = supportEmail
        binding.tvPhoneValue.text = supportPhone
        binding.tvWebValue.text   = websiteUrl

        binding.btnSendEmail.setOnClickListener {
            val i = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$supportEmail")
                putExtra(Intent.EXTRA_SUBJECT, "Anfrage aus der UML Lern-App")
                putExtra(Intent.EXTRA_TEXT, "Hallo Team,\n\n")
            }
            tryStart(i, "Kein E-Mail-Client gefunden.")
        }

        binding.btnCall.setOnClickListener {
            val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$supportPhone"))
            tryStart(i, "Telefon-App nicht gefunden.")
        }

        binding.btnOpenWebsite.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
            tryStart(i, "Kein Browser gefunden.")
        }

        binding.btnSettings.setOnClickListener {
            // Falls du SettingsActivity noch nicht hast, erstelle sie oder ersetze das Ziel.
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun tryStart(intent: Intent, errorMsg: String) {
        try { startActivity(intent) }
        catch (_: ActivityNotFoundException) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
