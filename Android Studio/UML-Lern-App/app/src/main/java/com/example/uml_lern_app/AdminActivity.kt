package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uml_lern_app.databinding.ActivityAdminBinding
import com.example.uml_lern_app.databinding.ItemAdminCourseBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth                     // ← NEU
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source                    // ← NEU

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }    // ← NEU
    private var isAdmin = false                                // ← NEU

    private val adapter = CourseAdminAdapter(mutableListOf(), ::onMore)

    data class AdminCourse(
        val id: String,
        val title: String,
        val questionCount: Int = 0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tabs: Liste sichtbar lassen, Navigation erfolgt über "…"-Menü pro Kurs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Kurse verwalten"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Fragen verwalten"))
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { /* nichts verstecken */ }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Liste
        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        binding.rvItems.adapter = adapter

        // + Kurs
        binding.btnAdd.setOnClickListener { showAddDialog() }

        // Kurse laden
        subscribeCourses()
    }

    override fun onStart() {
        super.onStart()
        guardAdmin()   // ← NEU: Zugriff prüfen
    }

    // --- Admin-Guard (offline: Cache, dann Server) ---
    private fun guardAdmin() {
        val u = auth.currentUser
        if (u == null) {
            Toast.makeText(this, "Bitte zuerst einloggen.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val doc = db.collection("users").document(u.uid)

        // 1) Cache (offline)
        doc.get(Source.CACHE)
            .addOnSuccessListener { d ->
                if ((d.getString("role") ?: "") == "admin") isAdmin = true
            }
            .addOnCompleteListener {
                // 2) Server (online)
                doc.get(Source.SERVER)
                    .addOnSuccessListener { fresh ->
                        isAdmin = (fresh.getString("role") == "admin")
                        if (!isAdmin) {
                            Toast.makeText(this, "Kein Admin-Zugriff.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        if (!isAdmin) {
                            Toast.makeText(this, "Kein Admin-Zugriff (offline & kein Cache).", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
            }
    }

    // --- Firestore: live lesen + Frageanzahl mit Offline-Fallback ---
    private fun subscribeCourses() {
        db.collection("courses")
            .orderBy("title", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Toast.makeText(this, "Laden fehlgeschlagen: ${err.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { d ->
                    AdminCourse(id = d.id, title = d.getString("title") ?: d.id, questionCount = 0)
                }?.toMutableList() ?: mutableListOf()

                adapter.setData(list)

                // Frageanzahl pro Kurs
                list.forEachIndexed { index, course ->
                    val qRef = db.collection("courses").document(course.id).collection("questions")
                    qRef.count()
                        .get(AggregateSource.SERVER)
                        .addOnSuccessListener { agg ->
                            adapter.updateAt(index, course.copy(questionCount = agg.count.toInt()))
                        }
                        .addOnFailureListener {
                            qRef.get(Source.CACHE)
                                .addOnSuccessListener { cacheSnap ->
                                    adapter.updateAt(index, course.copy(questionCount = cacheSnap.size()))
                                }
                        }
                }
            }
    }

    // --- Dialoge ---
    private fun showAddDialog(existing: AdminCourse? = null) {
        val etId = EditText(this).apply {
            hint = "ID (z.B. uml_basics)"; setText(existing?.id ?: "")
            isEnabled = existing == null
        }
        val etTitle = EditText(this).apply {
            hint = "Titel"; setText(existing?.title ?: "")
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 12, 24, 0)
            addView(etId); addView(etTitle)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (existing == null) "Kurs hinzufügen" else "Kurs bearbeiten")
            .setView(box)
            .setPositiveButton("Speichern") { _, _ ->
                val id = etId.text.toString().trim()
                val title = etTitle.text.toString().trim()
                if (id.isEmpty() || title.isEmpty()) {
                    Toast.makeText(this, "Bitte ID und Titel eingeben", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                saveCourse(id, title)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun saveCourse(id: String, title: String) {
        db.collection("courses").document(id)
            .set(mapOf("title" to title))
            .addOnSuccessListener { Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e -> Toast.makeText(this, "Fehler: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    private fun deleteCourse(id: String) {
        db.collection("courses").document(id)
            .delete()
            .addOnSuccessListener { Toast.makeText(this, "Gelöscht", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e -> Toast.makeText(this, "Fehler: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    // --- Popup-Menü ---
    private fun onMore(anchor: View, item: AdminCourse) {
        PopupMenu(this, anchor).apply {
            menu.add(0, 1, 0, "Bearbeiten")
            menu.add(0, 2, 1, "Fragen verwalten")
            menu.add(0, 3, 2, "Löschen")
            setOnMenuItemClickListener { mi: MenuItem ->
                when (mi.itemId) {
                    1 -> showAddDialog(item)
                    2 -> {
                        // **JETZT wirklich öffnen statt Toast**
                        val i = Intent(this@AdminActivity, QuestionsAdminActivity::class.java).apply {
                            putExtra("courseId", item.id)
                            putExtra("courseTitle", item.title)
                        }
                        startActivity(i)
                    }
                    3 -> deleteCourse(item.id)
                }
                true
            }
            show()
        }
    }

    // --- Adapter ---
    private class CourseAdminAdapter(
        private val data: MutableList<AdminCourse>,
        private val onMore: (View, AdminCourse) -> Unit
    ) : RecyclerView.Adapter<CourseAdminAdapter.VH>() {

        fun setData(newData: List<AdminCourse>) {
            data.clear(); data.addAll(newData); notifyDataSetChanged()
        }

        fun updateAt(index: Int, updated: AdminCourse) {
            if (index in data.indices) {
                data[index] = updated
                notifyItemChanged(index)
            }
        }

        inner class VH(val vb: ItemAdminCourseBinding) : RecyclerView.ViewHolder(vb.root)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val vb = ItemAdminCourseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VH(vb)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val c = data[position]
            holder.vb.tvTitle.text = c.title
            holder.vb.tvSubtitle.text = "${c.questionCount} Fragen"
            holder.vb.btnMore.setOnClickListener { onMore(it, c) }
        }

        override fun getItemCount(): Int = data.size
    }
}
