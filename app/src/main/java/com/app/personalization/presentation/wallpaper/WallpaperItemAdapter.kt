package com.app.personalization.presentation.wallpaper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import com.bumptech.glide.Glide

class WallpaperItemAdapter(
    private val screenWidth: Int,
    private val columns: Int,
    private val onItemClick: (WidgetThemeWallpaper) -> Unit,
    private val onFavoriteClick: (WidgetThemeWallpaper) -> Unit
) : RecyclerView.Adapter<WallpaperItemAdapter.ViewHolder>() {

    private var items = listOf<WidgetThemeWallpaper>()

    fun submitList(list: List<WidgetThemeWallpaper>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_wallpaper_item_layout,
            parent,
            false
        )
        // Dynamically scale item size based on screen width and columns
        val itemW = screenWidth / columns
        view.layoutParams = ViewGroup.LayoutParams(itemW, ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onItemClick, onFavoriteClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
        private val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)
        private val cardView: View = view.findViewById(R.id.cardView)

        fun bind(
            wallpaper: WidgetThemeWallpaper,
            onClick: (WidgetThemeWallpaper) -> Unit,
            onFavClick: (WidgetThemeWallpaper) -> Unit
        ) {
            val localDrawable = wallpaper.generateLocalThemePreview(itemView.context)
            ivPreview.setImageDrawable(localDrawable)

            // Setup favorite icon state
            if (wallpaper.isFavorite) {
                ivFavorite.setImageResource(R.drawable.bg_favorite)
                ivFavorite.imageTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#FF4081")
                )
            } else {
                ivFavorite.setImageResource(R.drawable.bg_favorite)
                ivFavorite.imageTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#FFFFFF")
                )
            }

            ivFavorite.setOnClickListener {
                onFavClick(wallpaper)
            }

            cardView.setOnClickListener {
                onClick(wallpaper)
            }
        }
    }
}
