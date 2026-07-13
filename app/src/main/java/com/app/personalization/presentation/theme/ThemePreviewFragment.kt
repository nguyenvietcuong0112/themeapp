package com.app.personalization.presentation.theme

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.personalization.R
import com.app.personalization.data.ResourceConfig
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.di.ServiceLocator
import com.app.personalization.presentation.customviews.DownloadThemeButtonView
import com.app.personalization.presentation.widget.DownloadThemeActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemePreviewFragment : Fragment() {

    private lateinit var themeId: String
    private var theme: KeyboardTheme? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeId = arguments?.getString("theme_id") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val ivPreviewCustom = view.findViewById<ImageView>(R.id.ivPreviewCustom)
        val llDownload = view.findViewById<DownloadThemeButtonView>(R.id.llDownload)

        // Apply native blur filter to background image on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            thumbnailImageView?.setRenderEffect(
                android.graphics.RenderEffect.createBlurEffect(
                    25f, 25f, android.graphics.Shader.TileMode.CLAMP
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val themeItem = withContext(Dispatchers.IO) {
                if (themeId.startsWith("default_")) {
                    val path = themeId.substringAfter("default_")
                    val cat = path.substringBefore("/")
                    val name = path.substringAfter("/").replace("-", " ").replaceFirstChar { it.uppercase() }
                    KeyboardTheme(id = themeId, categoryId = cat.lowercase(), name = name, path = path, rawType = "default")
                } else {
                    ServiceLocator.getThemeDao(context).getThemeById(themeId)
                }
            }

            theme = themeItem

            themeItem?.let { item ->
                llDownload?.setText("Apply")

                val previewUrl = ResourceConfig.getWidgetPreviewUrl(context, item.path)

                // Load blurred background
                if (thumbnailImageView != null) {
                    Glide.with(context)
                        .load(previewUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_default_placeholder)
                        .into(thumbnailImageView)
                }

                // Load mockup preview
                if (imageView != null) {
                    Glide.with(context)
                        .load(previewUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_default_placeholder)
                        .into(imageView)
                }

                llDownload?.setOnClickListener {
                    val intent = Intent(context, DownloadThemeActivity::class.java).apply {
                        putExtra("theme_id", item.id)
                        putExtra("theme_name", item.name)
                        putExtra("theme_path", item.path)
                        putExtra("theme_type", item.rawType)
                    }
                    startActivity(intent)
                    activity?.finish()
                }
            }
        }
    }

    companion object {
        fun newInstance(themeId: String): ThemePreviewFragment {
            return ThemePreviewFragment().apply {
                arguments = Bundle().apply {
                    putString("theme_id", themeId)
                }
            }
        }
    }
}
