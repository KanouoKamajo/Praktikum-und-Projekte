package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * NotesActivity
 * - CRUD für Notizen des eingeloggten Users
 * - Optionaler Kontext (Intent-Extras): courseId, unitId, questionId
 * - Filter: Alle | Diese Unit | Diese Frage
 * - Offline: Erst Cache, dann Server
 */
class NotesActivity : AppCompatActivity() {

    // UI
    private lateinit var spFilter: Spinner
    private lateinit var btnAdd: Button
    private lateinit var etNote: EditText
    private lateinit var btnSave: Button
    private lateinit var rvNotes: RecyclerView
    private lateinit var btnBackToUnit: Button
    private lateinit var btnFinishNotes: Button

    // Firebase
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    // Daten
    private val notes = mutableListOf<Note>()
    private lateinit var adapter: NotesAdapter

    // Kontext (optional)
    private var courseId: String? = null
    private var unitId: String? = null
    private var questionId: String? = null

    // Edit-State
    private var editingNoteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        // Eingeloggt?
        if (auth.currentUser == null) {
            Toast.makeText(this, "Bitte zuerst einloggen", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // UI referenzieren
        spFilter = findViewById(R.id.spFilter)
        btnAdd = findViewById(R.id.btnAdd)
        etNote = findViewById(R.id.etNote)
        btnSave = findViewById(R.id.btnSave)
        rvNotes = findViewById(R.id.rvNotes)
        btnBackToUnit = findViewById(R.id.btnBackToUnit)
        btnFinishNotes = findViewById(R.id.btnFinishNotes)

        // Kontext aus Intent
        courseId = intent.getStringExtra("courseId")
        unitId = intent.getStringExtra("unitId")
        questionId = intent.getStringExtra("questionId")

        // Liste einrichten
        adapter = NotesAdapter(
            items = notes,
            onEdit = { note -> startEdit(note) },
            onDelete = { note -> deleteNote(note.id) }
        )
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = adapter

        // Filterwechsel → neu laden
        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) = loadNotes()
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Neu → Editor öffnen
        btnAdd.setOnClickListener {
            editingNoteId = null
            etNote.setText("")
            etNote.visibility = View.VISIBLE
            btnSave.visibility = View.VISIBLE
            etNote.requestFocus()
        }

        // Speichern → create oder update
        btnSave.setOnClickListener {
            val text = etNote.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Bitte Text eingeben", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (editingNoteId == null) createNote(text) else updateNote(editingNoteId!!, text)
        }

        // Navigation
        btnBackToUnit.setOnClickListener {
            startActivity(Intent(this, UnitActivity::class.java).apply {
                putExtra("courseId", courseId ?: "")
                putExtra("unitId", unitId ?: "")
            })
            finish()
        }
        btnFinishNotes.setOnClickListener {
            startActivity(Intent(this, CourseActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }

        // Start
        loadNotes()
    }

    // ----- Datenmodell -----
    data class Note(
        val id: String = "",
        val userId: String = "",
        val courseId: String? = null,
        val unitId: String? = null,
        val questionId: String? = null,
        val scope: String = "general",  // "general" | "unit" | "question"
        val text: String = "",
        val createdAt: Timestamp? = null,
        val updatedAt: Timestamp? = null
    )

    // ----- Offline-fähiges Laden (Cache → Server) -----
    private fun loadNotes() {
        val uid = auth.currentUser?.uid ?: return
        val filter = spFilter.selectedItem?.toString() ?: "Alle"

        var q: Query = db.collection("notes").whereEqualTo("userId", uid)
        when (filter) {
            "Diese Unit"   -> if (!unitId.isNullOrEmpty()) q = q.whereEqualTo("unitId", unitId)
            "Diese Frage"  -> if (!questionId.isNullOrEmpty()) q = q.whereEqualTo("questionId", questionId)
            else -> { /* Alle */ }
        }
        q = q.orderBy("updatedAt", Query.Direction.DESCENDING)

        // 1) Erst: Cache (zeigt sofort – auch offline)
        q.get(Source.CACHE)
            .addOnSuccessListener { snap ->
                render(snap)
                // 2) Dann: Server (falls online; aktualisiert Liste, wenn neuer)
                q.get(Source.SERVER)
                    .addOnSuccessListener { fresh -> render(fresh) }
                    .addOnFailureListener { e ->
                        handleLoadFailure(e)
                    }
            }
            .addOnFailureListener {
                // Cache evtl. leer → direkt Server versuchen
                q.get(Source.SERVER)
                    .addOnSuccessListener { snap -> render(snap) }
                    .addOnFailureListener { e -> handleLoadFailure(e) }
            }
    }

    private fun handleLoadFailure(e: Exception) {
        if (e is FirebaseFirestoreException &&
            e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION &&
            (e.message?.contains("index", true) == true)
        ) {
            Log.e("Notes", "Index fehlt für diese Query.", e)
            Toast.makeText(
                this,
                "Für diesen Filter fehlt ein Firestore-Index. Bitte in der Konsole anlegen.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Log.e("Notes", "Load failed", e)
            Toast.makeText(this, "Offline? Zeige Cache-Daten (falls vorhanden).", Toast.LENGTH_SHORT).show()
        }
    }

    private fun render(snap: com.google.firebase.firestore.QuerySnapshot) {
        notes.clear()
        for (doc in snap.documents) {
            notes.add(
                Note(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    courseId = doc.getString("courseId"),
                    unitId = doc.getString("unitId"),
                    questionId = doc.getString("questionId"),
                    scope = doc.getString("scope") ?: "general",
                    text = doc.getString("text") ?: "",
                    createdAt = doc.getTimestamp("createdAt"),
                    updatedAt = doc.getTimestamp("updatedAt")
                )
            )
        }
        adapter.notifyDataSetChanged()
    }

    // ----- Create -----
    private fun createNote(text: String) {
        val uid = auth.currentUser?.uid ?: return
        val now = Timestamp.now()
        val scope = when {
            questionId != null -> "question"
            unitId != null -> "unit"
            else -> "general"
        }

        val data = hashMapOf(
            "userId" to uid,
            "courseId" to courseId,
            "unitId" to unitId,
            "questionId" to questionId,
            "scope" to scope,
            "text" to text,
            "createdAt" to now,
            "updatedAt" to now
        )

        db.collection("notes").add(data)
            .addOnSuccessListener {
                // Auch offline bekommst du bereits eine lokale DocRef; Sync folgt automatisch.
                Toast.makeText(this, "Gespeichert (wird synchronisiert …)", Toast.LENGTH_SHORT).show()
                etNote.setText("")
                etNote.visibility = View.GONE
                btnSave.visibility = View.GONE
                loadNotes()
            }
            .addOnFailureListener { e ->
                Log.e("Notes", "Save failed", e)
                Toast.makeText(this, "Speichern fehlgeschlagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ----- Update -----
    private fun updateNote(id: String, text: String) {
        val now = Timestamp.now()
        db.collection("notes").document(id)
            .update(mapOf("text" to text, "updatedAt" to now))
            .addOnSuccessListener {
                etNote.setText("")
                etNote.visibility = View.GONE
                btnSave.visibility = View.GONE
                editingNoteId = null
                Toast.makeText(this, "Aktualisiert (wird synchronisiert …)", Toast.LENGTH_SHORT).show()
                loadNotes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Aktualisierung fehlgeschlagen", Toast.LENGTH_SHORT).show()
            }
    }

    // ----- Delete -----
    private fun deleteNote(id: String) {
        db.collection("notes").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Gelöscht (wird synchronisiert …)", Toast.LENGTH_SHORT).show()
                loadNotes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Löschen fehlgeschlagen", Toast.LENGTH_SHORT).show()
            }
    }

    // ----- Edit starten -----
    private fun startEdit(note: Note) {
        editingNoteId = note.id
        etNote.visibility = View.VISIBLE
        btnSave.visibility = View.VISIBLE
        etNote.setText(note.text)
        etNote.requestFocus()
    }

    // ----- RecyclerView Adapter -----
    class NotesAdapter(
        private val items: List<Note>,
        private val onEdit: (Note) -> Unit,
        private val onDelete: (Note) -> Unit
    ) : RecyclerView.Adapter<NotesAdapter.VH>() {

        private val df = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvMeta: TextView = v.findViewById(R.id.tvMeta)
            val tvText: TextView = v.findViewById(R.id.tvText)
            val btnEdit: Button = v.findViewById(R.id.btnEdit)
            val btnDelete: Button = v.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val n = items[position]
            val whenTxt = n.updatedAt?.toDate()?.let { df.format(it) } ?: "-"
            val scopeTxt = when {
                n.questionId != null -> "Frage"
                n.unitId != null -> "Unit"
                else -> "Allgemein"
            }
            h.tvMeta.text = "$scopeTxt • $whenTxt"
            h.tvText.text = n.text
            h.btnEdit.setOnClickListener { onEdit(n) }
            h.btnDelete.setOnClickListener { onDelete(n) }
        }

        override fun getItemCount() = items.size
    }
}
