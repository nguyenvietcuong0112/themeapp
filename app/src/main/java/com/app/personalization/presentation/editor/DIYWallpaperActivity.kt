package com.app.personalization.presentation.editor

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
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.personalization.R

class DIYWallpaperActivity : AppCompatActivity() {

    private lateinit var wallpaperCanvas: DIYWallpaperCanvasView
    private lateinit var btnApplyWallpaper: View
    private lateinit var btnAddText: Button
    private lateinit var btnAddSticker: Button
    private lateinit var btnChangeBase: Button
    private lateinit var layoutTextControls: LinearLayout
    private lateinit var spinnerFont: Spinner

    companion object {
        private const val REQUEST_PICK_BASE_IMAGE = 5001
        private const val REQUEST_PICK_STICKER_IMAGE = 5002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallpaper)

        wallpaperCanvas = findViewById(R.id.wallpaperCanvas)
        btnApplyWallpaper = findViewById(R.id.tvSave)
        btnAddText = findViewById(R.id.btnAddText)
        btnAddSticker = findViewById(R.id.btnAddSticker)
        btnChangeBase = findViewById(R.id.btnChangeBase)
        layoutTextControls = findViewById(R.id.layoutTextControls)
        spinnerFont = findViewById(R.id.spinnerFont)

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        setupFontSpinner()
        setupButtons()
    }

    private fun setupFontSpinner() {
        try {
            val fonts = mutableListOf("normal")
            val assetFonts = assets.list("fonts")?.filter { it.endsWith(".ttf") || it.endsWith(".otf") } ?: emptyList()
            fonts.addAll(assetFonts)

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
                        activeTextLayer.typeface = if (selectedFont != "normal") {
                            try {
                                android.graphics.Typeface.createFromAsset(assets, "fonts/$selectedFont")
                            } catch (e: Exception) {
                                android.graphics.Typeface.DEFAULT
                            }
                        } else {
                            android.graphics.Typeface.DEFAULT
                        }
                        wallpaperCanvas.invalidate()
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
        val stickerList = mutableListOf<String>()
        try {
            val root = "themes/stickers"
            val files = assets.list(root) ?: emptyArray()
            for (f in files) {
                if (f.endsWith(".png") || f.endsWith(".jpg") || f.endsWith(".webp")) {
                    stickerList.add("$root/$f")
                } else {
                    val subFiles = assets.list("$root/$f") ?: emptyArray()
                    for (sf in subFiles) {
                        if (sf.endsWith(".png") || sf.endsWith(".jpg") || sf.endsWith(".webp")) {
                            stickerList.add("$root/$f/$sf")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (stickerList.isEmpty()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_PICK_STICKER_IMAGE)
            return
        }

        val displayOptions = stickerList.map { it.substringAfterLast("/") }.toMutableList().apply {
            add("Choose from Gallery...")
        }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Sticker")
        builder.setItems(displayOptions) { dialog, which ->
            if (which == displayOptions.size - 1) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
                startActivityForResult(intent, REQUEST_PICK_STICKER_IMAGE)
            } else {
                val assetPath = stickerList[which]
                try {
                    val inputStream = assets.open(assetPath)
                    val bmp = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    if (bmp != null) {
                        wallpaperCanvas.addStickerLayer(bmp)
                        Toast.makeText(this, "Sticker added!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load sticker asset", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showBaseLayerDialog() {
        val choices = arrayOf("Solid Color", "Linear Gradient", "Gallery Image")
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
            }
            dialog.dismiss()
        }
        builder.show()
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
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                try {
                    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                    val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                    parcelFileDescriptor?.close()

                    if (bitmap != null) {
                        if (requestCode == REQUEST_PICK_BASE_IMAGE) {
                            wallpaperCanvas.setBackgroundImage(bitmap)
                        } else if (requestCode == REQUEST_PICK_STICKER_IMAGE) {
                            wallpaperCanvas.addStickerLayer(bitmap)
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

            val wallpaperManager = WallpaperManager.getInstance(this)
            wallpaperManager.setBitmap(bitmap)

            Toast.makeText(this, "DIY Wallpaper applied successfully!", Toast.LENGTH_LONG).show()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to apply wallpaper: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
