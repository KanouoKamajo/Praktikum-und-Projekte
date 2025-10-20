package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityUnitBinding

class UnitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitBinding

    // === Level-Lock: gleiche prefs/keys wie in QuizActivity ===
    private val PREFS = "quiz_prefs"
    private val KEY_PASSED_PREFIX = "passed_"   // + unitId

    private fun isUnitPassed(unitId: String?): Boolean {
        if (unitId.isNullOrBlank()) return true // keine Vorgänger-Unit → frei
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        return prefs.getBoolean(KEY_PASSED_PREFIX + unitId, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Kontext aus Intent ---
        val courseId   = intent.getStringExtra("courseId").orEmpty()
        val unitId     = intent.getStringExtra("unitId")          // aktuelle Unit (Level)
        val prevUnitId = intent.getStringExtra("prevUnitId")      // VORGÄNGER-Unit (optional)

        // Falls nur courseId verwendet wird, Unit-IDs sauber ableiten
        val effectiveUnitId = unitId ?: courseId
        val effectivePrevId = prevUnitId ?: when (effectiveUnitId) {
            "uml_basics"   -> null
            "uml_advanced" -> "uml_basics"
            "uml_practice" -> "uml_advanced"
            else -> null
        }

        // --- UI-Inhalte basierend auf courseId / effectiveUnitId ---
        when (effectiveUnitId) {
            "uml_basics" -> {
                binding.tvTitle.text = "UML Grundlagen"
                binding.tvMaterial.text = "Einführung in UML-Diagramme"
                binding.tvDescription.text = "Lernen Sie die Basis: Klassen-, Use-Case- und Sequenzdiagramme."
                binding.imgPreview.setImageResource(R.drawable.uml_basics)
            }
            "uml_advanced" -> {
                binding.tvTitle.text = "Fortgeschrittene UML"
                binding.tvMaterial.text = "Aktivitäts- & Zustandsdiagramme"
                binding.tvDescription.text = "Vertiefung zu Kontrollfluss, Guards, Zustandsautomaten und Paketen."
                binding.imgPreview.setImageResource(R.drawable.uml_advanced)
            }
            "uml_practice" -> {
                binding.tvTitle.text = "UML in der Praxis"
                binding.tvMaterial.text = "Fallstudien & Projekte"
                binding.tvDescription.text = "UML in realen Szenarien anwenden – mit Mini-Cases."
                binding.imgPreview.setImageResource(R.drawable.uml_practice)
            }
            else -> {
                binding.tvTitle.text = "Kurs"
                binding.tvMaterial.text = "Lernmaterial"
                binding.tvDescription.text = "Bitte wählen Sie einen Kurs auf der vorherigen Seite."
                // binding.imgPreview.setImageResource(R.drawable.placeholder)
            }
        }

        // --- Quiz starten (mit Level-Lock) ---
        binding.btnQuiz.setOnClickListener {
            // Wenn es eine Vorgänger-Unit gibt, muss sie bestanden sein
            if (!isUnitPassed(effectivePrevId)) {
                Toast.makeText(this, "Bitte zuerst die vorherige Unit bestehen.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(
                Intent(this, QuizActivity::class.java).apply {
                    putExtra("courseId", effectiveUnitId) // bei dir = auch Kurskennung
                    putExtra("quizTitle", binding.tvTitle.text.toString())
                    // wichtig für Ergebnis/Level-Lock:
                    putExtra("unitId", effectiveUnitId)
                }
            )
        }

        // --- Zur Kursübersicht ---
        binding.btnBackToCourses.setOnClickListener {
            startActivity(Intent(this, CourseActivity::class.java))
            finish()
        }

        // --- Optionaler Zurück-Button (falls im Layout vorhanden) ---
        val backButton = binding.root.findViewById<android.view.View?>(R.id.btnBack)
        backButton?.setOnClickListener { finish() }

        // === Notizen öffnen (mit Kontext) ===
        val btnOpenNotes: Button? = binding.root.findViewById(R.id.btnOpenNotes)
        btnOpenNotes?.setOnClickListener {
            if (effectiveUnitId.isEmpty()) {
                Toast.makeText(this, "CourseId/UnitId fehlt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val i = Intent(this, NotesActivity::class.java).apply {
                putExtra("courseId", effectiveUnitId)
                putExtra("unitId", effectiveUnitId)
            }
            startActivity(i)
        }

        // (Optional) UI-Hinweis: Button disabled anzeigen, wenn gesperrt
        // if (!isUnitPassed(effectivePrevId)) {
        //     binding.btnQuiz.isEnabled = false
        //     binding.btnQuiz.alpha = 0.5f
        // }
    }
}
