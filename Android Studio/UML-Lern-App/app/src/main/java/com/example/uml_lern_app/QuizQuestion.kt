package com.example.uml_lern_app

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
