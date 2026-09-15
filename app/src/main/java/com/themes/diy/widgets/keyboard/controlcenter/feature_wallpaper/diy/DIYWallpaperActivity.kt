package com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.diy

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
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig
import com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.data.entity.Template
import com.themes.diy.widgets.keyboard.controlcenter.core.di.ServiceLocator
import com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.SetWallpaperBottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private lateinit var templateContainer: View

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

        templateContainer = findViewById(R.id.templateContainer)
        wallpaperCanvas = findViewById(R.id.wallpaperCanvas)
        btnApplyWallpaper = findViewById(R.id.tvSave)
        btnAddText = findViewById(R.id.tabText)
        btnAddSticker = findViewById(R.id.tabSticker)
        btnChangeBase = findViewById(R.id.tabBackground)
        btnFrame = findViewById(R.id.tabFrame)
        layoutTextControls = findViewById(R.id.layoutTextControls)
        spinnerFont = findViewById(R.id.spinnerFont)
        rvTemplates = findViewById(R.id.rvTemplates)

        findViewById<View>(R.id.ivBackTemplate)?.setOnClickListener { finish() }

        findViewById<View>(R.id.ivBack)?.setOnClickListener {
            if (templateContainer.visibility == View.GONE) {
                templateContainer.visibility = View.VISIBLE
            } else {
                finish()
            }
        }

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

    override fun onBackPressed() {
        if (templateContainer.visibility == View.GONE) {
            templateContainer.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    private fun setupTemplatesSelector() {
        rvTemplates.layoutManager = GridLayoutManager(this, 2)

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
                rvTemplates.adapter = TemplateAdapter(
                    list = finalList,
                    onBlankClick = {
                        templateContainer.visibility = View.GONE
                        wallpaperCanvas.initBlankCanvas()
                    },
                    onClick = { selectedTemplate ->
                        templateContainer.visibility = View.GONE
                        wallpaperCanvas.loadTemplate(selectedTemplate.templateFolder)
                    }
                )
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
                                val fontUrl = ResourceConfig.getExclusiveFontUrl(selectedFont)
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
            showStickerBottomSheet()
        }

        btnChangeBase.setOnClickListener {
            showBackgroundBottomSheet()
        }

        btnFrame.setOnClickListener {
            templateContainer.visibility = View.VISIBLE
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

    private fun showStickerBottomSheet() {
        val sheet = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_diy_sticker, null)
        sheet.setContentView(sheetView)

        val btnPickFromGallery = sheetView.findViewById<View>(R.id.btnPickFromGallery)
        val rvCategories = sheetView.findViewById<RecyclerView>(R.id.rvStickerCategories)
        val rvStickers = sheetView.findViewById<RecyclerView>(R.id.rvStickers)
        val pbLoading = sheetView.findViewById<View>(R.id.pbLoading)

        btnPickFromGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_PICK_STICKER_IMAGE)
            sheet.dismiss()
        }

        val categories = listOf("Cute", "Heart", "Animal", "Flowers", "Birthday", "Food&Drink", "Emoji", "Party", "Holiday", "Babies", "Graduation", "Wedding")
        var selectedCategory = categories.first()

        fun loadStickers(category: String) {
            pbLoading.visibility = View.VISIBLE
            rvStickers.visibility = View.GONE

            lifecycleScope.launch(Dispatchers.IO) {
                val stickerFiles = (1..20).map { "ic_${category}_$it.png" }

                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    rvStickers.visibility = View.VISIBLE
                    rvStickers.layoutManager = GridLayoutManager(this@DIYWallpaperActivity, 4)
                    rvStickers.adapter = StickerGridAdapter(category, stickerFiles) { cdnUrl ->
                        sheet.dismiss()
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
                            }
                        }
                    }
                }
            }
        }

        rvCategories.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rvCategories.adapter = CategoryChipAdapter(categories) { cat ->
            selectedCategory = cat
            loadStickers(cat)
        }

        loadStickers(selectedCategory)
        sheet.show()
    }

    private fun showBackgroundBottomSheet() {
        val sheet = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_diy_background, null)
        sheet.setContentView(sheetView)

        val btnPickFromGallery = sheetView.findViewById<View>(R.id.btnPickBgFromGallery)
        val rvCategories = sheetView.findViewById<RecyclerView>(R.id.rvBgCategories)
        val rvBackgrounds = sheetView.findViewById<RecyclerView>(R.id.rvBackgrounds)
        val pbLoading = sheetView.findViewById<View>(R.id.pbBgLoading)

        btnPickFromGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_PICK_BASE_IMAGE)
            sheet.dismiss()
        }

        val categories = listOf("Solid Colors", "Gradients", "Aesthetic", "Cute", "Texture", "Colorful", "Elegant")
        var selectedCategory = categories.first()

        val solidColors = listOf(
            Pair("#12121A", "Midnight"),
            Pair("#1E1E2E", "Dark Slate"),
            Pair("#2D3748", "Charcoal"),
            Pair("#FF6B6B", "Coral Pink"),
            Pair("#4ECDC4", "Mint"),
            Pair("#FFE66D", "Pastel Sun"),
            Pair("#6C5CE7", "Purple"),
            Pair("#A8E6CF", "Light Mint"),
            Pair("#FD79A8", "Rose"),
            Pair("#FFFFFF", "Pure White"),
            Pair("#F5F6FA", "Cloud"),
            Pair("#2C3E50", "Deep Navy")
        )

        val gradients = listOf(
            Pair("Neon Glow", Pair(0xFF00E5FF.toInt(), 0xFF7C4DFF.toInt())),
            Pair("Sunset", Pair(0xFFFF6B6B.toInt(), 0xFFFFBE53.toInt())),
            Pair("Dark Cosmic", Pair(0xFF12121A.toInt(), 0xFF3A3D52.toInt())),
            Pair("Ocean Mint", Pair(0xFF4ECDC4.toInt(), 0xFF556270.toInt())),
            Pair("Soft Dream", Pair(0xFFA18CD1.toInt(), 0xFFFBC2EB.toInt())),
            Pair("Sky Blue", Pair(0xFF84FAB0.toInt(), 0xFF8FD3F4.toInt()))
        )

        fun loadBackgroundTab(category: String) {
            when (category) {
                "Solid Colors" -> {
                    pbLoading.visibility = View.GONE
                    rvBackgrounds.visibility = View.VISIBLE
                    rvBackgrounds.layoutManager = GridLayoutManager(this@DIYWallpaperActivity, 3)
                    rvBackgrounds.adapter = ColorPaletteAdapter(solidColors) { colorHex ->
                        wallpaperCanvas.setBackgroundSolid(Color.parseColor(colorHex))
                        sheet.dismiss()
                    }
                }
                "Gradients" -> {
                    pbLoading.visibility = View.GONE
                    rvBackgrounds.visibility = View.VISIBLE
                    rvBackgrounds.layoutManager = GridLayoutManager(this@DIYWallpaperActivity, 3)
                    rvBackgrounds.adapter = GradientPaletteAdapter(gradients) { gradPair ->
                        wallpaperCanvas.setBackgroundGradient(gradPair.first, gradPair.second)
                        sheet.dismiss()
                    }
                }
                else -> {
                    pbLoading.visibility = View.VISIBLE
                    rvBackgrounds.visibility = View.GONE
                    lifecycleScope.launch(Dispatchers.IO) {
                        val bgFiles = (1..15).map { "bg_wallpaper_$it.png" }
                        withContext(Dispatchers.Main) {
                            pbLoading.visibility = View.GONE
                            rvBackgrounds.visibility = View.VISIBLE
                            rvBackgrounds.layoutManager = GridLayoutManager(this@DIYWallpaperActivity, 3)
                            rvBackgrounds.adapter = CdnBgGridAdapter(category, bgFiles) { cdnUrl ->
                                sheet.dismiss()
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        rvCategories.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rvCategories.adapter = CategoryChipAdapter(categories) { cat ->
            selectedCategory = cat
            loadBackgroundTab(cat)
        }

        loadBackgroundTab(selectedCategory)
        sheet.show()
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
        private val onBlankClick: () -> Unit,
        private val onClick: (Template) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_BLANK = 0
            private const val TYPE_TEMPLATE = 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) TYPE_BLANK else TYPE_TEMPLATE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == TYPE_BLANK) {
                val view = inflater.inflate(R.layout.item_diy_template_blank, parent, false)
                BlankViewHolder(view)
            } else {
                val view = inflater.inflate(R.layout.item_diy_template, parent, false)
                TemplateViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is BlankViewHolder) {
                holder.bind(onBlankClick)
            } else if (holder is TemplateViewHolder) {
                holder.bind(list[position - 1], onClick)
            }
        }

        override fun getItemCount(): Int = list.size + 1

        class BlankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(onBlankClick: () -> Unit) {
                itemView.setOnClickListener { onBlankClick() }
            }
        }

        class TemplateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            private val tvName: TextView = view.findViewById(R.id.tvName)
            private val tvBadge: TextView? = view.findViewById(R.id.tvBadge)

            fun bind(item: Template, onClick: (Template) -> Unit) {
                tvName.text = item.name
                val badgeText = when {
                    item.templateFolder.endsWith("1") || item.templateFolder.endsWith("7") -> "HOT"
                    item.templateFolder.endsWith("2") || item.templateFolder.endsWith("8") -> "STORY"
                    item.templateFolder.endsWith("3") || item.templateFolder.endsWith("9") -> "COLLAGE"
                    else -> "NEW"
                }
                tvBadge?.text = badgeText

                val previewUrl = ResourceConfig.getDiyPreviewUrl(item.templateFolder)

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

    private class CategoryChipAdapter(
        private val categories: List<String>,
        private val onSelect: (String) -> Unit
    ) : RecyclerView.Adapter<CategoryChipAdapter.ViewHolder>() {
        private var selectedIndex = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diy_sticker_category, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(categories[position], position == selectedIndex) {
                val prev = selectedIndex
                selectedIndex = position
                notifyItemChanged(prev)
                notifyItemChanged(selectedIndex)
                onSelect(categories[position])
            }
        }

        override fun getItemCount(): Int = categories.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tv: TextView = view.findViewById(R.id.tvCategoryName)
            fun bind(cat: String, isSelected: Boolean, onClick: () -> Unit) {
                tv.text = cat
                if (isSelected) {
                    tv.setBackgroundResource(R.drawable.bg_category_chip_selected)
                    tv.setTextColor(Color.WHITE)
                } else {
                    tv.setBackgroundResource(R.drawable.bg_category_chip_unselected)
                    tv.setTextColor(Color.parseColor("#1A1A1A"))
                }
                tv.setOnClickListener { onClick() }
            }
        }
    }

    private class StickerGridAdapter(
        private val category: String,
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<StickerGridAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diy_sticker_picker, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(category, items[position], onClick)
        }
        override fun getItemCount(): Int = items.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val iv: ImageView = view.findViewById(R.id.ivSticker)
            fun bind(cat: String, imageName: String, onClick: (String) -> Unit) {
                val url = ResourceConfig.getDiyStickerUrl(cat, imageName)
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(R.drawable.bg_default_placeholder)
                    .into(iv)
                itemView.setOnClickListener { onClick(url) }
            }
        }
    }

    private class ColorPaletteAdapter(
        private val colors: List<Pair<String, String>>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<ColorPaletteAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diy_color_circle, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(colors[position], onClick)
        }
        override fun getItemCount(): Int = colors.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val viewColor: View = view.findViewById(R.id.viewColor)
            private val tvLabel: TextView = view.findViewById(R.id.tvColorLabel)
            fun bind(item: Pair<String, String>, onClick: (String) -> Unit) {
                viewColor.setBackgroundColor(Color.parseColor(item.first))
                tvLabel.text = item.second
                itemView.setOnClickListener { onClick(item.first) }
            }
        }
    }

    private class GradientPaletteAdapter(
        private val gradients: List<Pair<String, Pair<Int, Int>>>,
        private val onClick: (Pair<Int, Int>) -> Unit
    ) : RecyclerView.Adapter<GradientPaletteAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diy_color_circle, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(gradients[position], onClick)
        }
        override fun getItemCount(): Int = gradients.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val viewColor: View = view.findViewById(R.id.viewColor)
            private val tvLabel: TextView = view.findViewById(R.id.tvColorLabel)
            fun bind(item: Pair<String, Pair<Int, Int>>, onClick: (Pair<Int, Int>) -> Unit) {
                val gd = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(item.second.first, item.second.second)
                )
                viewColor.background = gd
                tvLabel.text = item.first
                itemView.setOnClickListener { onClick(item.second) }
            }
        }
    }

    private class CdnBgGridAdapter(
        private val category: String,
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<CdnBgGridAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diy_bg_picker, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(category, items[position], onClick)
        }
        override fun getItemCount(): Int = items.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val iv: ImageView = view.findViewById(R.id.ivBgImage)
            fun bind(cat: String, imageName: String, onClick: (String) -> Unit) {
                val url = ResourceConfig.getDiyBackgroundUrl(cat, imageName)
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(R.drawable.bg_default_placeholder)
                    .centerCrop()
                    .into(iv)
                itemView.setOnClickListener { onClick(url) }
            }
        }
    }
}
