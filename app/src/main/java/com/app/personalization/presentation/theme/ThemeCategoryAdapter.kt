package com.app.personalization.presentation.theme

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
            val typedValue = android.util.TypedValue()
            
            if (item.isSelected) {
                context.theme.resolveAttribute(R.attr.primaryColor, typedValue, true)
                val primaryColor = typedValue.data
                llContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(primaryColor)
                tvName.setTextColor(Color.WHITE)
            } else {
                context.theme.resolveAttribute(R.attr.secondaryBackgroundColor, typedValue, true)
                val secondaryBg = typedValue.data
                context.theme.resolveAttribute(R.attr.titleTextColor, typedValue, true)
                val textColor = typedValue.data
                llContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(secondaryBg)
                tvName.setTextColor(textColor)
            }

            llContainer.setOnClickListener {
                onClick(item)
            }
        }
    }
}
