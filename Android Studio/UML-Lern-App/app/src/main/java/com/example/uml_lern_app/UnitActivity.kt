package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityUnitBinding
import kotlin.jvm.java

class UnitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kurs-ID aus Intent lesen
        // In UnitActivity.kt → in onCreate() nach dem binding = ... und setContentView(binding.root)
        val courseId = intent.getStringExtra("courseId").orEmpty()

        when (courseId) {
            "uml_basics" -> {
                binding.tvTitle.text = "UML Grundlagen"
                binding.tvMaterial.text = "Einführung in UML-Diagramme"
                binding.tvDescription.text = "Lernen Sie die Basis: Klassen-, Use-Case- und Sequenzdiagramme."
                binding.imgPreview.setImageResource(R.drawable.uml_basics)   // ← Bild setzen
            }
            "uml_advanced" -> {
                binding.tvTitle.text = "Fortgeschrittene UML"
                binding.tvMaterial.text = "Aktivitäts- & Zustandsdiagramme"
                binding.tvDescription.text = "Vertiefung zu Kontrollfluss, Guards, Zustandsautomaten und Paketen."
                binding.imgPreview.setImageResource(R.drawable.uml_advanced) // ← Bild setzen
            }
            "uml_practice" -> {
                binding.tvTitle.text = "UML in der Praxis"
                binding.tvMaterial.text = "Fallstudien & Projekte"
                binding.tvDescription.text = "UML in realen Szenarien anwenden – mit Mini-Cases."
                binding.imgPreview.setImageResource(R.drawable.uml_practice) // ← Bild setzen
            }
            else -> {
                binding.tvTitle.text = "Kurs"
                binding.tvMaterial.text = "Lernmaterial"
                binding.tvDescription.text = "Bitte wählen Sie einen Kurs auf der vorherigen Seite."
                // Fallback: grau lassen oder ein Platzhalterbild verwenden
                // binding.imgPreview.setImageResource(R.drawable.placeholder)
            }
        }

        binding.btnQuiz.setOnClickListener {
            startActivity(
                Intent(this, QuizActivity::class.java).apply {
                    putExtra("courseId", courseId)
                    putExtra("quizTitle", binding.tvTitle.text.toString())
                    // optional: feste Anzahl, dann erscheint kein Dialog
                    // putExtra("questionCount", 7)
                }
            )
        }


        binding.btnBackToCourses.setOnClickListener {
            // Zurück zur Kursübersicht
            val intent = Intent(this, CourseActivity::class.java)
            startActivity(intent)
            finish()
        }


        // Falls im Layout vorhanden: Zurück-Button (unten)
        val backButton = binding.root.findViewById<android.view.View?>(R.id.btnBack)
        backButton?.setOnClickListener { finish() }
    }
}
