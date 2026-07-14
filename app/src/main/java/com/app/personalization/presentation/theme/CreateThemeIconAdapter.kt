package com.app.personalization.presentation.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.bumptech.glide.Glide

class CreateThemeIconAdapter(
    private val iconUrls: List<String>
) : RecyclerView.Adapter<CreateThemeIconAdapter.ViewHolder>() {

    // Default 8 app titles to show under icons
    private val appTitles = listOf(
        "Facebook", "Instagram", "Messenger", "TikTok",
        "Chrome", "Gmail", "Camera", "Settings"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_icon_create_theme_layout,
            parent,
            false
        )
        // Set dynamic sizing for grid items (approx 56dp square for icon)
        val density = parent.context.resources.displayMetrics.density
        val size = (56 * density).toInt()
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        ivIcon.layoutParams.width = size
        ivIcon.layoutParams.height = size
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = iconUrls.getOrNull(position) ?: ""
        val title = appTitles.getOrNull(position) ?: "App"
        holder.bind(url, title)
    }

    override fun getItemCount(): Int = minOf(iconUrls.size, 8).coerceAtLeast(iconUrls.size)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)

        fun bind(url: String, title: String) {
            tvTitle.text = title
            if (url.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(url)
                    .into(ivIcon)
            } else {
                ivIcon.setImageResource(R.mipmap.ic_launcher)
            }
        }
    }
}
