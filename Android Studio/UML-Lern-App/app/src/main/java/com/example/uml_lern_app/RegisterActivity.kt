package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.jvm.java

/**
 * RegisterActivity
 *
 * Registriert Nutzer mit:
 *   - Benutzername  (z. B. "Junior")
 *   - Passwort      (>= 6 Zeichen)
 *
 * WICHTIG:
 *  - Wir erzeugen eine synthetische E-Mail aus dem Benutzernamen
 *    (z. B. "junior@umlapp.local"), damit wir FirebaseAuth (E-Mail/Passwort) nutzen können.
 *  - Wir speichern in /users/{uid} u. a. "usernameLowercase" = username.lowercase()
 *    Damit der Login per Benutzername zuverlässig funktioniert (case-insensitive).
 *
 * NACH REGISTRIERUNG:
 *  - Direkt zur OnboardingActivity (Back-Stack wird geleert).
 */
class RegisterActivity : AppCompatActivity() {

    // ViewBinding für activity_register.xml
    private lateinit var binding: ActivityRegisterBinding

    // Firebase: Auth & Firestore (Lazy = erst beim Zugriff erzeugt)
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Layout aufblasen
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) "Schon ein Konto? Jetzt anmelden!" -> zurück zur Login-Seite
        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 3) "Konto erstellen" -> Registrierung starten
        //    (Achte darauf, dass der Button in XML "btnCreateAccount" heißt.)
        binding.btnCreateAccount.setOnClickListener { registerWithUsername() }
        // Falls dein XML-Button noch "btnLogin" heißt, könntest du alternativ:
        // binding.btnLogin.setOnClickListener { registerWithUsername() }
    }

    /**
     * Liest Eingaben, prüft Basics und baut die synthetische E-Mail.
     * Ruft danach den eigentlichen Registrierungsvorgang.
     */
    private fun registerWithUsername() {
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        // -- Basiskontrollen: kurze, klare Rückmeldungen --
        if (username.isEmpty()) {
            showStatus("Bitte Benutzernamen eingeben.")
            return
        }
        if (password.length < 6) {
            showStatus("Passwort muss mind. 6 Zeichen haben.")
            return
        }

        // Synthetische E-Mail aus Username (lowercase, stabil für Auth)
        val email = "${username.lowercase()}@umlapp.local"

        // UI sperren + Progress anzeigen
        setLoading(true)

        // Registrierung starten
        registerUser(username, email, password)
    }

    /**
     * Legt den Auth-Account an und speichert das Userprofil in Firestore.
     * Bei Erfolg: direkt zur OnboardingActivity.
     */
    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    setLoading(false)
                    showStatus("Registrierung fehlgeschlagen: ${task.exception?.message}")
                    return@addOnCompleteListener
                }

                // Aktuelle UID holen (sollte nach createUser.. vorhanden sein)
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    setLoading(false)
                    showStatus("Registrierung fehlgeschlagen (keine UID).")
                    return@addOnCompleteListener
                }

                // Firestore-Dokument vorbereiten
                val userDoc = mapOf(
                    "uid"               to uid,
                    "email"             to email,
                    "username"          to username,
                    "usernameLowercase" to username.lowercase(),   // ✅ wichtig für Login mit Benutzername
                    "displayName"       to username,
                    "role"              to "user",                 // passt zu deinen Rules
                    "level"             to 1,
                    "points"            to 0,
                    "createdAt"         to FieldValue.serverTimestamp(),
                    "lastSeenAt"        to FieldValue.serverTimestamp()
                )

                // In /users/{uid} speichern
                db.collection("users").document(uid).set(userDoc)
                    .addOnSuccessListener {
                        // UI wieder aktivieren
                        setLoading(false)
                        showStatus("Registrierung erfolgreich.", success = true)

                        // ✅ NEU: Direkt zur OnboardingActivity
                        startActivity(
                            Intent(this, OnboardingActivity::class.java).apply {
                                // Back-Stack leeren: Login/Register nicht mehr per "Zurück" erreichbar
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                        )
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // Falls Speichern fehlschlägt: Auth-User entfernen (keine „Leichen“)
                        auth.currentUser?.delete()
                        setLoading(false)
                        showStatus("Fehler beim Speichern des Profils: ${e.message}")
                    }
            }
    }

    /**
     * UI-State umschalten: Eingaben deaktivieren/aktivieren, Progress ein-/ausblenden.
     * Achte darauf, dass in deinem XML eine ProgressBar mit id="@+id/progressBar" existiert.
     */
    private fun setLoading(loading: Boolean) {
        // Eingabeelemente
        binding.etUsername.isEnabled = !loading
        binding.etPassword.isEnabled = !loading

        // Buttons
        binding.btnCreateAccount.isEnabled = !loading
        // Wenn du zusätzlich einen zweiten Button/Link hast, ggf. hier auch deaktivieren

        // Progress sichtbar/unsichtbar
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE

        // Status-Text ausblenden, wenn wir wieder im Idle sind
        if (!loading) binding.tvLoginStatus.visibility = View.GONE
    }

    /**
     * Zeigt Status/Fehler unter dem Formular an (und optional als Toast).
     */
    private fun showStatus(msg: String, success: Boolean = false) {
        binding.tvLoginStatus.apply {
            text = msg
            visibility = View.VISIBLE
        }
        if (!success) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
