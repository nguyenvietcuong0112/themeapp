package com.app.personalization.presentation.theme

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.app.personalization.R
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.ResourceConfig
import java.io.File

class ThemeAdapter(
    private val onThemeClick: (KeyboardTheme) -> Unit,
    private val onDeleteClick: ((KeyboardTheme) -> Unit)? = null
) : RecyclerView.Adapter<ThemeAdapter.ViewHolder>() {

    private var items = listOf<KeyboardTheme>()

    fun submitList(list: List<KeyboardTheme>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_theme_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onThemeClick, onDeleteClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
        private val ivPreviewCustom: ImageView = view.findViewById(R.id.ivPreviewCustom)
        private val ivDelete: View = view.findViewById(R.id.ivDelete)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val cardView: View = view.findViewById(R.id.cardView)

        fun bind(
            theme: KeyboardTheme,
            onClick: (KeyboardTheme) -> Unit,
            onDelete: ((KeyboardTheme) -> Unit)?
        ) {
            val context = itemView.context
            tvName.text = theme.name
            tvName.visibility = View.VISIBLE

            if (theme.rawType == "diy") {
                ivDelete.visibility = if (onDelete != null) View.VISIBLE else View.GONE
                ivPreview.visibility = View.GONE
                ivPreviewCustom.visibility = View.VISIBLE

                if (!theme.backgroundPath.isNullOrEmpty()) {
                    val file = File(theme.backgroundPath)
                    if (file.exists()) {
                        Glide.with(context)
                            .load(file)
                            .centerCrop()
                            .into(ivPreviewCustom)
                    } else {
                        setDefaultDiyBackground(theme)
                    }
                } else {
                    setDefaultDiyBackground(theme)
                }

                ivDelete.setOnClickListener {
                    onDelete?.invoke(theme)
                }
            } else {
                ivDelete.visibility = View.GONE
                ivPreview.visibility = View.VISIBLE
                ivPreviewCustom.visibility = View.GONE

                val localDrawable = theme.generateLocalThemePreview(context)
                val previewUrl = ResourceConfig.getWidgetPreviewUrl(context, theme.path)
                val themeFolder = ResourceConfig.getThemeFolderByPath(context, theme.path)
                val localAssetPath = "theme_decorates/$themeFolder/bg_preview.png"

                val assetExists = try {
                    context.assets.open(localAssetPath).use { }
                    true
                } catch (e: Exception) {
                    false
                }

                val glideRequest = if (previewUrl.isNotEmpty()) {
                    Glide.with(context).load(previewUrl)
                } else {
                    Glide.with(context).load(if (assetExists) "file:///android_asset/$localAssetPath" else localDrawable)
                }

                glideRequest
                    .placeholder(R.drawable.bg_default_placeholder)
                    .error(
                        Glide.with(context)
                            .load(if (assetExists) "file:///android_asset/$localAssetPath" else localDrawable)
                            .placeholder(R.drawable.bg_default_placeholder)
                            .error(localDrawable)
                    )
                    .into(ivPreview)
            }

            cardView.setOnClickListener {
                onClick(theme)
            }
        }

        private fun setDefaultDiyBackground(theme: KeyboardTheme) {
            val config = theme.themeConfig
            val colorStr = config?.key?.customStyle?.backgroundColor ?: "#1E1E2E"
            val color = try { Color.parseColor(colorStr) } catch (e: Exception) { 0xFF1E1E2E.toInt() }
            
            ivPreviewCustom.setImageDrawable(GradientDrawable().apply {
                setColor(color)
                cornerRadius = 16f
            })
        }
    }
}
