package com.example.uml_lern_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uml_lern_app.databinding.ActivityUnitBinding

class UnitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId")
        binding.tvMaterial.text = "Einführung in UML Diagramme"
        binding.tvDescription.text = "Hier finden Sie Erklärungen, Beispiele und Abbildungen zu UML-Diagrammen."

        binding.btnQuiz.setOnClickListener {
            Toast.makeText(this, "Quiz zu $courseId gestartet!", Toast.LENGTH_SHORT).show()
        }
    }
}
