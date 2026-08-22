package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.themes.diy.widgets.keyboard.controlcenter.R

data class ControlCategoryItem(
    val slug: String,
    val name: String,
    val isSelected: Boolean = false
)

class ControlCategoryAdapter(
    private var items: List<ControlCategoryItem>,
    private val onCategoryClick: (ControlCategoryItem) -> Unit
) : RecyclerView.Adapter<ControlCategoryAdapter.ViewHolder>() {

    fun submitList(list: List<ControlCategoryItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_collection_tab,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTabName: TextView = view.findViewById(R.id.tvTabName)

        fun bind(item: ControlCategoryItem) {
            tvTabName.text = item.name
            val context = itemView.context

            if (item.isSelected) {
                tvTabName.setBackgroundResource(R.drawable.bg_category_tab_selected)
                tvTabName.setTextColor(Color.parseColor("#FA5783"))
                tvTabName.typeface = ResourcesCompat.getFont(context, R.font.inter_semi_bold)
            } else {
                tvTabName.setBackgroundResource(R.drawable.bg_category_tab_unselected)
                tvTabName.setTextColor(Color.parseColor("#1A1A1A"))
                tvTabName.typeface = ResourcesCompat.getFont(context, R.font.inter_medium)
            }

            val clickHandler = View.OnClickListener {
                if (!item.isSelected) {
                    onCategoryClick(item)
                }
            }
            itemView.setOnClickListener(clickHandler)
            tvTabName.setOnClickListener(clickHandler)
        }
    }
}
