package com.themes.diy.widgets.keyboard.controlcenter.feature_widget

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.data.entity.KeyboardTheme
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper
import com.themes.diy.widgets.keyboard.controlcenter.databinding.FragmentDownloadWallpaperBinding
import com.themes.diy.widgets.keyboard.controlcenter.core.di.ServiceLocator
import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.DownloadThemeWallpaperBottomSheet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadWallpaperFragment : Fragment() {

    private var _binding: FragmentDownloadWallpaperBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: DownloadThemeViewModel
    private lateinit var theme: KeyboardTheme
    private var wallpaper: WidgetThemeWallpaper? = null

    companion object {
        fun newInstance(theme: KeyboardTheme): DownloadWallpaperFragment {
            return DownloadWallpaperFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("theme", theme)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(DownloadThemeViewModel::class.java)
        theme = arguments?.getSerializable("theme") as? KeyboardTheme
            ?: throw IllegalArgumentException("Theme required")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadWallpaperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbCreate.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val matched = withContext(Dispatchers.IO) {
                    try {
                        val uuid = java.util.UUID.fromString(theme.id)
                        val diyList = com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.ThemeDatabase.getDatabase(requireContext()).wallpaperDao().getWallpapersByTheme(uuid)
                        if (diyList.isNotEmpty()) {
                            val diyWp = diyList[0]
                            WidgetThemeWallpaper(
                                id = diyWp.id.toString(),
                                themeId = diyWp.themeId.toString(),
                                name = diyWp.imageName,
                                order = 1,
                                folder = diyWp.folder,
                                imageBg = diyWp.imageName
                            )
                        } else {
                            WidgetThemeWallpaper(
                                id = "wp_${theme.id}",
                                themeId = theme.id,
                                name = theme.name,
                                order = 1,
                                folder = theme.path,
                                imageBg = "bg_wallpaper"
                            )
                        }
                    } catch (e: Exception) {
                        WidgetThemeWallpaper(
                            id = "wp_${theme.id}",
                            themeId = theme.id,
                            name = theme.name,
                            order = 1,
                            folder = theme.path,
                            imageBg = "bg_wallpaper"
                        )
                    }
                }
                wallpaper = matched
                setupUI()
            } catch (e: Exception) {
                e.printStackTrace()
                binding.pbCreate.visibility = View.GONE
                Toast.makeText(context, "Failed to load theme wallpaper details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var downloadedBitmap: Bitmap? = null

    private fun setupUI() {
        val wp = wallpaper
        if (wp == null) {
            binding.pbCreate.visibility = View.GONE
            Toast.makeText(context, "No wallpaper found for this theme", Toast.LENGTH_SHORT).show()
            return
        }

        binding.pbCreate.visibility = View.GONE

        // Load preview
        val cdnPreviewUrl = wp.getOnlinePreviewUri(requireContext())
        val localFallbackUri = wp.getImageUri()

        val glideRequest = if (cdnPreviewUrl != null && cdnPreviewUrl.toString().isNotEmpty()) {
            Glide.with(this).load(cdnPreviewUrl)
        } else {
            Glide.with(this).load(localFallbackUri)
        }

        glideRequest
            .placeholder(R.drawable.bg_default_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(
                Glide.with(this)
                    .load(localFallbackUri)
                    .placeholder(R.drawable.bg_default_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            )
            .into(binding.ivPreview)

        // Setup Download / Set Wallpaper action button
        updateButtonState(isDownloaded = downloadedBitmap != null)

        binding.btnAction.setOnClickListener {
            val bmp = downloadedBitmap
            if (bmp != null) {
                showSetWallpaperSheet(bmp)
            } else {
                downloadWallpaper(wp)
            }
        }
    }

    private fun updateButtonState(isDownloaded: Boolean) {
        if (isDownloaded) {
            binding.tvAction.text = "Set Wallpaper"
            binding.btnAction.setBackgroundResource(R.drawable.bg_btn_theme_set_wallpaper)
            binding.tvAction.setTextColor(android.graphics.Color.WHITE)
        } else {
            binding.tvAction.text = "Download"
            binding.btnAction.setBackgroundResource(R.drawable.bg_btn_theme_download_outline)
            binding.tvAction.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.brand_primary))
        }
    }

    private fun downloadWallpaper(wp: WidgetThemeWallpaper) {
        val downloadDialog = DownloadDialogFragment()
        downloadDialog.setParams(wp.getOnlineImageUri(requireContext()).toString(), object : DownloadDialogFragment.DownloadCallback {
            override fun onDownloadComplete(bitmap: Bitmap) {
                downloadedBitmap = bitmap
                updateButtonState(isDownloaded = true)
            }

            override fun onDownloadFailed() {
                Toast.makeText(context, "Failed to download wallpaper", Toast.LENGTH_SHORT).show()
            }
        })
        downloadDialog.show(parentFragmentManager, "download")
    }

    private fun showSetWallpaperSheet(bitmap: Bitmap) {
        val sheet = DownloadThemeWallpaperBottomSheet()
        sheet.setCallback(object : DownloadThemeWallpaperBottomSheet.Callback {
            override fun onApply(flag: Int) {
                applyWallpaper(bitmap, flag)
            }
        })
        sheet.show(childFragmentManager, "set_wallpaper")
    }

    private fun applyWallpaper(bitmap: Bitmap, flag: Int) {
        binding.pbCreate.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val wm = WallpaperManager.getInstance(requireContext())
                if (flag == 0) {
                    // System and Lock
                    wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                } else {
                    wm.setBitmap(bitmap, null, true, flag)
                }
                withContext(Dispatchers.Main) {
                    binding.pbCreate.visibility = View.GONE
                    SetupSucceedDialogFragment().show(parentFragmentManager, "success")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    binding.pbCreate.visibility = View.GONE
                    Toast.makeText(context, "Failed to apply wallpaper", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
