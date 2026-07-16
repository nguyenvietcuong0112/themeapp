package com.app.personalization.presentation.theme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R

class CreateThemeCategoryAdapter(
    private val categories: List<String>,
    private var selectedCategory: String,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CreateThemeCategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_theme_category_layout,
            parent,
            false
          )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, category == selectedCategory) {
            val oldSelected = selectedCategory
            selectedCategory = category
            notifyItemChanged(categories.indexOf(oldSelected))
            notifyItemChanged(position)
            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val llContainer: View = view.findViewById(R.id.llContainer)

        fun bind(category: String, isSelected: Boolean, onClick: () -> Unit) {
            tvName.text = category
            
            if (isSelected) {
                llContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#00E5FF"))
                tvName.setTextColor(Color.parseColor("#12121A"))
            } else {
                llContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2E2E3E"))
                tvName.setTextColor(Color.parseColor("#FFFFFF"))
            }

            llContainer.setOnClickListener {
                onClick()
            }
        }
    }
}
