package com.example.uml_lern_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityQuizBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import kotlin.math.max

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    // Fragen-Logik
    private var fullPool: List<QuizQuestion> = emptyList()
    private var questions: MutableList<QuizQuestion> = mutableListOf()
    private var currentIndex = 0
    private var score = 0
    private var isResultMode = false

    // Punkte-/Level-Logik
    private val POINTS_PER_CORRECT = 10
    private val PASS_THRESHOLD = 0.70f

    // --- TIMER (gesamt fürs Quiz) ---
    private val QUIZ_TOTAL_MS = 5 * 60 * 1000L        // 5 Minuten
    private var remainingMs: Long = QUIZ_TOTAL_MS
    private var countDown: CountDownTimer? = null
    private val STATE_REMAINING = "state_remaining"

    // State Keys
    private val STATE_INDEX = "state_index"
    private val STATE_SCORE = "state_score"

    // Persistence
    private val PREFS = "quiz_prefs"
    private val KEY_WRONG_PREFIX = "wrong_"
    private val KEY_PROFILE_POINTS = "profile_points"
    private val KEY_PASSED_PREFIX = "passed_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId") ?: ""
        val unitId   = intent.getStringExtra("unitId")
        val quizTitle = intent.getStringExtra("quizTitle") ?: titleForCourse(courseId)
        binding.tvQuizTitle.text = quizTitle

        // Timer-Status initialisieren (zeigt sofort 05:00 o. Restzeit)
        remainingMs = savedInstanceState?.getLong(STATE_REMAINING, QUIZ_TOTAL_MS) ?: QUIZ_TOTAL_MS
        updateTimerText(remainingMs)

        // 1) Vollständigen Fragenpool laden
        fullPool = loadQuestions(courseId)

        // 2) Falsche zuerst
        val previouslyWrong = loadWrongSet(courseId)

        // 3) gewünschte Anzahl (0/negativ = keine Vorgabe)
        val requestedCount = intent.getIntExtra("questionCount", 0)

        // Session vorbereiten (falsche zuerst, dann Rest gemischt)
        questions = buildSessionQuestions(previouslyWrong, fullPool)

        if (savedInstanceState == null) {
            // Frisch gestartet → evtl. Anzahl fragen
            maybeAskForCount(requestedCount, questions.size) {
                questions = questions.take(it).toMutableList()
                showQuestion()
            }
        } else {
            // Zustand wiederherstellen
            currentIndex = savedInstanceState.getInt(STATE_INDEX, 0)
            score = savedInstanceState.getInt(STATE_SCORE, 0)

            // Auch hier ggf. Anzahl anwenden/abfragen
            maybeAskForCount(requestedCount, questions.size) {
                questions = questions.take(it).toMutableList()
                if (currentIndex >= questions.size) currentIndex = 0
                showQuestion()
            }
        }

        // Weiter / Fertig
        binding.btnNext.setOnClickListener {
            if (isResultMode) {
                startActivity(
                    Intent(this, CourseActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("courseId", courseId)
                    }
                )
                finish()
                return@setOnClickListener
            }

            val checkedId = binding.rgOptions.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(this, "Bitte eine Antwort wählen!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIndex = binding.rgOptions.indexOfChild(findViewById<RadioButton>(checkedId))
            val currentQ = questions[currentIndex]

            val correct = (selectedIndex == currentQ.correctIndex)
            if (correct) {
                score++
                addProfilePoints(POINTS_PER_CORRECT)
                removeWrong(currentQ.question, courseId)
            } else {
                saveWrong(currentQ.question, courseId)
            }

            currentIndex++
            if (currentIndex < questions.size) showQuestion() else showResult(courseId, unitId)
        }

        // Zurück
        binding.btnBack.setOnClickListener { finish() }
    }

    // Timer lifecycle
    override fun onResume() {
        super.onResume()
        if (!isResultMode && remainingMs > 0L) startTimer()
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_INDEX, currentIndex)
        outState.putInt(STATE_SCORE, score)
        outState.putLong(STATE_REMAINING, remainingMs)
    }

    // ---------- Timer Helpers ----------
    private fun startTimer() {
        stopTimer()
        countDown = object : CountDownTimer(remainingMs, 1000L) {
            override fun onTick(msLeft: Long) {
                remainingMs = max(0L, msLeft)
                updateTimerText(remainingMs)
            }
            override fun onFinish() {
                remainingMs = 0L
                updateTimerText(0L)
                if (!isResultMode) {
                    Toast.makeText(this@QuizActivity, "Zeit abgelaufen! Ergebnis wird angezeigt.", Toast.LENGTH_LONG).show()
                    val cId = intent.getStringExtra("courseId") ?: ""
                    val uId = intent.getStringExtra("unitId")
                    showResult(cId, uId)
                }
            }
        }.start()
    }

    private fun stopTimer() {
        countDown?.cancel()
        countDown = null
    }

    private fun updateTimerText(ms: Long) {
        val totalSec = (ms / 1000L).toInt()
        val mm = totalSec / 60
        val ss = totalSec % 60
        // falls tvTimer nicht im Layout vorhanden ist, crasht es nicht
        binding.tvTimer?.text = String.format("%02d:%02d", mm, ss)
    }

    /** Fragt ggf. die Anzahl ab und ruft dann onChosen(validierte_Anzahl). */
    private fun maybeAskForCount(requestedCount: Int, maxCount: Int, onChosen: (Int) -> Unit) {
        if (maxCount <= 0) {
            Toast.makeText(this, "Keine Fragen verfügbar.", Toast.LENGTH_LONG).show()
            return
        }
        if (requestedCount in 1..maxCount) {
            onChosen(requestedCount)
            return
        }
        promptForQuestionCount(maxCount, onChosen)
    }

    /** Session: zuerst falsch beantwortete (falls vorhanden), dann Rest gemischt. */
    private fun buildSessionQuestions(previouslyWrong: Set<String>, pool: List<QuizQuestion>): MutableList<QuizQuestion> {
        val wrongFirst = pool.filter { it.question in previouslyWrong }
        val rest = pool.filter { it.question !in previouslyWrong }.shuffled()
        return (wrongFirst + rest).toMutableList()
    }

    /** Dialog: Anzahl Fragen. */
    private fun promptForQuestionCount(maxCount: Int, onChosen: (Int) -> Unit) {
        val til = TextInputLayout(this).apply {
            isHintEnabled = true
            hint = "Anzahl Fragen (1–$maxCount)"
        }
        val et = TextInputEditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(minOf(5, maxCount).toString())
        }
        til.addView(et)

        MaterialAlertDialogBuilder(this)
            .setTitle("Anzahl der Fragen")
            .setView(til)
            .setPositiveButton("OK") { _, _ ->
                val raw = et.text?.toString()?.trim().orEmpty()
                val n = raw.toIntOrNull() ?: maxCount
                onChosen(n.coerceIn(1, maxCount))
            }
            .setNegativeButton("Alle") { _, _ -> onChosen(maxCount) }
            .setCancelable(false)
            .show()
    }

    /** Aktuelle Frage anzeigen. */
    private fun showQuestion() {
        isResultMode = false

        val q = questions[currentIndex]
        binding.tvQuestion.text = q.question
        binding.tvProgress.text = "Frage ${currentIndex + 1} / ${questions.size}"

        val progress = ((currentIndex) * 100f / questions.size).toInt()
        binding.progressBar.progress = progress

        binding.rgOptions.removeAllViews()
        q.options.forEach { opt ->
            val rb = android.widget.RadioButton(this).apply {
                text = opt
                textSize = 16f
                setPadding(8, 12, 8, 12)
            }
            binding.rgOptions.addView(rb)
        }
        binding.tvResult.text = ""
        binding.rgOptions.clearCheck()
        binding.btnNext.text = if (currentIndex == questions.size - 1) "Ergebnis anzeigen" else "Weiter"
        binding.btnBack.visibility = View.GONE
        binding.btnNext.isEnabled = true
        binding.btnErrorList.visibility = View.GONE

        // sicherstellen, dass Timer sichtbar ist
        binding.tvTimer?.visibility = View.VISIBLE
    }

    /** Ergebnis anzeigen + Unit ggf. als bestanden markieren. */
    private fun showResult(courseId: String, unitId: String?) {
        isResultMode = true
        stopTimer()
        binding.tvTimer?.visibility = View.GONE

        binding.tvQuestion.text = "Ergebnis"
        binding.rgOptions.removeAllViews()
        binding.btnNext.isEnabled = true
        binding.btnNext.text = "Fertig"
        binding.tvProgress.text = ""
        binding.progressBar.progress = 100
        binding.tvResult.text = "Sie haben $score von ${questions.size} richtig beantwortet."

        val ratio = if (questions.isEmpty()) 0f else score.toFloat() / questions.size
        val passed = ratio >= PASS_THRESHOLD
        if (!unitId.isNullOrBlank()) setUnitPassed(unitId, passed)

        binding.btnBack.visibility = View.VISIBLE

        binding.btnErrorList.visibility = View.VISIBLE
        binding.btnErrorList.setOnClickListener {
            startActivity(Intent(this, ErrorListActivity::class.java))
        }
    }

    // ---------------------- SharedPreferences helpers ----------------------
    private fun prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun loadWrongSet(courseId: String): Set<String> {
        val key = KEY_WRONG_PREFIX + courseId
        val json = prefs().getString(key, "[]") ?: "[]"
        val arr = JSONArray(json)
        val set = mutableSetOf<String>()
        for (i in 0 until arr.length()) set += arr.optString(i)
        return set
    }

    private fun saveWrong(questionText: String, courseId: String) {
        val key = KEY_WRONG_PREFIX + courseId
        val set = loadWrongSet(courseId).toMutableSet()
        set += questionText
        val arr = JSONArray()
        set.forEach { arr.put(it) }
        prefs().edit().putString(key, arr.toString()).apply()
    }

    private fun removeWrong(questionText: String, courseId: String) {
        val key = KEY_WRONG_PREFIX + courseId
        val set = loadWrongSet(courseId).toMutableSet()
        if (set.remove(questionText)) {
            val arr = JSONArray()
            set.forEach { arr.put(it) }
            prefs().edit().putString(key, arr.toString()).apply()
        }
    }

    private fun addProfilePoints(delta: Int) {
        val current = prefs().getInt(KEY_PROFILE_POINTS, 0)
        prefs().edit().putInt(KEY_PROFILE_POINTS, current + delta).apply()
    }

    private fun setUnitPassed(unitId: String, passed: Boolean) {
        prefs().edit().putBoolean(KEY_PASSED_PREFIX + unitId, passed).apply()
    }

    // ---------------------- Fragen & Titel ----------------------
    private fun loadQuestions(courseId: String): List<QuizQuestion> = when (courseId) {
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

    private fun titleForCourse(courseId: String) = when (courseId) {
        "uml_basics"   -> "Quiz – UML Grundlagen"
        "uml_advanced" -> "Quiz – Fortgeschrittene UML"
        "uml_practice" -> "Quiz – UML in der Praxis"
        else           -> "Quiz"
    }
}
