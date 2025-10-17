package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

/**
 * Home-Screen nach erfolgreichem Login:
 *  - zeigt Begrüßung (z. B. E-Mail)
 *  - Button „Weiterarbeiten“ -> öffnet z. B. Kursübersicht
 *  - Button „Logout“ -> abmelden & zurück zum Login
 */
class HomeActivity : AppCompatActivity() {

    // Firebase Auth zum Verwalten der Anmeldung
    private val auth by lazy { FirebaseAuth.getInstance() }

    // ViewBinding für activity_home.xml
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding-Objekt erzeugen und Layout setzen
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Begrüßungstext mit aktuellem Nutzer anzeigen
        val email = auth.currentUser?.email ?: "Gast"
        binding.tvWelcome.text = "Willkommen, $email"

        // Button: Weiterarbeiten
        binding.btnContinue.setOnClickListener {
            goToNextScreen()
        }

        // Button: Logout
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    /**
     * Öffnet die nächste Seite (z. B. Kursübersicht oder Hauptmenü)
     * Hier kannst du später deine eigentliche App-Startseite angeben.
     */
    private fun goToNextScreen() {
        // Beispiel: Wir öffnen (noch zu erstellende) CourseActivity
        val intent = Intent(this, CourseActivity::class.java)
        startActivity(intent)
    }

    /**
     * Meldet den Nutzer ab und löscht den Back-Stack.
     * Danach kehrt man zur Login-Seite zurück.
     */
    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java).apply {
            // sorgt dafür, dass HomeActivity nicht im Verlauf bleibt
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}
