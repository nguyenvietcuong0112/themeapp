package com.app.personalization.presentation.theme

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.ActivityCreateThemeBinding
import com.app.personalization.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CreateThemeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateThemeBinding
    private lateinit var viewModel: CreateThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CreateThemeViewModel::class.java]

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.ivClose.setOnClickListener {
            finish()
        }

        binding.ivInfo.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun setupListeners() {
        // Toolbar actions
        binding.actionView.llWallpaper.setOnClickListener {
            val wallpaperSheet = ChangeCreateThemeWallpaperBottomSheet()
            wallpaperSheet.show(supportFragmentManager, "wallpaper_picker")
        }

        binding.actionView.llIcon.setOnClickListener {
            val iconSheet = ChangeCreateThemeIconBottomSheet()
            iconSheet.show(supportFragmentManager, "icon_picker")
        }

        binding.actionView.llWidget.setOnClickListener {
            val widgetSheet = ChangeCreateThemeWidgetBottomSheet()
            widgetSheet.show(supportFragmentManager, "widget_picker")
        }

        binding.actionView.ivReset.setOnClickListener {
            viewModel.resetTheme()
            Toast.makeText(this, "Theme reset to default", Toast.LENGTH_SHORT).show()
        }

        binding.actionView.tvSave.setOnClickListener {
            saveTheme()
        }
    }

    private fun observeViewModel() {
        viewModel.wallpaperState.observe(this) { url ->
            binding.themeView.setWallpaper(url)
        }

        viewModel.widgetState.observe(this) { url ->
            binding.themeView.setWidget(url)
        }

        viewModel.iconPackState.observe(this) { icons ->
            binding.themeView.setIcons(icons)
        }
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Guide")
            .setMessage("Customize your home screen theme by choosing a wallpaper, icon pack, and widget from the bottom panel. Tap Save to apply your custom theme.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveTheme() {
        // Manually capture clView bitmap
        val targetView = binding.themeView.binding.clView
        if (targetView.width == 0 || targetView.height == 0) {
            Toast.makeText(this, "Preview not ready, please wait...", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = viewToBitmap(targetView)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Save bitmap to file system
                val themeId = UUID.randomUUID().toString()
                val filename = "theme_preview_$themeId.png"
                val file = File(filesDir, filename)
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                // Insert into original AppDatabase
                val customTheme = KeyboardTheme(
                    id = themeId,
                    name = "My Custom Theme ${themeId.take(4)}",
                    path = file.absolutePath,
                    rawType = "diy",
                    backgroundPath = file.absolutePath,
                    previewPath = file.absolutePath
                )
                ServiceLocator.getThemeDao(this@CreateThemeActivity).insertTheme(customTheme)

                // Insert into new ThemeDatabase
                val newWidgetTheme = com.app.personalization.data.database.entity.WidgetTheme(
                    id = UUID.fromString(themeId),
                    name = "My Custom Theme ${themeId.take(4)}",
                    folder = file.absolutePath,
                    order = 999,
                    isCustom = true
                )
                com.app.personalization.data.database.ThemeDatabase.getDatabase(this@CreateThemeActivity).themeDao().insertTheme(newWidgetTheme)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateThemeActivity, "Theme saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateThemeActivity, "Failed to save theme!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
