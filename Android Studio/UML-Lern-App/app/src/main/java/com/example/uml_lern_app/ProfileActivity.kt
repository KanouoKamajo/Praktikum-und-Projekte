package com.example.uml_lern_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject



/**
 * Zeigt die Profildaten des aktuell eingeloggten Users (users/{uid}).
 * Verwendet View Binding + Firebase Auth + Firestore (KTX).
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding initialisieren
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Eingeloggt?
        val uid = auth.currentUser?.uid
        if (uid == null) {
            binding.tvStatus.text = "Nicht eingeloggt – bitte zuerst anmelden."
            return
        }

        // User-Dokument laden
        binding.tvStatus.text = ""
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                // nutzt das KTX-Extension toObject<T>() -> benötigt Import s.o.
                val user = snap.toObject<UserActivity>()
                if (user == null) {
                    binding.tvStatus.text = "Kein User-Dokument gefunden."
                    return@addOnSuccessListener
                }

                binding.tvDisplayName.text = user.displayName
                //binding.tvEmail.text = user.email
                binding.tvLevel.text = "Level: ${user.level}"
                binding.tvPoints.text = "Punkte: ${user.points}"
            }
            .addOnFailureListener { e ->
                binding.tvStatus.text = "Fehler beim Laden: ${e.message}"
            }
    }
}
