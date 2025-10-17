package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Login mit BENUTZERNAME ODER E-MAIL + Passwort.
 *
 * Ablauf:
 *  1) Nutzer tippt username/email + password ein
 *  2) Falls username: Suche in Firestore (users.usernameLowercase) → ermittle E-Mail
 *  3) Sign-in via FirebaseAuth mit E-Mail + Passwort
 *
 * Extras:
 *  - "Als Gast fortfahren" (anonymous sign-in)
 *  - Auto-Weiterleitung, wenn schon eingeloggt (onStart)
 *  - Nach JEDEM erfolgreichen Login direkt zur OnboardingActivity
 *  - Intent-Flags leeren Backstack (Login nicht mehr sichtbar)
 */
class LoginActivity : AppCompatActivity() {

    // ViewBinding: stellt typsichere Referenzen auf die Views bereit
    private lateinit var binding: ActivityLoginBinding

    // Firebase: Auth + Firestore
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Layout setzen (activity_login.xml)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) LOGIN-Button: username/email → email auflösen → signIn
        binding.btnLogin.setOnClickListener {
            val usernameOrEmail = binding.etUsername.text?.toString()?.trim().orEmpty()
            val password       = binding.etPassword.text?.toString().orEmpty()

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                toast("Bitte Benutzername/E-Mail und Passwort eingeben.")
                return@setOnClickListener
            }

            setLoading(true) // UI sperren + Progress zeigen

            // E-Mail ermitteln (falls Eingabe nur Benutzername war)
            lookupEmailByUsernameOrEmail(usernameOrEmail) { email, error ->
                if (error != null) {
                    setLoading(false)
                    toast(error)
                    return@lookupEmailByUsernameOrEmail
                }
                if (email == null) {
                    setLoading(false)
                    toast("Benutzername nicht gefunden.")
                    return@lookupEmailByUsernameOrEmail
                }

                // Mit E-Mail + Passwort bei Firebase anmelden
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        setLoading(false)
                        if (task.isSuccessful) {
                            // ✅ NEU: immer zur ONBOARDING-Seite
                            goToOnboarding()
                        } else {
                            toast(task.exception?.localizedMessage ?: "Login fehlgeschlagen.")
                        }
                    }
            }
        }

        // 3) GAST-Login: anonyme Session (praktisch zum Testen)
        binding.btnGuest.setOnClickListener {
            setLoading(true)
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        // ✅ NEU: auch Gast direkt zur ONBOARDING-Seite
                        goToOnboarding()
                    } else {
                        toast(task.exception?.localizedMessage ?: "Gast-Login fehlgeschlagen.")
                    }
                }
        }

        // 4) Link: "Noch kein Konto? Jetzt registrieren" → RegisterActivity öffnen
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Auto-Weiterleitung:
     * Ist bereits jemand eingeloggt, zeigen wir die Login-Seite nicht erneut,
     * sondern springen DIREKT zur OnboardingActivity.
     */
   /* override fun onStart() {
        super.onStart()
        auth.currentUser?.let {
            goToOnboarding()
        }
    }*/

    /**
     * Hilfsfunktion:
     * Löst eine Eingabe (username oder email) zu einer E-Mail auf.
     * - Wenn '@' enthalten ist → direkte E-Mail
     * - Sonst Suche in Firestore: users.usernameLowercase == lower(input)
     */
    private fun lookupEmailByUsernameOrEmail(
        input: String,
        cb: (String?, String?) -> Unit
    ) {
        val v = input.trim()
        if (v.contains("@")) { cb(v, null); return } // direkte E-Mail → fertig

        val lower = v.lowercase()
        Firebase.firestore.collection("users")
            .whereEqualTo("usernameLowercase", lower)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val email = snap.documents.firstOrNull()?.getString("email")
                cb(email, null)
            }
            .addOnFailureListener { e ->
                cb(null, e.localizedMessage ?: "Lookup-Fehler")
            }
    }

    /**
     * NEU: Statt HomeActivity jetzt OnboardingActivity starten.
     * Flags:
     *  - NEW_TASK + CLEAR_TASK: leert den Backstack (Login nicht mehr via Zurück erreichbar).
     */
    private fun goToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish() // Sicherheitsnetz: Activity schließen
    }

    /** UI-Helfer: Eingaben sperren / Progress anzeigen */
    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.btnGuest.isEnabled = !loading
        binding.etUsername.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
    }

    /** UI-Helfer: kurze Toast-Meldung */
    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
