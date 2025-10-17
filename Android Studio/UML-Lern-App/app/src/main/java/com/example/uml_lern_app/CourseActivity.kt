package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uml_lern_app.databinding.ActivityCourseBinding

class CourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseBinding.inflate(layoutInflater) // Layout: res/layout/activity_course.xml
        setContentView(binding.root)

        val courses = listOf(
            Course("uml1", "UML Grundlagen", "Grundlagen der UML-Diagramme", R.drawable.ic_course_red),
            Course("uml2", "Fortgeschrittene UML", "Komplexere Konzepte & Systemdesign", R.drawable.ic_course_red),
            Course("uml3", "UML in der Praxis", "Reale Anwendungsfälle & Projekte", R.drawable.ic_course_red)
        )

        binding.rvCourses.layoutManager = LinearLayoutManager(this)
        binding.rvCourses.adapter = CourseAdapter(courses) { clicked ->
            startActivity(Intent(this, UnitActivity::class.java).putExtra("courseId", clicked.id))
        }

        // BottomNav optional...
    }
}
