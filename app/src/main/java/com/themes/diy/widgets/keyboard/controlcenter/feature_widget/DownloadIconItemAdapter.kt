package com.themes.diy.widgets.keyboard.controlcenter.feature_widget

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.makeramen.roundedimageview.RoundedImageView

class DownloadIconItemAdapter(
    private val items: List<ThemeIconItem>,
    private val onSelectToggle: (ThemeIconItem) -> Unit,
    private val onChangeApp: (ThemeIconItem) -> Unit,
    private val onInstallClick: (ThemeIconItem) -> Unit
) : RecyclerView.Adapter<DownloadIconItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_download_icon_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onSelectToggle, onChangeApp, onInstallClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivSelect: ImageView = itemView.findViewById(R.id.ivSelect)
        private val ivNewIcon: RoundedImageView = itemView.findViewById(R.id.ivNewIcon)
        private val ivEditBadge: ImageView = itemView.findViewById(R.id.ivEditBadge)
        private val tvThemeIconName: TextView = itemView.findViewById(R.id.tvThemeIconName)
        private val llOldIcon: View = itemView.findViewById(R.id.llOldIcon)
        private val ivOldIcon: RoundedImageView = itemView.findViewById(R.id.ivOldIcon)
        private val ivAdd: ImageView = itemView.findViewById(R.id.ivAdd)
        private val etAppName: TextView = itemView.findViewById(R.id.etAppName)
        private val btnAction: FrameLayout = itemView.findViewById(R.id.btnAction)
        private val tvActionText: TextView = itemView.findViewById(R.id.tvActionText)

        fun bind(
            item: ThemeIconItem,
            onSelectToggle: (ThemeIconItem) -> Unit,
            onChangeApp: (ThemeIconItem) -> Unit,
            onInstallClick: (ThemeIconItem) -> Unit
        ) {
            val context = itemView.context
            val cleanName = item.iconName.removePrefix("ic_").replaceFirstChar { it.uppercase() }

            // 1. Checkbox State
            if (item.isSelected) {
                ivSelect.setImageResource(R.drawable.ic_radio_checked)
                ivSelect.imageTintList = null
            } else {
                ivSelect.setImageResource(R.drawable.bg_circle)
                ivSelect.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
            }
            ivSelect.setOnClickListener {
                onSelectToggle(item)
            }

            // 2. Left Icon (Theme Asset Icon)
            tvThemeIconName.text = cleanName
            val assetPath = item.assetPath
            val cleanAssetPath = when {
                assetPath.startsWith("file:///android_asset/") -> assetPath.removePrefix("file:///android_asset/")
                assetPath.startsWith("file://android_asset/") -> assetPath.removePrefix("file://android_asset/")
                assetPath.startsWith("android_asset/") -> assetPath.removePrefix("android_asset/")
                else -> assetPath
            }

            val iconUri = if (cleanAssetPath.startsWith("http://") || cleanAssetPath.startsWith("https://")) {
                Uri.parse(cleanAssetPath)
            } else {
                Uri.parse("file:///android_asset/$cleanAssetPath")
            }

            Glide.with(context)
                .load(iconUri)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivNewIcon)

            ivEditBadge.setOnClickListener {
                onChangeApp(item)
            }

            // 3. Right Icon (Device Target App)
            if (item.targetAppIcon != null) {
                etAppName.text = item.targetAppName ?: cleanName
                ivOldIcon.setImageDrawable(item.targetAppIcon)
                ivAdd.visibility = View.GONE
            } else {
                etAppName.text = "Add app"
                ivOldIcon.setImageResource(R.drawable.bg_default_placeholder)
                ivAdd.visibility = View.VISIBLE
            }

            llOldIcon.setOnClickListener {
                onChangeApp(item)
            }

            // 4. Action Button (Unlock vs Cài đặt)
            val primaryColor = androidx.core.content.ContextCompat.getColor(context, R.color.brand_primary)
            if (item.isUnlocked) {
                btnAction.setBackgroundResource(R.drawable.btn_primary_pill)
                tvActionText.text = "Cài đặt"
                tvActionText.setTextColor(Color.WHITE)
                btnAction.setOnClickListener {
                    onInstallClick(item)
                }
            } else {
                btnAction.setBackgroundResource(R.drawable.btn_outline_pill)
                tvActionText.text = "Unlock"
                tvActionText.setTextColor(primaryColor)
                btnAction.setOnClickListener {
                    item.isUnlocked = true
                    btnAction.setBackgroundResource(R.drawable.btn_primary_pill)
                    tvActionText.text = "Cài đặt"
                    tvActionText.setTextColor(Color.WHITE)
                    btnAction.setOnClickListener {
                        onInstallClick(item)
                    }
                }
            }
        }
    }
}
