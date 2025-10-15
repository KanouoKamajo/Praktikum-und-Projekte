package com.example.uml_lern_app

// Repräsentiert einen Kurs aus Firestore.
// Felder sind bewusst einfach gehalten.
data class Course(
    val id: String,
    val title: String
)
