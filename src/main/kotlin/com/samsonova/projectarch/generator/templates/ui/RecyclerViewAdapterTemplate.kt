package com.samsonova.projectarch.generator.templates.ui

import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.Feature
import com.samsonova.projectarch.models.Screen

object RecyclerViewAdapterTemplate {
    fun generate(architecture: AppArchitecture, feature: Feature, screen: Screen): String {
        return """
            package ${architecture.packageName}.${feature.name.lowercase()}.presentation
            
            import android.view.LayoutInflater
            import android.view.ViewGroup
            import androidx.recyclerview.widget.RecyclerView
            import androidx.recyclerview.widget.DiffUtil
            import androidx.recyclerview.widget.ListAdapter
            
            data class ${screen.name}Item(val id: String, val title: String)
            
            class ${screen.name}Adapter : ListAdapter<${screen.name}Item, ${screen.name}Adapter.ViewHolder>(ItemDiffCallback()) {
            
                inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
                    fun bind(item: ${screen.name}Item) {
                        // Bind item to view
                    }
                }
            
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_${screen.name.camelToSnakeCase()}, parent, false)
                    )
                }
            
                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    holder.bind(getItem(position))
                }
            
                class ItemDiffCallback : DiffUtil.ItemCallback<${screen.name}Item>() {
                    override fun areItemsTheSame(old: ${screen.name}Item, new: ${screen.name}Item) = old.id == new.id
                    override fun areContentsTheSame(old: ${screen.name}Item, new: ${screen.name}Item) = old == new
                }
            }
        """.trimIndent()
    }
}