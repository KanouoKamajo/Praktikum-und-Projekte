package com.example.datenbanken

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter (context : Context) : CursorAdapter (context, null, 0) {

    private val date: Date = Date()

    private val DF_DATE: DateFormat = SimpleDateFormat
        .getDateInstance(DateFormat.MEDIUM)
    private val DF_TIME: DateFormat = SimpleDateFormat
        .getTimeInstance(DateFormat.MEDIUM)

    private val inflator: LayoutInflater = LayoutInflater.from(context)

    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        val ciMood: Int = cursor.getColumnIndex(OpenHandler.MOOD_MOOD)
        val ciTimeMillis: Int = cursor.getColumnIndex(OpenHandler.MOOD_TIME)
        val image: ImageView = view.findViewById(R.id.icon)
        val mood: Int = cursor.getInt(ciMood)
        if (mood == OpenHandler.MOOD_FINE) {
            image.setImageResource(R.mipmap.smiley_gut)
        } else if (mood == OpenHandler.MOOD_OK) {
            image.setImageResource(R.mipmap.smiley_ok)
        } else {
            image.setImageResource(R.mipmap.smiley_schlecht)
        }
        val textview1: TextView = view.findViewById(R.id.text1)
        val textview2: TextView = view.findViewById(R.id.text2)
        val timeMillis: Long = cursor.getLong(ciTimeMillis)
        date.setTime(timeMillis)
        textview1.setText(DF_DATE.format(date))
        textview2.setText(DF_TIME.format(date))
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View? {
        return inflator.inflate(R.layout.icon_text_text, null)
    }

}