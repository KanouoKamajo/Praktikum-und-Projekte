package com.example.uml_lern_app

import com.google.firebase.Timestamp

/**
 * Repräsentiert ein Dokument in "users/{uid}".
 * Rolle: "learner" oder "admin".
 */
data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "learner",
    val level: Int = 1,
    val points: Int = 0,
    val createdAt: Timestamp? = null,
    val lastSeenAt: Timestamp? = null
)
