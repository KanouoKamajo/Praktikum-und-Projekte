package com.example.uml_lern_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ------------------------------------------------------------------
        // Verbindung zur Firestore-Datenbank wird hergestellt.
        // Diese Instanz ermöglicht Lese- und Schreibzugriffe auf Collections.
        // ------------------------------------------------------------------
        val db = FirebaseFirestore.getInstance()

        // ------------------------------------------------------------------
        // Lese-Test: Abfrage aller Dokumente aus der Collection "courses".
        // Die Ergebnisse werden nach dem Zahlenfeld "order" sortiert.
        // Bei Erfolg werden alle Kurse im Log ausgegeben.
        // ------------------------------------------------------------------
        db.collection("courses")
            .orderBy("order")
            .get()
            .addOnSuccessListener { daten ->
                if (daten.isEmpty) {
                    Log.d("COURSE", "Keine Kurse gefunden.")
                } else {
                    for (doc in daten.documents) {
                        val titel = doc.getString("title")
                        Log.d("COURSE", "id=${doc.id} title=$titel")
                    }
                }
            }
            .addOnFailureListener { fehler ->
                Log.e("COURSE", "Fehler beim Lesen", fehler)
            }

        // ------------------------------------------------------------------
        // Schreib-Test: Ein neues Dokument wird in der Collection "attempts"
        // angelegt. Es enthält Basisdaten für einen Kursversuch.
        // Das Dokument erhält automatisch eine eindeutige ID.
        // ------------------------------------------------------------------
        val neuerVersuch = hashMapOf(
            "nutzerId" to "TEST_USER",       // Platzhalter-ID; später echte Benutzer-ID aus Firebase Auth
            "kursId" to "uml-basics",        // Referenz auf Kurs
            "einheitId" to null,             // Referenz auf Unit (wird später ergänzt)
            "modus" to "practice",           // Mögliche Werte: "practice" oder "exam"
            "anzahlFragen" to 0,             // Anzahl gewählter Fragen (Platzhalter)
            "startZeit" to Timestamp.now()   // Aktuelle Uhrzeit im Firestore-Format
        )

        // ------------------------------------------------------------------
        // Das Dokument "neuerVersuch" wird in der Collection "attempts" gespeichert.
        // Bei erfolgreichem Schreiben wird die automatisch erzeugte ID im Log angezeigt.
        // Tritt ein Fehler auf (z. B. wegen Berechtigungen oder Netzwerk),
        // wird dieser im Log ausgegeben.
        // ------------------------------------------------------------------
        db.collection("attempts")
            .add(neuerVersuch)
            .addOnSuccessListener { ref ->
                Log.d("ATTEMPT", "Neuer Versuch angelegt: id=${ref.id}")
            }
            .addOnFailureListener { fehler ->
                Log.e("ATTEMPT", "Fehler beim Schreiben", fehler)
            }

        // ------------------------------------------------------------------
// Lese-Test (Units): Abfrage aller Units eines Kurses.
// Quelle: courses/uml-basics/units
// Ausgabe: Unit-ID und Titel im Log.
// ------------------------------------------------------------------
        FirebaseFirestore.getInstance()
            .collection("courses")
            .document("uml-basics")
            .collection("units")
            .orderBy("order")
            .get()
            .addOnSuccessListener { daten ->
                if (daten.isEmpty) {
                    Log.d("UNIT", "Keine Units gefunden.")
                } else {
                    for (doc in daten.documents) {
                        val unitId = doc.id
                        val unitTitle = doc.getString("title")
                        Log.d("UNIT", "unitId=$unitId title=$unitTitle")
                    }
                }
            }
            .addOnFailureListener { fehler ->
                Log.e("UNIT", "Fehler beim Lesen der Units", fehler)
            }
// ------------------------------------------------------------------
// Lese-Test (Questions): Abfrage der Fragen einer Unit.
// Quelle: courses/uml-basics/units/class-basics/questions
// Ausgabe: Frage-ID, Prompt, korrekter Index im Log.
// ------------------------------------------------------------------
        FirebaseFirestore.getInstance()
            .collection("courses")
            .document("uml-basics")
            .collection("units")
            .document("class-basics")
            .collection("questions")
            .get()
            .addOnSuccessListener { daten ->
                if (daten.isEmpty) {
                    Log.d("QUESTION", "Keine Fragen gefunden.")
                } else {
                    for (doc in daten.documents) {
                        val qId = doc.id
                        val prompt = doc.getString("prompt")
                        val correctIndex = doc.getLong("correctIndex")?.toInt()
                        Log.d("QUESTION", "id=$qId prompt=$prompt correctIndex=$correctIndex")
                    }
                }
            }
            .addOnFailureListener { fehler ->
                Log.e("QUESTION", "Fehler beim Lesen der Fragen", fehler)
            }


        // ------------------------------------------------------------------
        // Standardcode für die Darstellung von Systemrändern.
        // Diese Zeilen gehören zum Standardprojekt und sind nicht Firestore-bezogen.
        // ------------------------------------------------------------------
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
