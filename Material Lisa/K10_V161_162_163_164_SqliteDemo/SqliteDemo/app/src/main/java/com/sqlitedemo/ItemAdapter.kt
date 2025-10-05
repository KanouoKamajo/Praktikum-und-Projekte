package com.sqlitedemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.items_row.view.*

class ItemAdapter(val context: Context, val items: ArrayList<EmpModelClass>) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    /**
     * Setzt das Item in MyViewHolder auf das Item, welches in XML von uns erstellt wurde
     *
     * Erstellung eines neuen
     * {@link ViewHolder} und Initialisierung einiger privater Felder, welche für das RecyclerView
     * benötigt werden.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.items_row,
                parent,
                false
            )
        )
    }

    /**
     * Verknüpft ein Item aus der View mit einem Element aus der ArrayList
     *
     * Wird aufgerufen, wenn das RecyclerView einen neuen {@link ViewHolder} des gegebenen
     * Typen benötigt.
     *
     * ViewHolder sollte mit einer neuen View erstellt werden, welches die Items des gegebenen
     * Typs repräsentiert. Ein View kann manuell erstellt werden oder durch eine XML-Datei
     * beeinflusst werden.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items.get(position)

        holder.tvName.text = item.name
        holder.tvEmail.text = item.email

        // Hintergrundfarbe basierend auf gerade/ ungerade Zahl anpassen.
        if (position % 2 == 0) {
            holder.llMain.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorLightGray
                )
            )
        } else {
            holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
        }

        holder.ivEdit.setOnClickListener { view ->

            if (context is MainActivity) {
                context.updateRecordDialog(item)
            }
        }

        holder.ivDelete.setOnClickListener { view ->

            if (context is MainActivity) {
                context.deleteRecordAlertDialog(item)
            }
        }
    }

    /**
     * Gibt die Anzahl der Einträge der ArrayList zurück
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * ViewHolder beschreibt eine Item-Ansicht und enthält Metadaten über die Platzierung im
     * RecyclerView.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val llMain = view.llMain
        val tvName = view.tvName
        val tvEmail = view.tvEmail
        val ivEdit = view.ivEdit
        val ivDelete = view.ivDelete
    }
}