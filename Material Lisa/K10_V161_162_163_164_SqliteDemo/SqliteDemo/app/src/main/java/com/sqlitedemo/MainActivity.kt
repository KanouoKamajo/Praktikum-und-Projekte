package com.sqlitedemo

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_update.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btnAdd.setOnClickListener { view ->

            addRecord(view)
        }

        setupListofDataIntoRecyclerView()
    }

    /**
     * Funktion zeigt Liste der vorhandenen Einträge an.
     */
    private fun setupListofDataIntoRecyclerView() {

        if (getItemsList().size > 0) {

            rvItemsList.visibility = View.VISIBLE
            tvNoRecordsAvailable.visibility = View.GONE

            // LayoutManager für das RecyclerView setzen.
            rvItemsList.layoutManager = LinearLayoutManager(this)
            // AdapterKlasse initialisieren und Item Liste übergeben.
            val itemAdapter = ItemAdapter(this, getItemsList())
            // Instanz der AdapterKlasse als Adapter des RecyclerViews setzen.
            rvItemsList.adapter = itemAdapter
        } else {

            rvItemsList.visibility = View.GONE
            tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }

    /**
     * Funktion gibt Liste aller Mitarbeiter aus der Datenbank zurück.
     */
    private fun getItemsList(): ArrayList<EmpModelClass> {
        // Instanz eines DatenbankHandlers erstellen
        val databaseHandler: DatabaseHandler = DatabaseHandler(this)
        // Aufruf der Funktion viewEmployee, um alle Mitarbeiter als Liste zu bekommen
        val empList: ArrayList<EmpModelClass> = databaseHandler.viewEmployee()

        return empList
    }

    // Methode, um einen Datensatz in der Datenbank zu speichern
    fun addRecord(view: View) {
        val name = etName.text.toString()
        val email = etEmailId.text.toString()
        val databaseHandler: DatabaseHandler = DatabaseHandler(this)
        if (!name.isEmpty() && !email.isEmpty()) {
            val status =
                databaseHandler.addEmployee(EmpModelClass(0, name, email))
            if (status > -1) {
                Toast.makeText(applicationContext, "Datensatz gespeichert", Toast.LENGTH_LONG).show()
                etName.text.clear()
                etEmailId.text.clear()

                setupListofDataIntoRecyclerView()
            }
        } else {
            Toast.makeText(
                applicationContext,
                "Name oder Email darf nicht leer sein",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Methode zeigt Dialog an.
     */
    fun updateRecordDialog(empModelClass: EmpModelClass) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        // Content für die Anzeige als XML-Datei setzen.
        updateDialog.setContentView(R.layout.dialog_update)

        updateDialog.etUpdateName.setText(empModelClass.name)
        updateDialog.etUpdateEmailId.setText(empModelClass.email)

        updateDialog.tvUpdate.setOnClickListener(View.OnClickListener {

            val name = updateDialog.etUpdateName.text.toString()
            val email = updateDialog.etUpdateEmailId.text.toString()

            val databaseHandler: DatabaseHandler = DatabaseHandler(this)

            if (!name.isEmpty() && !email.isEmpty()) {
                val status =
                    databaseHandler.updateEmployee(EmpModelClass(empModelClass.id, name, email))
                if (status > -1) {
                    Toast.makeText(applicationContext, "Datensatz aktualisiert.", Toast.LENGTH_LONG).show()

                    setupListofDataIntoRecyclerView()

                    updateDialog.dismiss() // Dialog wird geschlossen
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Name oder Email darf nicht leer sein",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        updateDialog.tvCancel.setOnClickListener(View.OnClickListener {
            updateDialog.dismiss()
        })
        // Dialog starten und anzeigen.
        updateDialog.show()
    }

    /**
     * Funktion zeigt Dialog mit Auswahlmöglichkeit an.
     */
    fun deleteRecordAlertDialog(empModelClass: EmpModelClass) {
        val builder = AlertDialog.Builder(this)
        // Titel des Dialogs setzen
        builder.setTitle("Datensatz löschen")
        // Nachricht für den Dialog setzen
        builder.setMessage("Bist du dir sicher, dass du den Datensatz ${empModelClass.name} löschen möchtest?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        // Wenn ja ausgewählt wird
        builder.setPositiveButton("Ja") { dialogInterface, which ->

            // Instanz der Klasse DatabaseHandler erstellen
            val databaseHandler: DatabaseHandler = DatabaseHandler(this)
            // Aufruf der Methode deleteEmployee im Handler, um den Datensatz zu löschen
            val status = databaseHandler.deleteEmployee(EmpModelClass(empModelClass.id, "", ""))
            if (status > -1) {
                Toast.makeText(
                    applicationContext,
                    "Datensatz erfolgreich gelöscht.",
                    Toast.LENGTH_LONG
                ).show()

                setupListofDataIntoRecyclerView()
            }

            dialogInterface.dismiss() // Dialog wird geschlossen
        }
        // Wenn nein ausgewählt wird
        builder.setNegativeButton("Nein") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog wird geschlossen
        }
        // Dialog erstellen
        val alertDialog: AlertDialog = builder.create()
        // Weitere Einstellungen zum Dialog einstellen
        alertDialog.setCancelable(false) // Der Nutzer kann den Dialog nicht abbrechen -> Entweder ja oder nein.
        alertDialog.show()  // Dialog im UI anzeigen
    }
}
