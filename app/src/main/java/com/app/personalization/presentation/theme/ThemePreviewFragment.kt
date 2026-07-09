package com.app.personalization.presentation.theme

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.personalization.R
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.di.ServiceLocator
import com.starnest.widget.ui.theme.widget.DownloadThemeButtonView
import com.bumptech.glide.Glide
import java.io.File
import androidx.lifecycle.lifecycleScope
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val ivPreviewCustom = view.findViewById<ImageView>(R.id.ivPreviewCustom)
        val llDownload = view.findViewById<DownloadThemeButtonView>(R.id.llDownload)

        viewLifecycleOwner.lifecycleScope.launch {
            val activeTheme = withContext(Dispatchers.IO) {
                if (themeId.startsWith("default_")) {
                    val path = themeId.substringAfter("default_")
                    val cat = path.substringBefore("/")
                    val name = path.substringAfter("/").replace("-", " ").replaceFirstChar { it.uppercase() }
                    KeyboardTheme(id = themeId, categoryId = cat.lowercase(), name = name, path = path, rawType = "default")
                } else {
                    ServiceLocator.getThemeDao(context).getThemeById(themeId)
                }
            }

            theme = activeTheme

            // Hide the activity's loading progress bar
            activity?.findViewById<View>(R.id.pbLoading)?.visibility = View.GONE

            activeTheme?.let { themeItem ->
                llDownload?.setText("Apply")

                if (themeItem.rawType == "diy") {
                    imageView?.visibility = View.GONE
                    ivPreviewCustom?.visibility = View.VISIBLE

                    if (!themeItem.backgroundPath.isNullOrEmpty()) {
                        val file = File(themeItem.backgroundPath)
                        if (file.exists()) {
                            Glide.with(context).load(file).centerCrop().into(ivPreviewCustom)
                        } else {
                            setDefaultDiyBackground(themeItem, ivPreviewCustom)
                        }
                    } else {
                        setDefaultDiyBackground(themeItem, ivPreviewCustom)
                    }
                } else {
                    imageView?.visibility = View.VISIBLE
                    ivPreviewCustom?.visibility = View.GONE

                    val assetManager = context.assets
                    val baseAssetPath = "theme_decorates/${themeItem.path}"
                    val possiblePaths = listOf(
                        "$baseAssetPath/keyboard_background.png",
                        "$baseAssetPath/key/preview.png",
                        "$baseAssetPath/popup_background.png"
                    )

                    var loaded = false
                    for (path in possiblePaths) {
                        try {
                            assetManager.open(path).use {
                                Glide.with(context)
                                    .load("file:///android_asset/$path")
                                    .centerCrop()
                                    .into(imageView)
                                loaded = true
                            }
                        } catch (e: Exception) {}
                        if (loaded) break
                    }

                    if (!loaded) {
                        val color = themeItem.tintColor(context)
                        imageView?.setImageDrawable(GradientDrawable().apply {
                            setColor(color)
                        })
                    }
                }

                llDownload?.setOnClickListener {
                    applyTheme(context, themeItem)
                }
            }
        }
    }

    private fun setDefaultDiyBackground(theme: KeyboardTheme, iv: ImageView) {
        val config = theme.themeConfig
        val colorStr = config?.key?.customStyle?.backgroundColor ?: "#1E1E2E"
        val color = try { Color.parseColor(colorStr) } catch (e: Exception) { 0xFF1E1E2E.toInt() }
        iv.setImageDrawable(GradientDrawable().apply {
            setColor(color)
            cornerRadius = 16f
        })
    }

    private fun applyTheme(context: Context, theme: KeyboardTheme) {
        val prefs = context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("selected_theme_id", theme.id)
            putString("current_theme_path", theme.path)
            putString("current_theme_type", theme.rawType)
            apply()
        }

        val intent = Intent("com.app.personalization.ACTION_THEME_CHANGED").apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)

        Toast.makeText(context, "Theme applied successfully!", Toast.LENGTH_SHORT).show()
        activity?.finish()
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
