package com.example.uml_lern_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    // gleiche Keys wie Quiz/Unit
    private val PREFS = "quiz_prefs"
    private val KEY_PROFILE_POINTS = "profile_points"
    private val KEY_PASSED_PREFIX = "passed_" // + unitId

    private val unitOrder = listOf("uml_basics","uml_advanced","uml_practice")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val points = prefs.getInt(KEY_PROFILE_POINTS, 0)

        val passedMap = unitOrder.associateWith { id ->
            prefs.getBoolean(KEY_PASSED_PREFIX + id, false)
        }
        val passedCount = passedMap.values.count { it }
        val currentLevel = when {
            passedMap["uml_practice"] == true -> 3
            passedMap["uml_advanced"] == true -> 2
            passedMap["uml_basics"] == true -> 1
            else -> 0
        }

        binding.tvUsername.text = "User" // später aus Auth/Firestore
        binding.tvPoints.text = "Punkte: $points"
        binding.tvLevel.text = "Level: $currentLevel / 3"

        // Liste/Status kompakt
        binding.tvProgress.text = buildString {
            appendLine("UML Grundlagen: " + tick(passedMap["uml_basics"] == true))
            appendLine("Fortgeschrittene UML: " + tick(passedMap["uml_advanced"] == true))
            append("UML in der Praxis: " + tick(passedMap["uml_practice"] == true))
        }

        // Buttons
        binding.btnBack.setOnClickListener { finish() }
        binding.btnResetProgress.setOnClickListener {
            // nur lokal zurücksetzen (MVP)
            unitOrder.forEach { prefs.edit().putBoolean(KEY_PASSED_PREFIX + it, false).apply() }
            prefs.edit().putInt(KEY_PROFILE_POINTS, 0).apply()
            recreate()
        }
    }

    private fun tick(ok: Boolean) = if (ok) "✔️ bestanden" else "⛔ gesperrt"
}
