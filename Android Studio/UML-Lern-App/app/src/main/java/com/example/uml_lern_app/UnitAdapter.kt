package com.example.uml_lern_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UnitAdapter(
    private var items: List<UnitItem>,
    private val onClick: (UnitItem) -> Unit = {}
) : RecyclerView.Adapter<UnitAdapter.UnitVH>() {

    class UnitVH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvUnitTitle)
        val sub: TextView = v.findViewById(R.id.tvUnitSub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unit, parent, false)
        return UnitVH(v)
    }

    override fun onBindViewHolder(holder: UnitVH, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.sub.text = "" // z. B. "5 Fragen" – später
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<UnitItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
