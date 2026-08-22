package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.themes.diy.widgets.keyboard.controlcenter.R

class ControlCenterAdapter(
    private var items: List<ControlTheme>,
    private val onItemClick: (ControlTheme) -> Unit
) : RecyclerView.Adapter<ControlCenterAdapter.ViewHolder>() {

    fun updateData(newItems: List<ControlTheme>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_control_theme, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: View? = itemView.findViewById(R.id.cardView)
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDownloads: TextView? = itemView.findViewById(R.id.tvDownloads)

        fun bind(item: ControlTheme) {
            tvName.text = item.name
            tvDownloads?.text = item.downloads.toString()

            Glide.with(itemView.context)
                .load(item.thumbPath)
                .placeholder(R.color.grayF2F2F2)
                .error(R.color.grayF2F2F2)
                .into(ivThumbnail)

            val clickListener = View.OnClickListener {
                onItemClick(item)
            }
            itemView.setOnClickListener(clickListener)
            cardView?.setOnClickListener(clickListener)
        }
    }
}
