package com.example.uml_lern_app

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class ErrorListActivity : AppCompatActivity() {

    private lateinit var spCourse: Spinner
    private lateinit var rvErrors: RecyclerView
    private lateinit var btnClearAll: Button

    private val items = mutableListOf<String>()
    private lateinit var adapter: ErrorAdapter

    private val PREFS = "quiz_prefs"
    private val KEY_WRONG_PREFIX = "wrong_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_list)

        spCourse = findViewById(R.id.spCourse)
        rvErrors = findViewById(R.id.rvErrors)
        btnClearAll = findViewById(R.id.btnClearAll)

        // Kurs-Auswahl (IDs wie in deinem Projekt – ggf. anpassen)
        val courses = listOf(
            "uml_basics" to "UML Grundlagen",
            "uml_advanced" to "Fortgeschrittene UML",
            "uml_practice" to "UML in der Praxis"
        )
        spCourse.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            courses.map { it.second }
        )

        adapter = ErrorAdapter(items) { question ->
            val courseId = courses[spCourse.selectedItemPosition].first
            removeWrong(question, courseId)
            loadFor(courseId)
        }
        rvErrors.layoutManager = LinearLayoutManager(this)
        rvErrors.adapter = adapter

        spCourse.setOnItemSelectedListenerCompat { _, _, pos, _ ->
            loadFor(courses[pos].first)
        }

        btnClearAll.setOnClickListener {
            val courseId = courses[spCourse.selectedItemPosition].first
            clearAll(courseId)
            loadFor(courseId)
            Toast.makeText(this, "Fehlerliste geleert", Toast.LENGTH_SHORT).show()
        }
    }

    // ---- SharedPreferences Helfer ----
    private fun prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun loadFor(courseId: String) {
        val json = prefs().getString(KEY_WRONG_PREFIX + courseId, "[]") ?: "[]"
        val arr = JSONArray(json)
        items.clear()
        for (i in 0 until arr.length()) items += arr.optString(i)
        adapter.notifyDataSetChanged()
    }

    private fun removeWrong(question: String, courseId: String) {
        val json = prefs().getString(KEY_WRONG_PREFIX + courseId, "[]") ?: "[]"
        val arr = JSONArray(json)
        val set = mutableSetOf<String>()
        for (i in 0 until arr.length()) set += arr.optString(i)
        if (set.remove(question)) {
            val out = JSONArray()
            set.forEach { out.put(it) }
            prefs().edit().putString(KEY_WRONG_PREFIX + courseId, out.toString()).apply()
        }
    }

    private fun clearAll(courseId: String) {
        prefs().edit().remove(KEY_WRONG_PREFIX + courseId).apply()
    }

    // ---- RecyclerView Adapter ----
    class ErrorAdapter(
        private val data: List<String>,
        private val onRemove: (String) -> Unit
    ) : RecyclerView.Adapter<ErrorAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tv = v.findViewById<android.widget.TextView>(R.id.tvQuestion)
            val btn = v.findViewById<Button>(R.id.btnRemove)
        }

        override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int): VH {
            val v = android.view.LayoutInflater.from(p.context)
                .inflate(R.layout.item_error, p, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, i: Int) {
            val q = data[i]
            h.tv.text = q
            h.btn.setOnClickListener { onRemove(q) }
        }

        override fun getItemCount() = data.size
    }

    // ---- Spinner Extension (kompakt) ----
    private fun Spinner.setOnItemSelectedListenerCompat(
        onItemSelected: (Spinner, View?, Int, Long) -> Unit
    ) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long
            ) { onItemSelected(this@setOnItemSelectedListenerCompat, view, position, id) }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }
}
