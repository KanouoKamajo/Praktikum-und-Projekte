package com.example.uml_lern_app

import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityQuizBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    // Fragen-Logik
    private var fullPool: List<QuizQuestion> = emptyList()  // kompletter Pool (für courseId)
    private var questions: MutableList<QuizQuestion> = mutableListOf() // eigentliche Session-Fragen
    private var currentIndex = 0
    private var score = 0

    // Keys
    private val STATE_INDEX = "state_index"
    private val STATE_SCORE = "state_score"

    // Persistence
    private val PREFS = "quiz_prefs"
    private val KEY_WRONG_PREFIX = "wrong_"            // + courseId → JSON-Array von Frage-Strings
    private val KEY_PROFILE_POINTS = "profile_points"  // Gesamtpunkte

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId") ?: ""
        val quizTitle = intent.getStringExtra("quizTitle") ?: titleForCourse(courseId)
        binding.tvQuizTitle.text = quizTitle

        // 1) Vollständigen Fragenpool laden
        fullPool = loadQuestions(courseId)

        // 2) Falsche Fragen aus vorheriger Session laden (werden zuerst geübt)
        val previouslyWrong = loadWrongSet(courseId)   // Set<String> (Fragetexte)

        // 3) gewünschte Anzahl: per Intent oder später per Dialog (Text-Eingabe)
        val requestedCount = intent.getIntExtra("questionCount", -1)

        if (savedInstanceState == null) {
            // Session-Fragen vorbereiten (zuerst falsche, dann Rest auffüllen)
            questions = buildSessionQuestions(previouslyWrong, fullPool)

            if (requestedCount in 1..questions.size) {
                // Wenn eine gültige Anzahl vorgegeben wurde → auf diese Anzahl begrenzen
                questions = questions.take(requestedCount).toMutableList()
                showQuestion()
            } else {
                // Dialog mit Textfeld: User tippt die Anzahl (statt Auswahl)
                promptForQuestionCount(questions.size) { chosen ->
                    questions = questions.take(chosen).toMutableList()
                    showQuestion()
                }
            }
        } else {
            currentIndex = savedInstanceState.getInt(STATE_INDEX, 0)
            score = savedInstanceState.getInt(STATE_SCORE, 0)
            // Für „ohne große Änderungen“: baue Session erneut (Reihenfolge kann variieren bei Rotation)
            questions = buildSessionQuestions(previouslyWrong, fullPool)
            if (requestedCount in 1..questions.size) {
                questions = questions.take(requestedCount).toMutableList()
            }
            showQuestion()
        }

        // Weiter/Ergebnis
        binding.btnNext.setOnClickListener {
            val checkedId = binding.rgOptions.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(this, "Bitte eine Antwort wählen!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIndex = binding.rgOptions.indexOfChild(findViewById<RadioButton>(checkedId))
            val currentQ = questions[currentIndex]

            // Treffer werten + falsche Frage persistieren/entfernen
            val correct = (selectedIndex == currentQ.correctIndex)
            if (correct) {
                score++
                removeWrong(currentQ.question, courseId) // war evtl. vorher falsch gespeichert → entfernen
            } else {
                saveWrong(currentQ.question, courseId)   // als falsch merken
            }

            currentIndex++
            if (currentIndex < questions.size) showQuestion() else showResult(courseId)
        }

        // Zurück vom Ergebnis
        binding.btnBack.setOnClickListener {
            finish() // zurück zur Unit-Seite
        }
    }

    /**
     * Erstellt die Session-Fragen:
     * - zuerst alle zuvor falsch beantworteten (falls noch im Pool vorhanden),
     * - dann die restlichen gemischt.
     */
    private fun buildSessionQuestions(previouslyWrong: Set<String>, pool: List<QuizQuestion>): MutableList<QuizQuestion> {
        val wrongFirst = pool.filter { it.question in previouslyWrong }
        val rest = pool.filter { it.question !in previouslyWrong }.shuffled()
        return (wrongFirst + rest).toMutableList()
    }

    /**
     * Dialog mit Textfeld (Zahl) – User gibt die gewünschte Anzahl an.
     * Begrenzung: 1..maxCount
     */
    private fun promptForQuestionCount(maxCount: Int, onChosen: (Int) -> Unit) {
        // TextInput programmatisch erstellen (kein extra XML nötig)
        val til = TextInputLayout(this).apply {
            isHintEnabled = true
            hint = "Anzahl Fragen (1–$maxCount)"
        }
        val et = TextInputEditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(minOf(5, maxCount).toString()) // Vorschlag: 5 oder weniger
        }
        til.addView(et)

        MaterialAlertDialogBuilder(this)
            .setTitle("Anzahl der Fragen")
            .setView(til)
            .setPositiveButton("OK") { _, _ ->
                val raw = et.text?.toString()?.trim().orEmpty()
                val n = raw.toIntOrNull() ?: maxCount
                val clamped = n.coerceIn(1, maxCount)
                onChosen(clamped)
            }
            .setNegativeButton("Alle") { _, _ ->
                onChosen(maxCount)
            }
            .setCancelable(false)
            .show()
    }

    /** Zeigt aktuelle Frage + aktualisiert ProgressBar. */
    private fun showQuestion() {
        val q = questions[currentIndex]
        binding.tvQuestion.text = q.question
        binding.tvProgress.text = "Frage ${currentIndex + 1} / ${questions.size}"

        // ProgressBar: 0..100
        val progress = ((currentIndex) * 100f / questions.size).toInt()
        binding.progressBar.progress = progress

        binding.rgOptions.removeAllViews()
        q.options.forEach { opt ->
            val rb = RadioButton(this).apply {
                text = opt
                textSize = 16f
                setPadding(8, 12, 8, 12)
            }
            binding.rgOptions.addView(rb)
        }
        binding.tvResult.text = ""
        binding.rgOptions.clearCheck()
        binding.btnNext.text = if (currentIndex == questions.size - 1) "Ergebnis anzeigen" else "Weiter"
        binding.btnBack.visibility = android.view.View.GONE
    }

    /** Ergebnis anzeigen, Punkte speichern, ProgressBar auf 100 setzen, Zurück-Button zeigen. */
    private fun showResult(courseId: String) {
        binding.tvQuestion.text = "Ergebnis"
        binding.rgOptions.removeAllViews()
        binding.btnNext.isEnabled = false
        binding.btnNext.text = "Fertig"
        binding.tvProgress.text = ""
        binding.progressBar.progress = 100

        binding.tvResult.text = "Sie haben $score von ${questions.size} richtig beantwortet."

        // Punkte im „Profil“ addieren (einfaches Beispiel: Summe aller erzielten Punkte)
        addProfilePoints(score)

        // „Zurück“-Button sichtbar machen
        binding.btnBack.visibility = android.view.View.VISIBLE
    }

    // ---------------------- Persistence: falsche Fragen je Kurs ----------------------

    private fun prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Lädt Set der falsch beantworteten Fragen (nach Fragetext) für einen Kurs. */
    private fun loadWrongSet(courseId: String): Set<String> {
        val key = KEY_WRONG_PREFIX + courseId
        val json = prefs().getString(key, "[]") ?: "[]"
        val arr = JSONArray(json)
        val set = mutableSetOf<String>()
        for (i in 0 until arr.length()) set += arr.optString(i)
        return set
    }

    /** Speichert/mergt eine falsche Frage in die Liste. */
    private fun saveWrong(questionText: String, courseId: String) {
        val key = KEY_WRONG_PREFIX + courseId
        val set = loadWrongSet(courseId).toMutableSet()
        set += questionText
        val arr = JSONArray()
        set.forEach { arr.put(it) }
        prefs().edit().putString(key, arr.toString()).apply()
    }

    /** Entfernt eine Frage aus der „falsch“-Liste (wenn sie korrekt beantwortet wurde). */
    private fun removeWrong(questionText: String, courseId: String) {
        val key = KEY_WRONG_PREFIX + courseId
        val set = loadWrongSet(courseId).toMutableSet()
        if (set.remove(questionText)) {
            val arr = JSONArray()
            set.forEach { arr.put(it) }
            prefs().edit().putString(key, arr.toString()).apply()
        }
    }

    // ---------------------- Punkte im „Profil“ (SharedPreferences) ----------------------

    private fun addProfilePoints(delta: Int) {
        val current = prefs().getInt(KEY_PROFILE_POINTS, 0)
        prefs().edit().putInt(KEY_PROFILE_POINTS, current + delta).apply()
        // Optional: Toast als Feedback
        // Toast.makeText(this, "Punkte gesamt: ${current + delta}", Toast.LENGTH_SHORT).show()
    }

    // ---------------------- Fragen & Titel (wie zuvor) ----------------------

    private fun loadQuestions(courseId: String): List<QuizQuestion> {
        return when (courseId) {
            "uml_basics" -> listOf(
                QuizQuestion("Wofür steht UML?",
                    listOf("Unified Modeling Language","Universal Machine Logic","User Management Layer"), 0),
                QuizQuestion("Welches Diagramm zeigt Klassen und ihre Beziehungen?",
                    listOf("Klassendiagramm","Sequenzdiagramm","Use-Case-Diagramm"), 0),
                QuizQuestion("Was beschreibt ein Use-Case-Diagramm am besten?",
                    listOf("Interaktionen zwischen Akteuren und System","Objektzustände","Paketabhängigkeiten"), 0),
                QuizQuestion("Welche Beziehung drückt Vererbung aus?",
                    listOf("Generalisierung","Aggregation","Assoziation"), 0),
                QuizQuestion("Sequenzdiagramme zeigen…",
                    listOf("Nachrichtenfluss über die Zeit","Paketabhängigkeiten","Zustandsübergänge"), 0)
            )
            "uml_advanced" -> listOf(
                QuizQuestion("Wofür nutzt man Aktivitätsdiagramme?",
                    listOf("Abläufe/Prozesse","Klassenattribute","Netzwerktopologie"), 0),
                QuizQuestion("Zustandsdiagramme modellieren…",
                    listOf("Zustände eines Objekts und Übergänge","Akteure eines Systems","Deployment-Struktur"), 0),
                QuizQuestion("Was ist ein Guard?",
                    listOf("Bedingung für einen Übergang","Akteur im Use-Case","Objektattribut"), 0)
            )
            "uml_practice" -> listOf(
                QuizQuestion("Typischer Praxis-Schritt mit UML?",
                    listOf("Domänenanalyse & Diagramme","Nur Code schreiben","Nur Datenbank anlegen"), 0),
                QuizQuestion("Welches Diagramm eignet sich für Interaktionen?",
                    listOf("Sequenzdiagramm","Klassendiagramm","Paketdiagramm"), 0)
            )
            else -> listOf(
                QuizQuestion("Beispielfrage?",
                    listOf("Antwort A","Antwort B","Antwort C"), 0)
            )
        }
    }

    private fun titleForCourse(courseId: String) = when (courseId) {
        "uml_basics"   -> "Quiz – UML Grundlagen"
        "uml_advanced" -> "Quiz – Fortgeschrittene UML"
        "uml_practice" -> "Quiz – UML in der Praxis"
        else           -> "Quiz"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_INDEX, currentIndex)
        outState.putInt(STATE_SCORE, score)
    }
}
