package com.example.uml_lern_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter verbindet Kursdaten mit den Zeilenansichten der RecyclerView.
class CourseAdapter(
    private var items: List<Course>,
    private val onClick: (Course) -> Unit = {}
) : RecyclerView.Adapter<CourseAdapter.CourseVH>() {

    // ViewHolder hält Referenzen auf die Views einer Zeile.
    class CourseVH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvCourseTitle)
        val sub: TextView = v.findViewById(R.id.tvCourseSub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseVH(v)
    }

    override fun onBindViewHolder(holder: CourseVH, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.sub.text = "" // reserviert für spätere Infos (z. B. Units)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    // Erlaubt das Ersetzen der Liste (z. B. nach Firestore-Load).
    fun submitList(newItems: List<Course>) {
        items = newItems
        notifyDataSetChanged()
    }
}
