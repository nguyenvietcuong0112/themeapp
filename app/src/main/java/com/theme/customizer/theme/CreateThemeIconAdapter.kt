package com.theme.customizer.theme

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * Adapter hiển thị lưới 24 biểu tượng icon ứng dụng giả lập.
 */
class CreateThemeIconAdapter(
    private val iconUrls: List<String>
) : RecyclerView.Adapter<CreateThemeIconAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val size = (52 * density).toInt()
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(size, size)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(6, 6, 6, 6)
        }
        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = iconUrls.getOrNull(position) ?: ""
        holder.bind(url)
    }

    override fun getItemCount(): Int = minOf(iconUrls.size, 24).coerceAtLeast(24)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(url: String) {
            val imageView = itemView as ImageView
            if (url.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(url)
                    .centerInside()
                    .into(imageView)
            } else {
                imageView.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        }
    }
}
