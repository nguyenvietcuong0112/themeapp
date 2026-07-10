package com.app.personalization.presentation.theme

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import com.app.personalization.presentation.editor.SetWallpaperBottomSheet
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.app.personalization.presentation.widget.RemoteImageView

class DownloadWallpaperFragment : Fragment() {

    private var _binding: FragmentDownloadWallpaperBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: DownloadThemeViewModel
    private lateinit var theme: KeyboardTheme
    private var wallpaper: WidgetThemeWallpaper? = null

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
        
        // Load matching wallpaper for this theme folder
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = ServiceLocator.getWallpaperDao(requireContext())
            val wallpapers = dao.getAllWallpapers()
            val themeFolder = theme.path.substringBefore("/")
            
            val matched = wallpapers.firstOrNull { 
                it.folder.equals(themeFolder, ignoreCase = true) 
            } ?: wallpapers.firstOrNull()
            
            withContext(Dispatchers.Main) {
                wallpaper = matched
                setupUI()
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
        Glide.with(this)
            .load(wp.getImageUri())
            .placeholder(R.drawable.bg_overlay_wallpaper)
            .into(binding.ivPreview)

        // Hide ads button as requested
        binding.llPlayVideo.visibility = View.GONE
        binding.actionView.llPlayVideo.root.visibility = View.GONE

        // Setup Coins / Gems Pricing UI
        binding.actionView.tvCoin.text = "100"

        updateLockUnlockState()

        binding.actionView.llAddCoin.setOnClickListener {
            if (viewModel.deductCoins(100)) {
                viewModel.unlockWallpaper(wp.id)
                updateLockUnlockState()
                Toast.makeText(context, "Wallpaper unlocked successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Not enough coins! Please earn or buy more.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.actionView.llGetAll.setOnClickListener {
            Toast.makeText(context, "Unlock premium from store to get all!", Toast.LENGTH_SHORT).show()
        }

        binding.actionView.clInstall.setOnClickListener {
            installWallpaper(wp)
        }
    }

    private fun updateLockUnlockState() {
        val wp = wallpaper ?: return
        val isUnlocked = viewModel.isWallpaperUnlocked(wp.id, wp.isFree)

        if (isUnlocked) {
            binding.actionView.llAction.visibility = View.GONE
            binding.actionView.clInstall.visibility = View.VISIBLE
        } else {
            binding.actionView.llAction.visibility = View.VISIBLE
            binding.actionView.clInstall.visibility = View.GONE
        }
    }

    private fun installWallpaper(wp: WidgetThemeWallpaper) {
        binding.pbCreate.visibility = View.VISIBLE
        Glide.with(this)
            .asBitmap()
            .load(wp.getImageUri())
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.pbCreate.visibility = View.GONE
                    
                    // Save locally for offline caching
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val context = requireContext()
                            val file = wp.getFile(context)
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { out ->
                                resource.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // Show bottom sheet to choose set target
                    val sheet = SetWallpaperBottomSheet(resource)
                    sheet.show(childFragmentManager, "set_wallpaper")
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    binding.pbCreate.visibility = View.GONE
                    Toast.makeText(context, "Failed to download wallpaper", Toast.LENGTH_SHORT).show()
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(theme: KeyboardTheme): DownloadWallpaperFragment {
            return DownloadWallpaperFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("theme", theme)
                }
            }
        }
    }
}
