package com.example.uml_lern_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CourseListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)

        // RecyclerView konfigurieren.
        val rv: RecyclerView = findViewById(R.id.rvCourses)
        rv.layoutManager = LinearLayoutManager(this)

        // Adapter mit Testdaten (Firestore folgt später).
        val adapter = CourseAdapter(
            items = listOf(
                Course(id = "uml-basics", title = "UML-Grundlagen"),
                Course(id = "activity-diagrams", title = "Aktivitätsdiagramme")
            )
        )
        rv.adapter = adapter
    }
}
