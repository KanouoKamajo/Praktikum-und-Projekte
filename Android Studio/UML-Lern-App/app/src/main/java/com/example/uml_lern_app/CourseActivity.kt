package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uml_lern_app.databinding.ActivityCourseBinding

class CourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---------------------------------------
        // 1) Zurück-Button
        // ---------------------------------------
        binding.btnBack.setOnClickListener { finish() }

        // ---------------------------------------
        // 2) Kursliste
        // ---------------------------------------
        val courses = listOf(
            Course(
                id = "uml_basics",
                title = "UML Grundlagen",
                subtitle = "Klassendiagramme, Use-Cases, Sequenzen",
                iconRes = R.drawable.ic_course_red
            ),
            Course(
                id = "uml_advanced",
                title = "Fortgeschrittene UML",
                subtitle = "Aktivitäts-, Zustands- & Paketdiagramme",
                iconRes = R.drawable.ic_course_red
            ),
            Course(
                id = "uml_practice",
                title = "UML in der Praxis",
                subtitle = "Reale Anwendungsfälle & Projekte",
                iconRes = R.drawable.ic_course_red
            )
        )

        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(this@CourseActivity)
            adapter = CourseAdapter(courses) { clicked ->
                binding.tvHint.visibility = View.GONE
                // → direkt zur UnitActivity (mit Level-Kette)
                openUnit(clicked.id)
            }
        }

        // ---------------------------------------
        // 3) BottomNavigation (mit Profil)
        //    Erwartet ein Menü mit IDs: nav_courses, nav_quiz, nav_notes, nav_profile, nav_admin
        // ---------------------------------------
        binding.root.findViewById<View?>(R.id.bottomNav)?.let {
            try {
                val bottom = com.google.android.material.bottomnavigation.BottomNavigationView::class.java
                    .cast(it)

                bottom.selectedItemId = R.id.nav_courses

                bottom.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.nav_courses -> true // bereits hier

                        R.id.nav_quiz -> {
                            // Standard: erstes Level öffnen (oder passe an)
                            openUnit("uml_basics")
                            true
                        }

                        R.id.nav_notes -> {
                            startActivity(Intent(this, NotesActivity::class.java).apply {
                                putExtra("courseId", "uml_basics")
                                putExtra("unitId", "uml_basics")
                            })
                            true
                        }

                        R.id.nav_profile -> {
                            startActivity(Intent(this, ProfileActivity::class.java))
                            true
                        }

                        R.id.nav_admin -> {
                            // Placeholder – Activity anlegen, damit kein Crash
                            startActivity(Intent(this, AdminActivity::class.java))
                            true
                        }

                        else -> false
                    }
                }
            } catch (_: Throwable) {
                // BottomNav ist optional – kein Crash
            }
        }

        // ---------------------------------------
        // 4) Hinweistext (falls nicht im XML gesetzt)
        // ---------------------------------------
        if (binding.tvHint.text.isNullOrBlank()) {
            binding.tvHint.text = "Bitte wählen Sie einen Kurs aus, um fortzufahren."
        }
    }

    // ---------- Navigation-Helfer ----------

    private fun openUnit(unitId: String) {
        val prevId = when (unitId) {
            "uml_basics"   -> null
            "uml_advanced" -> "uml_basics"
            "uml_practice" -> "uml_advanced"
            else -> null
        }

        startActivity(
            Intent(this, UnitActivity::class.java).apply {
                // In deinem Projekt sind Kurs- und Unit-IDs identisch → beides setzen
                putExtra("courseId", unitId)
                putExtra("unitId", unitId)
                if (prevId != null) putExtra("prevUnitId", prevId)
            }
        )
    }
}
