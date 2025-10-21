package com.example.uml_lern_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // vorfüllen
        val user = auth.currentUser
        if (!user?.displayName.isNullOrBlank()) {
            binding.etUsername.setText(user!!.displayName)
        } else if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    binding.etUsername.setText(doc.getString("username") ?: "")
                }
        }

        binding.btnCancel.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            val name = binding.etUsername.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Bitte Benutzername eingeben", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveName(name)
        }
    }

    private fun saveName(name: String) {
        val user = auth.currentUser ?: run {
            Toast.makeText(this, "Nicht eingeloggt", Toast.LENGTH_SHORT).show()
            return
        }

        // 1) FirebaseAuth displayName
        val upd = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(upd).addOnCompleteListener {
            user.reload()
        }

        // 2) Firestore users/{uid}.username
        db.collection("users").document(user.uid)
            .set(mapOf("username" to name), SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fehler: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
