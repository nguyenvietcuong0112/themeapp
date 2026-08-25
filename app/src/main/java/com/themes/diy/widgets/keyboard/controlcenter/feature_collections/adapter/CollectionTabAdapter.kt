package com.themes.diy.widgets.keyboard.controlcenter.feature_collections.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data.CollectionTab

class CollectionTabAdapter(
    private var tabs: List<CollectionTab>,
    private val onTabSelected: (CollectionTab) -> Unit
) : RecyclerView.Adapter<CollectionTabAdapter.TabViewHolder>() {

    fun updateTabs(newTabs: List<CollectionTab>) {
        tabs = newTabs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collection_tab, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabs[position])
    }

    override fun getItemCount(): Int = tabs.size

    inner class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTabName: TextView = itemView.findViewById(R.id.tvTabName)

        fun bind(tab: CollectionTab) {
            tvTabName.text = tab.name
            val context = tvTabName.context
            if (tab.isSelected) {
                tvTabName.setBackgroundResource(R.drawable.bg_category_tab_selected)
                tvTabName.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.brand_primary))
                tvTabName.typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.inter_semi_bold)
            } else {
                tvTabName.setBackgroundResource(R.drawable.bg_category_tab_unselected)
                tvTabName.setTextColor(Color.parseColor("#1A1A1A"))
                tvTabName.typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.inter_medium)
            }

            val clickHandler = View.OnClickListener {
                if (!tab.isSelected) {
                    onTabSelected(tab)
                }
            }
            itemView.setOnClickListener(clickHandler)
            tvTabName.setOnClickListener(clickHandler)
        }
    }
}
