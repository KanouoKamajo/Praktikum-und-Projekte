package com.example.uml_lern_app

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

// Zeigt alle Units eines gewählten Kurses an.
class UnitListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_list)

        // Kursdaten aus der vorherigen Activity empfangen
        val courseId = intent.getStringExtra("courseId") ?: return
        val courseTitle = intent.getStringExtra("courseTitle") ?: ""

        // Überschrift mit Kursnamen anzeigen
        findViewById<TextView>(R.id.tvHeaderUnits).text =
            "Units – $courseTitle"

        // RecyclerView vorbereiten
        val rv: RecyclerView = findViewById(R.id.rvUnits)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = UnitAdapter(emptyList())
        rv.adapter = adapter

        // Firestore abrufen
        val db = FirebaseFirestore.getInstance()
        db.collection("courses")
            .document(courseId)
            .collection("units")
            .orderBy("order")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.map { d ->
                    UnitItem(
                        id = d.id,
                        title = d.getString("title") ?: "(ohne Titel)"
                    )
                }
                adapter.submitList(list)
                Log.d("UNIT_UI", "Units geladen: ${list.size}")
            }
            .addOnFailureListener { e ->
                Log.e("UNIT_UI", "Fehler beim Laden der Units", e)
            }
    }
}
