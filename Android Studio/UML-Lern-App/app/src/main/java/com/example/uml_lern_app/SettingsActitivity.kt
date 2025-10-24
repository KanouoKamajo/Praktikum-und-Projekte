package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Benutzer-Daten anzeigen (nur Name + E-Mail, kein Edit hier) ---
        val user = auth.currentUser
        val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Gast"
        val email = user?.email?.takeIf { !it.isNullOrBlank() } ?: "—"

        binding.tvNameValue.text = displayName
        binding.tvMailValue.text = email

        // Optional: Sprache/Design statisch anzeigen (oder später dynamisch)
        // binding.tvLangValue.text = "Deutsch"
        // binding.tvThemeValue.text = "System"

        // --- Buttons unten ---
        binding.btnBackProfile.setOnClickListener {
            // Wenn die Settings vom Profil aus geöffnet wurden, reicht finish()
            finish()
            // Falls du IMMER zurück zum Profil willst, statt finish():
            // startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnHome.setOnClickListener {
            startActivity(
                Intent(this, CourseActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            finish()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Abgemeldet", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    // Task leeren, damit man nicht zurück in Settings swipen kann
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
            finish()
        }
    }
}
