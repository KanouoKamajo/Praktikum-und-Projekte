package com.example.uml_lern_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uml_lern_app.databinding.ActivityQuestionsAdminBinding
import com.example.uml_lern_app.databinding.ItemAdminQuestionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class QuestionsAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionsAdminBinding
    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var courseId: String
    private lateinit var courseTitle: String

    data class QA(
        val id: String = "",
        val text: String = "",
        val options: List<String> = emptyList(),
        val correctIndex: Int = 0
    )

    private val items = mutableListOf<QA>()
    private val adapter = QAAdapter(items) { v, q -> showRowMenu(v, q) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getStringExtra("courseId") ?: ""
        courseTitle = intent.getStringExtra("courseTitle") ?: courseId
        binding.tvTitle.text = "Fragen – $courseTitle"

        binding.rvQuestions.layoutManager = LinearLayoutManager(this)
        binding.rvQuestions.adapter = adapter

        binding.btnAdd.setOnClickListener { showAddEditDialog(null) }
        binding.btnBack.setOnClickListener { finish() }

        subscribeQuestions()
    }

    private fun subscribeQuestions() {
        showLoading(true)
        db.collection("courses").document(courseId)
            .collection("questions")
            .orderBy("text", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                showLoading(false)
                if (err != null) {
                    Toast.makeText(this, "Laden fehlgeschlagen: ${err.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                items.clear()
                snap?.documents?.forEach { d ->
                    items += QA(
                        id = d.id,
                        text = d.getString("text") ?: "",
                        options = (d.get("options") as? List<*>)?.map { it.toString() } ?: emptyList(),
                        correctIndex = (d.getLong("correctIndex") ?: 0L).toInt()
                    )
                }
                adapter.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun showRowMenu(anchor: View, qa: QA) {
        PopupMenu(this, anchor).apply {
            menu.add(0, 1, 0, "Bearbeiten")
            menu.add(0, 2, 1, "Löschen")
            setOnMenuItemClickListener { mi: MenuItem ->
                when (mi.itemId) {
                    1 -> showAddEditDialog(qa)
                    2 -> deleteQuestion(qa.id)
                }
                true
            }
            show()
        }
    }

    private fun deleteQuestion(id: String) {
        db.collection("courses").document(courseId)
            .collection("questions").document(id)
            .delete()
            .addOnSuccessListener { Toast.makeText(this, "Gelöscht", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e -> Toast.makeText(this, "Fehler: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    private fun showAddEditDialog(existing: QA?) {
        val etText = EditText(this).apply {
            hint = "Fragetext"
            setText(existing?.text ?: "")
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        val etA = EditText(this).apply { hint = "Option 1"; setText(existing?.options?.getOrNull(0) ?: "") }
        val etB = EditText(this).apply { hint = "Option 2"; setText(existing?.options?.getOrNull(1) ?: "") }
        val etC = EditText(this).apply { hint = "Option 3 (optional)"; setText(existing?.options?.getOrNull(2) ?: "") }
        val etD = EditText(this).apply { hint = "Option 4 (optional)"; setText(existing?.options?.getOrNull(3) ?: "") }
        val etCorrect = EditText(this).apply {
            hint = "Richtige Option (1-4)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText((existing?.correctIndex?.plus(1) ?: 1).toString())
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 12, 24, 0)
            addView(etText); addView(etA); addView(etB); addView(etC); addView(etD); addView(etCorrect)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (existing == null) "Frage hinzufügen" else "Frage bearbeiten")
            .setView(box)
            .setPositiveButton("Speichern") { _, _ ->
                val qtext = etText.text.toString().trim()
                val opts = listOf(etA, etB, etC, etD)
                    .map { it.text.toString().trim() }
                    .filter { it.isNotEmpty() }
                val idx = (etCorrect.text.toString().toIntOrNull() ?: 1) - 1

                if (qtext.isEmpty() || opts.size < 2) {
                    Toast.makeText(this, "Mind. 2 Optionen & Fragetext nötig", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (idx !in opts.indices) {
                    Toast.makeText(this, "Index 1..${opts.size}", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val data = mapOf("text" to qtext, "options" to opts, "correctIndex" to idx)
                val ref = db.collection("courses").document(courseId).collection("questions")
                val task = if (existing == null) ref.add(data)
                else ref.document(existing.id).set(data)
                task.addOnSuccessListener {
                    Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Fehler: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    // ---- Adapter ----
    private class QAAdapter(
        private val data: MutableList<QA>,
        private val onMore: (View, QA) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<QAAdapter.VH>() {

        inner class VH(val vb: ItemAdminQuestionBinding)
            : androidx.recyclerview.widget.RecyclerView.ViewHolder(vb.root)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val vb = ItemAdminQuestionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VH(vb)
        }

        override fun onBindViewHolder(h: VH, i: Int) {
            val q = data[i]
            h.vb.tvTitle.text = q.text
            val optCount = q.options.size
            val correctStr = if (q.correctIndex in q.options.indices) q.options[q.correctIndex] else "-"
            h.vb.tvSubtitle.text = "$optCount Optionen • richtig: ${q.correctIndex + 1} ($correctStr)"
            h.vb.btnMore.setOnClickListener { onMore(it, q) }
        }

        override fun getItemCount(): Int = data.size
    }
}
