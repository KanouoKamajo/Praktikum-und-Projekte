package com.example.uml_lern_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityOnboardingStep2Binding


class OnboardingStep2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingStep2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pointsList.layoutAnimation =
            android.view.animation.AnimationUtils.loadLayoutAnimation(this, R.anim.layout_slide_fade_in)
        binding.pointsList.scheduleLayoutAnimation()



        // 🔙 Zurück zur ersten Onboarding-Seite
        binding.btnBack.setOnClickListener {
            finish() // beendet Seite 2 → automatisch zurück zu Seite 1
        }

        // ▶️ Weiter → öffnet Kursübersicht
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, CourseActivity::class.java)
            startActivity(intent)
            finish() // optional: schließt OnboardingStep2, damit man nicht zurückkommt
        }


    }
}
