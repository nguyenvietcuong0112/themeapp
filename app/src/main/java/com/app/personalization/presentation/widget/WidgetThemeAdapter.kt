package com.app.personalization.presentation.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.ResourceConfig
import com.app.personalization.presentation.customviews.RemoteImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class WidgetThemeAdapter(
    private val list: List<WidgetTheme>,
    private val parentWidth: Int,
    private val column: Int,
    private val onItemClick: (WidgetTheme) -> Unit
) : RecyclerView.Adapter<WidgetThemeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_theme_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item, parentWidth, column, onItemClick)
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivPreview: RemoteImageView = view.findViewById(R.id.ivPreview)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val ivFavorite: View? = view.findViewById(R.id.ivFavorite)
        private val ivDelete: View? = view.findViewById(R.id.ivDelete)
        private val cardView: View = view.findViewById(R.id.cardView)

        fun bind(item: WidgetTheme, parentWidth: Int, column: Int, onClick: (WidgetTheme) -> Unit) {
            val context = itemView.context
            
            // Hide delete and favorite buttons for shop themes view list
            ivDelete?.visibility = View.GONE
            ivFavorite?.visibility = View.GONE

            // Calculate dynamic item width based on grid columns
            val padding = context.resources.getDimensionPixelSize(R.dimen.dp_16)
            val itemWidth = (parentWidth - (padding * (column - 1))) / column
            itemView.layoutParams = ViewGroup.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

            tvName.text = item.name
            
            val previewUrl = com.app.personalization.data.CdnPathResolver.getThemePreviewUrl(item.folder)

            Glide.with(context)
                .load(previewUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivPreview)

            cardView.setOnClickListener {
                onClick(item)
            }
        }
    }
}
