package com.example.uml_lern_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityFeedbackBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSend.setOnClickListener {
            val text = binding.etFeedback.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Bitte Feedback eingeben.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val uid = user?.uid ?: "anonymous"
            val name = user?.displayName?.takeIf { it.isNotBlank() } ?: "Gast"

            // Optional: Doppel-Taps vermeiden
            binding.btnSend.isEnabled = false

            // Feedback in Firestore speichern
            val data = mapOf(
                "ownerId" to uid,
                "username" to name,
                "message" to text,
                "createdAt" to Timestamp.now()
            )

            db.collection("feedback").add(data)
                .addOnCompleteListener {
                    // **WICHTIG:** Feld JETZT leeren, damit es bereits leer ist, wenn der Dialog erscheint
                    binding.etFeedback.setText("")
                    showThanks(name)
                    binding.btnSend.isEnabled = true
                }
        }
    }

    private fun showThanks(username: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Danke!")
            .setMessage("Vielen Dank, $username, für Ihre Nachricht.")
            .setPositiveButton("OK") { _, _ ->
                // Dein ursprüngliches Leeren bleibt bestehen – doppelt schadet nicht
                binding.etFeedback.setText("")
                finish()
            }
            .show()
    }
}
