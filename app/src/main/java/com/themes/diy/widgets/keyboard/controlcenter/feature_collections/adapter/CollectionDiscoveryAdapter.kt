package com.themes.diy.widgets.keyboard.controlcenter.feature_collections.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data.CollectionItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class CollectionDiscoveryAdapter(
    private var items: List<CollectionItem>,
    private val onItemClick: (CollectionItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DEFAULT_CARD = 0
        private const val TYPE_WIDGET_CARD = 1
        private const val TYPE_ICON_CARD = 2
    }

    fun submitList(newItems: List<CollectionItem>) {
        Log.d("CollectionDebug", "Adapter: submitList called with ${newItems.size} items. First item category: ${newItems.firstOrNull()?.category}")
        items = newItems
        notifyDataSetChanged()
    }

    fun getItem(position: Int): CollectionItem? = items.getOrNull(position)

    override fun getItemViewType(position: Int): Int {
        val item = items.getOrNull(position)
        return when {
            item?.category.equals("Widget", ignoreCase = true) -> TYPE_WIDGET_CARD
            item?.category.equals("Icons", ignoreCase = true) -> TYPE_ICON_CARD
            else -> TYPE_DEFAULT_CARD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_WIDGET_CARD -> {
                val view = inflater.inflate(R.layout.item_config_widget, parent, false)
                WidgetViewHolder(view)
            }
            TYPE_ICON_CARD -> {
                val view = inflater.inflate(R.layout.item_collection_card_icon, parent, false)
                DefaultCardViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_collection_card, parent, false)
                DefaultCardViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is WidgetViewHolder) {
            holder.bind(item)
        } else if (holder is DefaultCardViewHolder) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // 1. ViewHolder for Widget Items (Matching WidgetConfigFragment layout)
    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPreview: ImageView = itemView.findViewById(R.id.ivPreview)
        private val llContainer: View = itemView.findViewById(R.id.llContainer) ?: itemView

        fun bind(item: CollectionItem) {
            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val density = displayMetrics.density
            val horizontalPadding = (32 * density).toInt()
            val gridWidth = screenWidth - horizontalPadding
            val itemHeight = (gridWidth / 3).coerceAtLeast((100 * density).toInt())

            val lp = itemView.layoutParams
            if (lp != null && lp.height != itemHeight) {
                lp.height = itemHeight
                itemView.layoutParams = lp
            }

            val cleanAssetPath = when {
                item.previewPath.startsWith("file:///android_asset/") -> item.previewPath.removePrefix("file:///android_asset/")
                item.previewPath.startsWith("file://android_asset/") -> item.previewPath.removePrefix("file://android_asset/")
                item.previewPath.startsWith("android_asset/") -> item.previewPath.removePrefix("android_asset/")
                else -> item.previewPath
            }

            val imageUri = if (cleanAssetPath.startsWith("http://") || cleanAssetPath.startsWith("https://")) {
                Uri.parse(cleanAssetPath)
            } else {
                Uri.parse("file:///android_asset/$cleanAssetPath")
            }

            Log.d("CollectionDebug", "WidgetViewHolder.bind: pos=$bindingAdapterPosition id=${item.id}, uri=$imageUri, height=$itemHeight")

            Glide.with(context)
                .asBitmap()
                .load(imageUri)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        Log.d("CollectionDebug", "Glide SUCCESS: Loaded bitmap (${resource.width}x${resource.height}) for widget ${item.id}")
                        ivPreview.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        ivPreview.setImageDrawable(placeholder)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Log.e("CollectionDebug", "Glide FAILED: Could not load bitmap for widget ${item.id}, uri=$imageUri")
                        ivPreview.setImageDrawable(errorDrawable)
                    }
                })

            llContainer.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // 2. ViewHolder for Default Collection Cards (Theme, Icons, Control Center, Wallpaper)
    inner class DefaultCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardThumb: CardView? = itemView.findViewById(R.id.cardThumb)
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDownloads: TextView = itemView.findViewById(R.id.tvDownloads)
        private val ivDownloadIcon: ImageView = itemView.findViewById(R.id.ivDownloadIcon)

        fun bind(item: CollectionItem) {
            tvName.text = item.name
            tvDownloads.text = item.downloads.toString()

            val cleanAssetPath = when {
                item.previewPath.startsWith("file:///android_asset/") -> item.previewPath.removePrefix("file:///android_asset/")
                item.previewPath.startsWith("file://android_asset/") -> item.previewPath.removePrefix("file://android_asset/")
                item.previewPath.startsWith("android_asset/") -> item.previewPath.removePrefix("android_asset/")
                else -> item.previewPath
            }

            val imageUri = if (cleanAssetPath.startsWith("http://") || cleanAssetPath.startsWith("https://")) {
                Uri.parse(cleanAssetPath)
            } else {
                Uri.parse("file:///android_asset/$cleanAssetPath")
            }

            Glide.with(itemView.context)
                .load(imageUri)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .into(ivThumbnail)

            itemView.setOnClickListener {
                onItemClick(item)
            }
            cardThumb?.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
