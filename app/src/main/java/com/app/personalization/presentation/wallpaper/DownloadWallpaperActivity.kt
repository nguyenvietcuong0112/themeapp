package com.app.personalization.presentation.wallpaper

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.app.personalization.R
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import com.app.personalization.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadWallpaperActivity : AppCompatActivity() {

    private lateinit var wallpaper: WidgetThemeWallpaper
    private lateinit var ivFavorite: ImageView
    private lateinit var ivClose: ImageView
    private lateinit var pbLoading: ProgressBar
    private lateinit var viewPager: ViewPager2
    private lateinit var btnApply: View

    private var loadedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_wallpaper)

        wallpaper = intent.getSerializableExtra("wallpaper_item") as? WidgetThemeWallpaper
            ?: intent.getSerializableExtra("WIDGET_WALLPAPER") as? WidgetThemeWallpaper
            ?: return finish()

        initViews()
        loadWallpaperImage()
    }

    private fun initViews() {
        ivFavorite = findViewById(R.id.ivFavorite)
        ivClose = findViewById(R.id.ivClose)
        pbLoading = findViewById(R.id.pbLoading)
        viewPager = findViewById(R.id.viewPager)
        btnApply = findViewById(R.id.btnApply)

        ivClose.setOnClickListener {
            finish()
        }

        updateFavoriteUI()
        ivFavorite.setOnClickListener {
            toggleFavorite()
        }

        btnApply.setOnClickListener {
            val bitmap = loadedBitmap
            if (bitmap != null) {
                val sheet = SetWallpaperBottomSheet(bitmap)
                sheet.show(supportFragmentManager, "set_wallpaper_sheet")
            } else {
                Toast.makeText(this, "Wallpaper is still downloading...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = ServiceLocator.getWallpaperDao(this@DownloadWallpaperActivity)
            wallpaper.isFavorite = !wallpaper.isFavorite
            db.updateWallpaper(wallpaper)
            withContext(Dispatchers.Main) {
                updateFavoriteUI()
                Toast.makeText(this@DownloadWallpaperActivity, "Favorite updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteUI() {
        if (wallpaper.isFavorite) {
            ivFavorite.imageTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#FF4081")
            )
        } else {
            ivFavorite.imageTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#FFFFFF")
            )
        }
    }

    private fun loadWallpaperImage() {
        pbLoading.visibility = View.VISIBLE
        lifecycleScope.launch {
            val bitmap = wallpaper.getImageBg(this@DownloadWallpaperActivity)
            pbLoading.visibility = View.GONE
            if (bitmap != null) {
                loadedBitmap = bitmap
                setupViewPager(bitmap)
            } else {
                Toast.makeText(this@DownloadWallpaperActivity, "Failed to load high resolution wallpaper", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupViewPager(bitmap: Bitmap) {
        viewPager.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<ImageHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
                val img = ImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                return ImageHolder(img)
            }

            override fun onBindViewHolder(holder: ImageHolder, position: Int) {
                holder.imageView.setImageBitmap(bitmap)
            }

            override fun getItemCount(): Int = 1
        }
    }

    class ImageHolder(val imageView: ImageView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(imageView)
}
