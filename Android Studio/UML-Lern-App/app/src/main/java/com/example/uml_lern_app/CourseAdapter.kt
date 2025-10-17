package com.example.uml_lern_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uml_lern_app.databinding.ActivityItemCourseBinding

class CourseAdapter(
    private val items: List<Course>,
    private val onClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.VH>() {

    inner class VH(private val b: ActivityItemCourseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Course) {
            b.tvTitle.text = item.title
            b.tvSubtitle.text = item.subtitle
            b.imgIcon.setImageResource(item.iconRes)   // ID muss im XML existieren
            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ActivityItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
