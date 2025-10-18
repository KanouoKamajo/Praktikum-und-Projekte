package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uml_lern_app.databinding.ActivityCourseBinding
import kotlin.jvm.java

class CourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---------------------------------------
        // 1) UI: Zurück-Button unten
        // ---------------------------------------
        binding.btnBack.setOnClickListener {
            finish() // zurück zur vorherigen Seite (z. B. Onboarding Step 2)
        }

        // ---------------------------------------
        // 2) Datenquelle: Kurse (jede ID hat später eigenen Inhalt)
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

        // ---------------------------------------
        // 3) RecyclerView einrichten
        // ---------------------------------------
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(this@CourseActivity)
            adapter = CourseAdapter(courses) { clicked ->
                // Hinweistext ausblenden, sobald etwas gewählt wurde
                binding.tvHint.visibility = View.GONE

                // zur UnitActivity mit kurs-spezifischer ID
                startActivity(
                    Intent(this@CourseActivity, UnitActivity::class.java)
                        .putExtra("courseId", clicked.id)
                )
            }
        }

        // ---------------------------------------
        // 4) Optional: BottomNavigation (falls im Layout vorhanden)
        // ---------------------------------------
        // Nur setzen, wenn es die View in deinem Layout gibt:
        binding.root.findViewById<View?>(R.id.bottomNav)?.let { _ ->
            // aktiven Tab optisch markieren (sofern du das Menü nutzt)
            // (cast vermeiden, wir setzen nur das Checked-Item über Menu)
            try {
                val bn = com.google.android.material.bottomnavigation.BottomNavigationView::class.java
                    .cast(findViewById(R.id.bottomNav))
                bn.selectedItemId = R.id.nav_courses
                bn.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.nav_courses -> true // schon hier
                        R.id.nav_quiz -> {
                            Toast.makeText(this, "Quiz kommt später 👀", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.nav_notes -> {
                            Toast.makeText(this, "Notizen kommen später 📝", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.nav_admin -> {
                            Toast.makeText(this, "Admin-Bereich ⚙️", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }
            } catch (_: Throwable) {
                // ignorieren – BottomNav ist optional
            }
        }

        // ---------------------------------------
        // 5) Hinweistext in „Sie“-Form (falls nicht statisch im XML)
        // ---------------------------------------
        // Wenn du den Text bereits im XML gesetzt hast, kannst du diesen Block entfernen.
        if (binding.tvHint.text.isNullOrBlank()) {
            binding.tvHint.text = "Bitte wählen Sie einen Kurs aus, um fortzufahren."
        }
    }
}
