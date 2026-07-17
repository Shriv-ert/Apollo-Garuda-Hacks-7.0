package com.garuda.floatingbubble.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.garuda.floatingbubble.R



class EntityAdapter(private var entities: List<com.garuda.floatingbubble.data.EntityData>) : RecyclerView.Adapter<EntityAdapter.EntityViewHolder>() {

    private var filteredList: List<com.garuda.floatingbubble.data.EntityData> = entities
    private var currentQuery: String = ""
    private var currentType: String? = null // null means "All"

    fun filter(query: String, type: String?) {
        currentQuery = query
        currentType = type
        
        filteredList = entities.filter {
            val matchesQuery = if (currentQuery.isEmpty()) true else {
                it.id.contains(currentQuery, ignoreCase = true) ||
                it.category.contains(currentQuery, ignoreCase = true)
            }
            val matchesType = if (currentType == null) true else it.type == currentType
            
            matchesQuery && matchesType
        }
        notifyDataSetChanged()
    }
    
    fun updateData(newEntities: List<com.garuda.floatingbubble.data.EntityData>) {
        this.entities = newEntities
        filter(currentQuery, currentType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entity, parent, false)
        return EntityViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        val entity = filteredList[position]
        holder.tvEntityValue.text = entity.id
        holder.tvEntityCategory.text = entity.category
        
        holder.tvEntityType.text = when(entity.type) {
            "phone" -> "NOMOR HP"
            "bank_account" -> "REKENING"
            "url" -> "TAUTAN (URL)"
            "email" -> "EMAIL"
            else -> "UNKNOWN"
        }
    }

    override fun getItemCount(): Int = filteredList.size

    class EntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEntityType: TextView = itemView.findViewById(R.id.tvEntityType)
        val tvEntityValue: TextView = itemView.findViewById(R.id.tvEntityValue)
        val tvEntityCategory: TextView = itemView.findViewById(R.id.tvEntityCategory)
    }
}
