package com.example.uml_lern_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uml_lern_app.databinding.ItemUnitActivityBinding

class UnitAdapter(
    private val units: List<UnitItem>,
    private val onClick: (UnitItem) -> Unit
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    inner class UnitViewHolder(val binding: ItemUnitActivityBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemUnitActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val item = units[position]
        holder.binding.tvUnitTitle.text = item.title
        holder.binding.tvUnitDescription.text = item.description
        holder.binding.tvUnitDuration.text = "Dauer: ${item.duration}"

        // Klick auf eine Unit
        holder.binding.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = units.size
}
