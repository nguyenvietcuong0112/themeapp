package com.app.personalization.presentation.widget

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
import com.app.personalization.R
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import com.app.personalization.databinding.FragmentDownloadWallpaperBinding
import com.app.personalization.di.ServiceLocator
import com.app.personalization.presentation.theme.DownloadThemeWallpaperBottomSheet
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
                    val dao = ServiceLocator.getWallpaperDao(requireContext())
                    val wallpapers = dao.getAllWallpapers()
                    val themeFolder = theme.path.substringBefore("/")
                    wallpapers.firstOrNull { 
                        it.folder.equals(themeFolder, ignoreCase = true) 
                    } ?: wallpapers.firstOrNull()
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



        // Always show install button
        binding.actionView.clInstall.visibility = View.VISIBLE
        binding.actionView.tvInstall.text = "Set Wallpaper"

        binding.actionView.clInstall.setOnClickListener {
            downloadAndApplyWallpaper(wp)
        }
    }

    private fun downloadAndApplyWallpaper(wp: WidgetThemeWallpaper) {
        val downloadDialog = DownloadDialogFragment()
        downloadDialog.setParams(wp.getOnlineImageUri(requireContext()).toString(), object : DownloadDialogFragment.DownloadCallback {
            override fun onDownloadComplete(bitmap: Bitmap) {
                // Show bottom sheet to choose set target
                val sheet = DownloadThemeWallpaperBottomSheet()
                sheet.setCallback(object : DownloadThemeWallpaperBottomSheet.Callback {
                    override fun onApply(flag: Int) {
                        applyWallpaper(bitmap, flag)
                    }
                })
                sheet.show(childFragmentManager, "set_wallpaper")
            }

            override fun onDownloadFailed() {
                Toast.makeText(context, "Failed to download wallpaper", Toast.LENGTH_SHORT).show()
            }
        })
        downloadDialog.show(parentFragmentManager, "download")
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
