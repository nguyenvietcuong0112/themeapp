package com.app.personalization.feature_icon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.feature_widget.data.entity.WidgetThemeIcon
import com.app.personalization.core.data.ResourceConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class IconPackAdapter(
    private val parentWidth: Int,
    private val columns: Int,
    private val onItemClick: (WidgetThemeIcon) -> Unit
) : RecyclerView.Adapter<IconPackAdapter.ViewHolder>() {

    private var items = listOf<WidgetThemeIcon>()

    fun submitList(list: List<WidgetThemeIcon>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_icon_pack_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], parentWidth, columns, onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val llViewAll: View? = view.findViewById(R.id.llViewAll)
        private val cardContainer: View = view.findViewById(R.id.cardContainer)

        fun bind(
            item: WidgetThemeIcon,
            parentWidth: Int,
            columns: Int,
            onClick: (WidgetThemeIcon) -> Unit
        ) {
            val context = itemView.context
            tvName.text = item.name

            val previewUrl = ResourceConfig.getIconCategoryPreviewUrl(item.folder)

            Glide.with(context)
                .load(previewUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivPreview)

            cardContainer.setOnClickListener {
                onClick(item)
            }
            llViewAll?.setOnClickListener {
                onClick(item)
            }
        }
    }
}
