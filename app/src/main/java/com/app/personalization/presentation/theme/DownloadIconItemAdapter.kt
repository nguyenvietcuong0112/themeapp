package com.app.personalization.presentation.theme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.bumptech.glide.Glide

class DownloadIconItemAdapter(
    private val items: List<ThemeIconItem>,
    private val isUnlocked: Boolean,
    private val onSelectToggle: (ThemeIconItem) -> Unit,
    private val onChangeApp: (ThemeIconItem) -> Unit,
    private val onUnlockClick: () -> Unit,
    private val onInstallClick: (ThemeIconItem) -> Unit
) : RecyclerView.Adapter<DownloadIconItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_download_icon_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], isUnlocked, onSelectToggle, onChangeApp, onUnlockClick, onInstallClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivSelect: ImageView = view.findViewWithTag("binding_1")
        private val ivNewIcon: ImageView = view.findViewById(R.id.ivNewIcon)
        private val llOldIcon: View = view.findViewById(R.id.llOldIcon)
        private val ivOldIcon: ImageView = view.findViewById(R.id.ivOldIcon)
        private val ivAdd: View = view.findViewById(R.id.ivAdd)
        private val llInstall: View = view.findViewById(R.id.llInstall)
        private val unlockView: View = view.findViewById(R.id.unlockView)

        fun bind(
            item: ThemeIconItem,
            isUnlocked: Boolean,
            onSelectToggle: (ThemeIconItem) -> Unit,
            onChangeApp: (ThemeIconItem) -> Unit,
            onUnlockClick: () -> Unit,
            onInstallClick: (ThemeIconItem) -> Unit
        ) {
            val context = itemView.context

            // Checkbox select state
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

            val assetPath = item.assetPath
            val themePath = if (assetPath.contains("theme_decorates/")) {
                assetPath.substringAfter("theme_decorates/").substringBefore("/key/")
            } else {
                assetPath.substringBefore("/key/")
            }
            val cdnUrl = com.app.personalization.data.ResourceConfig.getLauncherIconUrl("theme_decorates/$themePath", item.iconName)
            val localKeyPath = "file:///android_asset/theme_decorates/$themePath/key/key.png"

            // Load new icon from CDN, fallback to local key background, then to placeholder
            Glide.with(context)
                .load(cdnUrl)
                .placeholder(R.drawable.ic_style_weather)
                .error(
                    Glide.with(context)
                        .load(localKeyPath)
                        .placeholder(R.drawable.ic_style_weather)
                        .error(R.drawable.ic_style_weather)
                )
                .into(ivNewIcon)

            // Bound target application icon display
            if (item.targetAppIcon != null) {
                ivOldIcon.visibility = View.VISIBLE
                ivOldIcon.setImageDrawable(item.targetAppIcon)
                ivAdd.visibility = View.GONE
            } else {
                ivOldIcon.visibility = View.GONE
                ivAdd.visibility = View.VISIBLE
            }

            // Click container to change target app
            llOldIcon.setOnClickListener {
                onChangeApp(item)
            }

            // Lock / Install UI
            if (isUnlocked) {
                unlockView.visibility = View.GONE
                llInstall.visibility = View.VISIBLE
            } else {
                unlockView.visibility = View.VISIBLE
                llInstall.visibility = View.GONE
                
                unlockView.findViewById<View>(R.id.ctUnlock)?.setOnClickListener {
                    onUnlockClick()
                }
                unlockView.findViewById<View>(R.id.tvAds)?.visibility = View.GONE
            }

            llInstall.setOnClickListener {
                onInstallClick(item)
            }
        }
    }
}
