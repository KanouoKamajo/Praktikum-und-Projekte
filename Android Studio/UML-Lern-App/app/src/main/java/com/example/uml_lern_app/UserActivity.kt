package com.example.uml_lern_app

/**
 * Repräsentiert ein Dokument in "users/{uid}".
 * Rolle: "learner" oder "admin".
 */
data class UserActivity(
    val uid: String = "",
    val displayName: String = "",
    val password: String = "",
    val role: String = "learner",
    val level: Int = 1,
    val points: Int = 0,
)
