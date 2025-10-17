package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityOnboardingBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * 🟥 OnboardingActivity (Schritt 1)
 * ---------------------------------------------------------
 * Diese Seite begrüßt neue Benutzer mit einer Einführung.
 * Buttons:
 *   🔹 Weiter        → führt zu OnboardingStep2Activity (Erklärung der App)
 *   🔹 Überspringen  → führt direkt zur CourseActivity
 *   🔹 Logout        → meldet Benutzer ab und führt zurück zur LoginActivity
 */
class OnboardingActivity : AppCompatActivity() {

    // ViewBinding: Zugriff auf alle UI-Elemente aus activity_onboarding.xml
    private lateinit var binding: ActivityOnboardingBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // -----------------------------------------------------------
        // 🧭 Navigation: Buttons konfigurieren
        // -----------------------------------------------------------
// ▶️ "Weiter" → öffnet zweite Onboarding-Seite
        binding.btnWeiter.setOnClickListener {
            android.util.Log.d("ONB", "Weiter geklickt")          // Sichtbar in Logcat
            android.widget.Toast.makeText(this, "Weiter…", android.widget.Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, OnboardingStep2Activity::class.java))
        }


        // ⏩ "Überspringen" → öffnet direkt die Kursseite
        binding.tvSkip.setOnClickListener {
            val intent = Intent(this, CourseActivity::class.java)
            startActivity(intent)
            //finish() // beendet Onboarding

        }

        // 3️⃣ Logout → zurück zur Login-Seite und Benutzer abmelden
        binding.btnLogout.setOnClickListener {
            auth.signOut() // Firebase-Nutzer abmelden
            val intent = Intent(this, LoginActivity::class.java).apply {
                // Flags löschen, damit Login die neue Startseite ist
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        }
    }

    // -----------------------------------------------------------
    // Falls der Benutzer ausgeloggt ist → automatisch zur Login-Seite
    // -----------------------------------------------------------
    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
