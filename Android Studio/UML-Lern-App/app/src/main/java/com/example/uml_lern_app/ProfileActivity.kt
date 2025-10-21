package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.math.min

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val PREFS = "quiz_prefs"
    private val KEY_PROFILE_POINTS = "profile_points"
    private val KEY_PASSED_PREFIX = "passed_" // + unitId
    private val unitOrder = listOf("uml_basics", "uml_advanced", "uml_practice")
    private val XP_PER_LEVEL = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val user = auth.currentUser

        // Benutzername laden (nur Name, keine E-Mail)
        loadUsername(user)

        // Punkte & Level anzeigen
        val totalPoints = prefs.getInt(KEY_PROFILE_POINTS, 0)
        val passedMap = unitOrder.associateWith { id ->
            prefs.getBoolean(KEY_PASSED_PREFIX + id, false)
        }
        val passedCount = passedMap.values.count { it }
        val currentLevel = passedCount
        val xpInLevel = min(totalPoints % XP_PER_LEVEL, XP_PER_LEVEL)
        val nextLevelXp = XP_PER_LEVEL

        binding.tvLevelLine.text = "Level $currentLevel"
        binding.tvPoints.text = totalPoints.toString()
        binding.tvNextLevelXp.text = nextLevelXp.toString()
        binding.progress.max = XP_PER_LEVEL
        binding.progress.progress = xpInLevel
        binding.tvXpRight.text = "$xpInLevel/$nextLevelXp XP"

        // Buttons
        binding.btnResetProgress.setOnClickListener {
            unitOrder.forEach { prefs.edit().putBoolean(KEY_PASSED_PREFIX + it, false).apply() }
            prefs.edit().putInt(KEY_PROFILE_POINTS, 0).apply()
            Toast.makeText(this, "Fortschritt zurückgesetzt", Toast.LENGTH_SHORT).show()
            recreate()
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.btnFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Abgemeldet", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadUsername(user: com.google.firebase.auth.FirebaseUser?) {
        fun setName(name: String?) {
            binding.tvUsername.text = if (!name.isNullOrBlank()) name else "Gast"
        }
        if (user == null) { setName(null); return }

        if (!user.displayName.isNullOrBlank()) {
            setName(user.displayName)
        } else {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc -> setName(doc.getString("username")) }
                .addOnFailureListener { setName(null) }
        }
    }

    // Optional: falls du später Namen aus dieser Seite ändern willst
    @Suppress("unused")
    private fun updateDisplayName(newName: String) {
        val user = auth.currentUser ?: return
        val upd = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
        user.updateProfile(upd).addOnSuccessListener { user.reload() }
        db.collection("users").document(user.uid)
            .set(mapOf("username" to newName), SetOptions.merge())
    }
}
