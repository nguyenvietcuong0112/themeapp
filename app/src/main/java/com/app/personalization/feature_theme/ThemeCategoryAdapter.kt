package com.app.personalization.feature_theme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R

class ThemeCategoryAdapter(
    private val onCategoryClick: (CategoryTag) -> Unit
) : RecyclerView.Adapter<ThemeCategoryAdapter.ViewHolder>() {

    private var items = listOf<CategoryTag>()

    fun submitList(list: List<CategoryTag>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_theme_category_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onCategoryClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val llContainer: View = view.findViewById(R.id.llContainer)

        fun bind(item: CategoryTag, onClick: (CategoryTag) -> Unit) {
            tvName.text = item.name
            val context = tvName.context
            
            if (item.isSelected) {
                llContainer.setBackgroundResource(R.drawable.bg_category_tab_selected)
                llContainer.backgroundTintList = null
                tvName.setTextColor(Color.parseColor("#FA5783"))
                tvName.typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.inter_semi_bold)
            } else {
                llContainer.setBackgroundResource(R.drawable.bg_category_tab_unselected)
                llContainer.backgroundTintList = null
                tvName.setTextColor(Color.parseColor("#1A1A1A"))
                tvName.typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.inter_medium)
            }

            llContainer.setOnClickListener {
                onClick(item)
            }
        }
    }
}
