package com.app.personalization.presentation.widget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.databinding.ItemDownloadIconLayoutBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class DownloadIconItemAdapter(
    private val items: List<ThemeIconItem>,
    private val onSelectToggle: (ThemeIconItem) -> Unit,
    private val onChangeApp: (ThemeIconItem) -> Unit,
    private val onInstallClick: (ThemeIconItem) -> Unit
) : RecyclerView.Adapter<DownloadIconItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadIconLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onSelectToggle, onChangeApp, onInstallClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemDownloadIconLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private var textWatcher: android.text.TextWatcher? = null

        fun bind(
            item: ThemeIconItem,
            onSelectToggle: (ThemeIconItem) -> Unit,
            onChangeApp: (ThemeIconItem) -> Unit,
            onInstallClick: (ThemeIconItem) -> Unit
        ) {
            val context = itemView.context

            // Remove old watcher
            textWatcher?.let { binding.etAppName.removeTextChangedListener(it) }
            
            binding.etAppName.setText(item.targetAppName ?: item.iconName)
            
            val watcher = object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    item.targetAppName = s?.toString()
                }
            }
            binding.etAppName.addTextChangedListener(watcher)
            textWatcher = watcher

            // Select checkbox state (tag binding_1)
            val ivSelect = binding.root.findViewWithTag<android.widget.ImageView>("binding_1")
            if (ivSelect != null) {
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
            }

            val assetPath = item.assetPath
            val themePath = if (assetPath.contains("theme_decorates/")) {
                assetPath.substringAfter("theme_decorates/").substringBefore("/key/")
            } else {
                assetPath.substringBefore("/key/")
            }
            val cdnUrl = com.app.personalization.data.ResourceConfig.getLauncherIconUrl(context, themePath, item.iconName)
            val localKeyPath = "file:///android_asset/theme_decorates/$themePath/key/key.png"

            val glideRequest = if (cdnUrl.isNotEmpty()) {
                Glide.with(context).load(cdnUrl)
            } else {
                Glide.with(context).load(localKeyPath)
            }

            glideRequest
                .placeholder(R.drawable.bg_default_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    Glide.with(context)
                        .load(localKeyPath)
                        .placeholder(R.drawable.bg_default_placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.bg_default_placeholder)
                )
                .into(binding.ivNewIcon)

            // Bound target application icon display
            if (item.targetAppIcon != null) {
                binding.ivOldIcon.visibility = View.VISIBLE
                binding.ivOldIcon.setImageDrawable(item.targetAppIcon)
                binding.ivAdd.visibility = View.GONE
            } else {
                binding.ivOldIcon.visibility = View.GONE
                binding.ivAdd.visibility = View.VISIBLE
            }

            // Click container to change target app
            binding.llOldIcon.setOnClickListener {
                onChangeApp(item)
            }

            // Lock / Install UI: lock/gem views are hidden, install buttons always visible
            binding.unlockView.root.visibility = View.GONE
            binding.llInstall.visibility = View.VISIBLE

            binding.llInstall.setOnClickListener {
                onInstallClick(item)
            }
        }
    }
}
