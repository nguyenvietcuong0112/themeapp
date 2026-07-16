package com.app.personalization.presentation.wallpaper

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.Template
import com.app.personalization.di.ServiceLocator
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class DIYWallpaperActivity : AppCompatActivity() {

    private lateinit var wallpaperCanvas: DIYWallpaperCanvasView
    private lateinit var btnApplyWallpaper: View
    private lateinit var btnAddText: View
    private lateinit var btnAddSticker: View
    private lateinit var btnChangeBase: View
    private lateinit var btnFrame: View
    private lateinit var layoutTextControls: LinearLayout
    private lateinit var spinnerFont: Spinner
    private lateinit var rvTemplates: RecyclerView

    companion object {
        private const val REQUEST_PICK_BASE_IMAGE = 5001
        private const val REQUEST_PICK_STICKER_IMAGE = 5002
        private const val REQUEST_PICK_USER_IMAGE = 5003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallpaper)

        // Clear Glide disk cache to purge any corrupted HTML fallbacks cached as PNGs
        Thread {
            try {
                Glide.get(applicationContext).clearDiskCache()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        wallpaperCanvas = findViewById(R.id.wallpaperCanvas)
        btnApplyWallpaper = findViewById(R.id.tvSave)
        btnAddText = findViewById(R.id.tabText)
        btnAddSticker = findViewById(R.id.tabSticker)
        btnChangeBase = findViewById(R.id.tabBackground)
        btnFrame = findViewById(R.id.tabFrame)
        layoutTextControls = findViewById(R.id.layoutTextControls)
        spinnerFont = findViewById(R.id.spinnerFont)
        rvTemplates = findViewById(R.id.rvTemplates)

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        findViewById<View>(R.id.ivUndo)?.setOnClickListener {
            wallpaperCanvas.undo()
        }
        findViewById<View>(R.id.ivRedo)?.setOnClickListener {
            wallpaperCanvas.redo()
        }
        findViewById<View>(R.id.ivDownload)?.setOnClickListener {
            downloadWallpaper()
        }

        setupFontSpinner()
        setupButtons()
        setupTemplatesSelector()
    }

    private fun setupTemplatesSelector() {
        rvTemplates.layoutManager = GridLayoutManager(this, 3)

        lifecycleScope.launch(Dispatchers.IO) {
            val templateDao = ServiceLocator.getTemplateDao(this@DIYWallpaperActivity)
            var list = templateDao.getAllTemplates()

            // Filter out default non-collage templates, check if we need to pre-populate templates
            val diyList = list.filter { 
                it.templateFolder.startsWith("template") && 
                !it.templateFolder.contains("clock") && 
                !it.templateFolder.contains("hud") && 
                !it.templateFolder.contains("particle") 
            }

            val finalList = if (diyList.isEmpty()) {
                val newList = (1..33).map {
                    Template(
                        id = "tmpl_diy_$it",
                        name = "Template $it",
                        templateFolder = "template$it",
                        isLive = false,
                        isFree = true
                    )
                }
                templateDao.insertTemplates(newList)
                newList
            } else {
                diyList
            }

            withContext(Dispatchers.Main) {
                rvTemplates.adapter = TemplateAdapter(finalList) { selectedTemplate ->
                    rvTemplates.visibility = View.GONE
                    wallpaperCanvas.loadTemplate(selectedTemplate.templateFolder)
                }
            }
        }

        // Setup image templates click listener inside Canvas to load user selected photos
        wallpaperCanvas.onFrameClickListener = { clickedLayer ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_PICK_USER_IMAGE)
        }
    }

    private fun setupFontSpinner() {
        try {
            val fonts = mutableListOf("normal")
            val assetFonts = assets.list("fonts")?.filter { it.endsWith(".ttf") || it.endsWith(".otf") } ?: emptyList()
            fonts.addAll(assetFonts)

            val cdnFonts = listOf("Beautiful.ttf", "Handwritten.ttf", "Modern.ttf")
            fonts.addAll(cdnFonts)

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fonts)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFont.adapter = adapter

            spinnerFont.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedFont = fonts[position]
                    val activeTextLayer = wallpaperCanvas.getActiveTextLayer()
                    if (activeTextLayer != null) {
                        layoutTextControls.visibility = View.VISIBLE
                        activeTextLayer.fontName = selectedFont
                        if (selectedFont == "normal") {
                            activeTextLayer.typeface = android.graphics.Typeface.DEFAULT
                            wallpaperCanvas.invalidate()
                        } else if (cdnFonts.contains(selectedFont)) {
                            val cacheFile = java.io.File(cacheDir, selectedFont)
                            if (cacheFile.exists()) {
                                activeTextLayer.typeface = android.graphics.Typeface.createFromFile(cacheFile)
                                wallpaperCanvas.invalidate()
                            } else {
                                val fontUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/templates/font/$selectedFont"
                                lifecycleScope.launch(Dispatchers.IO) {
                                    try {
                                        val url = java.net.URL(fontUrl)
                                        url.openStream().use { input ->
                                            java.io.FileOutputStream(cacheFile).use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            activeTextLayer.typeface = android.graphics.Typeface.createFromFile(cacheFile)
                                            wallpaperCanvas.invalidate()
                                            Toast.makeText(this@DIYWallpaperActivity, "Font downloaded!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@DIYWallpaperActivity, "Failed to download font", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        } else {
                            activeTextLayer.typeface = try {
                                android.graphics.Typeface.createFromAsset(assets, "fonts/$selectedFont")
                            } catch (e: Exception) {
                                android.graphics.Typeface.DEFAULT
                            }
                            wallpaperCanvas.invalidate()
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupButtons() {
        btnApplyWallpaper.setOnClickListener {
            applyWallpaper()
        }

        btnAddText.setOnClickListener {
            showAddTextDialog()
        }

        btnAddSticker.setOnClickListener {
            showStickerPickerDialog()
        }

        btnChangeBase.setOnClickListener {
            showBaseLayerDialog()
        }

        btnFrame.setOnClickListener {
            rvTemplates.visibility = View.VISIBLE
        }
    }

    private fun showAddTextDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Custom Text Layer")

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val etInputText = EditText(this).apply {
            hint = "Enter text..."
        }
        root.addView(etInputText)

        val etTextColor = EditText(this).apply {
            hint = "Color Hex (e.g. #00E5FF)"
            setText("#00E5FF")
        }
        root.addView(etTextColor)

        val tvSize = TextView(this).apply {
            text = "Font Size: 48sp"
        }
        root.addView(tvSize)

        val sbSize = SeekBar(this).apply {
            max = 120
            progress = 48
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvSize.text = "Font Size: ${progress}sp"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        root.addView(sbSize)

        builder.setView(root)
        builder.setPositiveButton("Add") { dialog, _ ->
            val text = etInputText.text.toString().trim()
            val colorStr = etTextColor.text.toString().trim()
            val color = try { Color.parseColor(colorStr) } catch (e: Exception) { Color.WHITE }
            val size = sbSize.progress.toFloat() * resources.displayMetrics.scaledDensity

            if (text.isNotEmpty()) {
                val activeFont = spinnerFont.selectedItem?.toString() ?: "normal"
                wallpaperCanvas.addTextLayer(text, activeFont, color, size)
                layoutTextControls.visibility = View.VISIBLE
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showStickerPickerDialog() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbStickers = try {
                ServiceLocator.getStickerDao(this@DIYWallpaperActivity).getAllStickers()
            } catch (e: Exception) {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                if (dbStickers.isEmpty()) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    }
                    startActivityForResult(intent, REQUEST_PICK_STICKER_IMAGE)
                    return@withContext
                }

                val options = dbStickers.map { it.name }.toMutableList().apply {
                    add("Choose from Gallery...")
                }.toTypedArray()

                AlertDialog.Builder(this@DIYWallpaperActivity)
                    .setTitle("Select Sticker")
                    .setItems(options) { dialog, which ->
                        if (which == options.size - 1) {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "image/*"
                            }
                            startActivityForResult(intent, REQUEST_PICK_STICKER_IMAGE)
                        } else {
                            val selectedSticker = dbStickers[which]
                            val cdnUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/templates/${selectedSticker.category}/${selectedSticker.imageName}"
                            
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    val bmp = Glide.with(this@DIYWallpaperActivity)
                                        .asBitmap()
                                        .load(cdnUrl)
                                        .submit()
                                        .get()
                                    if (bmp != null) {
                                        withContext(Dispatchers.Main) {
                                            wallpaperCanvas.addStickerLayer(bmp)
                                            Toast.makeText(this@DIYWallpaperActivity, "Sticker added!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@DIYWallpaperActivity, "Failed to load sticker from CDN", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun showBaseLayerDialog() {
        val choices = arrayOf("Solid Color", "Linear Gradient", "Gallery Image", "CDN Canvas Background")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Base Background Type")
        builder.setItems(choices) { dialog, which ->
            when (which) {
                0 -> showSolidColorPickDialog()
                1 -> showGradientColorPickDialog()
                2 -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    }
                    startActivityForResult(intent, REQUEST_PICK_BASE_IMAGE)
                }
                3 -> showCDNBackgroundPickDialog()
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showCDNBackgroundPickDialog() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbBackgrounds = try {
                ServiceLocator.getBackgroundDao(this@DIYWallpaperActivity).getAllBackgrounds()
            } catch (e: Exception) {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                if (dbBackgrounds.isEmpty()) {
                    Toast.makeText(this@DIYWallpaperActivity, "No CDN backgrounds available", Toast.LENGTH_SHORT).show()
                    return@withContext
                }

                val options = dbBackgrounds.map { it.name }.toTypedArray()
                AlertDialog.Builder(this@DIYWallpaperActivity)
                    .setTitle("Select CDN Background")
                    .setItems(options) { dialog, which ->
                        val selectedBg = dbBackgrounds[which]
                        val cdnUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/templates/${selectedBg.category}/${selectedBg.imageName}"

                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val bmp = Glide.with(this@DIYWallpaperActivity)
                                    .asBitmap()
                                    .load(cdnUrl)
                                    .submit()
                                    .get()
                                if (bmp != null) {
                                    withContext(Dispatchers.Main) {
                                        wallpaperCanvas.setBackgroundImage(bmp)
                                        Toast.makeText(this@DIYWallpaperActivity, "Background applied!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@DIYWallpaperActivity, "Failed to load background from CDN", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun showSolidColorPickDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Background Color")
        val etColor = EditText(this).apply {
            setText("#12121A")
            setPadding(48, 48, 48, 48)
        }
        builder.setView(etColor)
        builder.setPositiveButton("Apply") { dialog, _ ->
            val colorStr = etColor.text.toString().trim()
            val color = try { Color.parseColor(colorStr) } catch (e: Exception) { 0xFF12121A.toInt() }
            wallpaperCanvas.setBackgroundSolid(color)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showGradientColorPickDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Gradient Colors")
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        val etStart = EditText(this).apply {
            setText("#00E5FF")
        }
        val etEnd = EditText(this).apply {
            setText("#7C4DFF")
        }
        root.addView(etStart)
        root.addView(etEnd)
        builder.setView(root)
        builder.setPositiveButton("Apply") { dialog, _ ->
            val start = try { Color.parseColor(etStart.text.toString().trim()) } catch (e: Exception) { 0xFF00E5FF.toInt() }
            val end = try { Color.parseColor(etEnd.text.toString().trim()) } catch (e: Exception) { 0xFF7C4DFF.toInt() }
            wallpaperCanvas.setBackgroundGradient(start, end)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { uri ->
                try {
                    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                    val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                    parcelFileDescriptor?.close()

                    if (bitmap != null) {
                        when (requestCode) {
                            REQUEST_PICK_BASE_IMAGE -> {
                                wallpaperCanvas.setBackgroundImage(bitmap)
                            }
                            REQUEST_PICK_STICKER_IMAGE -> {
                                wallpaperCanvas.addStickerLayer(bitmap)
                            }
                            REQUEST_PICK_USER_IMAGE -> {
                                wallpaperCanvas.setUserImageForActiveFrame(uri, bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyWallpaper() {
        if (wallpaperCanvas.width == 0 || wallpaperCanvas.height == 0) {
            Toast.makeText(this, "Canvas size not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val bitmap = Bitmap.createBitmap(wallpaperCanvas.width, wallpaperCanvas.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            wallpaperCanvas.draw(canvas)

            val bottomSheet = SetWallpaperBottomSheet(bitmap) {
                finish()
            }
            bottomSheet.show(supportFragmentManager, "SetWallpaperBottomSheet")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to apply wallpaper: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun downloadWallpaper() {
        if (wallpaperCanvas.width == 0 || wallpaperCanvas.height == 0) {
            Toast.makeText(this, "Canvas size not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val bitmap = Bitmap.createBitmap(wallpaperCanvas.width, wallpaperCanvas.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            wallpaperCanvas.draw(canvas)

            val filename = "DIY_Wallpaper_${System.currentTimeMillis()}.png"
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        Toast.makeText(this, "Downloaded and saved to Gallery!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Failed to save wallpaper", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Failed to save wallpaper", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving wallpaper: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private class TemplateAdapter(
        private val list: List<Template>,
        private val onClick: (Template) -> Unit
    ) : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diy_template, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position], onClick)
        }

        override fun getItemCount(): Int = list.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            private val tvName: TextView = view.findViewById(R.id.tvName)

            fun bind(item: Template, onClick: (Template) -> Unit) {
                tvName.text = item.name
                
                // CDN path for preview: https://csc-themeapp-widget.pages.dev/templates/[folder]/preview.png
                val previewUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/templates/${item.templateFolder}/preview.png"
                
                Glide.with(itemView.context)
                    .load(previewUrl)
                    .placeholder(R.drawable.bg_default_placeholder)
                    .centerCrop()
                    .into(ivPreview)

                itemView.setOnClickListener {
                    onClick(item)
                }
            }
        }
    }
}
