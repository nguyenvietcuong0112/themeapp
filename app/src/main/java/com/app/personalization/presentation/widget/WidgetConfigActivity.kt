package com.app.personalization.presentation.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.personalization.R
import com.app.personalization.di.ServiceLocator
import com.app.personalization.data.database.entity.WidgetConfig

class WidgetConfigActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedImageUri: String? = null

    private lateinit var tvWidgetId: TextView
    private lateinit var spinnerBgType: Spinner
    
    private lateinit var layoutSolidColor: LinearLayout
    private lateinit var etBgColor: EditText
    
    private lateinit var layoutGradientColor: LinearLayout
    private lateinit var etGradientStartColor: EditText
    private lateinit var etGradientEndColor: EditText
    
    private lateinit var layoutImageBg: LinearLayout
    private lateinit var btnSelectBgImage: Button
    private lateinit var tvImageUri: TextView
    
    private lateinit var etTextColor: EditText
    private lateinit var spinnerFont: Spinner
    private lateinit var btnSaveConfig: View

    companion object {
        private const val REQUEST_PICK_IMAGE = 4001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(this, "Invalid AppWidget ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContentView(R.layout.activity_create_widget)

        tvWidgetId = findViewById(R.id.tvWidgetId)
        spinnerBgType = findViewById(R.id.spinnerBgType)
        layoutSolidColor = findViewById(R.id.layoutSolidColor)
        etBgColor = findViewById(R.id.etBgColor)
        
        layoutGradientColor = findViewById(R.id.layoutGradientColor)
        etGradientStartColor = findViewById(R.id.etGradientStartColor)
        etGradientEndColor = findViewById(R.id.etGradientEndColor)
        
        layoutImageBg = findViewById(R.id.layoutImageBg)
        btnSelectBgImage = findViewById(R.id.btnSelectBgImage)
        tvImageUri = findViewById(R.id.tvImageUri)
        
        etTextColor = findViewById(R.id.etTextColor)
        spinnerFont = findViewById(R.id.spinnerFont)
        btnSaveConfig = findViewById(R.id.clInstall)

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        tvWidgetId.text = "Widget ID: $widgetId"

        setupBgTypeSpinner()
        setupFontSpinner()
        setupImagePicker()
        loadExistingConfig()

        btnSaveConfig.setOnClickListener {
            saveWidgetConfig()
        }
    }

    private fun setupBgTypeSpinner() {
        val bgTypes = listOf("Solid Color", "Linear Gradient", "Custom Image")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bgTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBgType.adapter = adapter

        spinnerBgType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        layoutSolidColor.visibility = View.VISIBLE
                        layoutGradientColor.visibility = View.GONE
                        layoutImageBg.visibility = View.GONE
                    }
                    1 -> {
                        layoutSolidColor.visibility = View.GONE
                        layoutGradientColor.visibility = View.VISIBLE
                        layoutImageBg.visibility = View.GONE
                    }
                    2 -> {
                        layoutSolidColor.visibility = View.GONE
                        layoutGradientColor.visibility = View.GONE
                        layoutImageBg.visibility = View.VISIBLE
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFontSpinner() {
        try {
            val fonts = mutableListOf("normal")
            val assetFonts = assets.list("fonts")?.filter { it.endsWith(".ttf") || it.endsWith(".otf") } ?: emptyList()
            fonts.addAll(assetFonts)

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fonts)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFont.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupImagePicker() {
        btnSelectBgImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri.toString()
                tvImageUri.text = selectedImageUri
            }
        }
    }

    private fun loadExistingConfig() {
        val config = ServiceLocator.getWidgetConfigDao(this).getConfigForWidget(widgetId) ?: return

        etTextColor.setText(String.format("#%06X", 0xFFFFFF and config.textColor))

        when (config.bgType) {
            "COLOR" -> {
                spinnerBgType.setSelection(0)
                etBgColor.setText(String.format("#%06X", 0xFFFFFF and config.solidColor))
            }
            "GRADIENT" -> {
                spinnerBgType.setSelection(1)
                etGradientStartColor.setText(String.format("#%06X", 0xFFFFFF and config.gradientStartColor))
                etGradientEndColor.setText(String.format("#%06X", 0xFFFFFF and config.gradientEndColor))
            }
            "IMAGE" -> {
                spinnerBgType.setSelection(2)
                selectedImageUri = config.imageUri
                tvImageUri.text = selectedImageUri ?: "No image selected"
            }
        }

        try {
            val fonts = mutableListOf("normal")
            val assetFonts = assets.list("fonts")?.filter { it.endsWith(".ttf") || it.endsWith(".otf") } ?: emptyList()
            fonts.addAll(assetFonts)

            val index = fonts.indexOf(config.fontStyle)
            if (index != -1) {
                spinnerFont.setSelection(index)
            }
        } catch (e: Exception) {}
    }

    private fun saveWidgetConfig() {
        val textColorStr = etTextColor.text.toString().trim()
        val textColor = safeParseColor(textColorStr, Color.WHITE)
        val fontStyle = spinnerFont.selectedItem?.toString() ?: "normal"

        val bgType = when (spinnerBgType.selectedItemPosition) {
            0 -> "COLOR"
            1 -> "GRADIENT"
            else -> "IMAGE"
        }

        val solidColor = safeParseColor(etBgColor.text.toString().trim(), 0xFF1A1A24.toInt())
        val gradStart = safeParseColor(etGradientStartColor.text.toString().trim(), 0xFF00E5FF.toInt())
        val gradEnd = safeParseColor(etGradientEndColor.text.toString().trim(), 0xFF7C4DFF.toInt())

        val config = WidgetConfig(
            widgetId = widgetId,
            bgType = bgType,
            solidColor = solidColor,
            imageUri = selectedImageUri,
            textColor = textColor,
            fontStyle = fontStyle,
            gradientStartColor = gradStart,
            gradientEndColor = gradEnd
        )

        ServiceLocator.getWidgetConfigDao(this).saveConfig(config)

        val appWidgetManager = AppWidgetManager.getInstance(this)
        Widget2x2Provider().updateWidget(this, appWidgetManager, widgetId)
        Widget4x2Provider().updateWidget(this, appWidgetManager, widgetId)
        Widget4x4Provider().updateWidget(this, appWidgetManager, widgetId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        Toast.makeText(this, "Widget customizations applied successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun safeParseColor(colorStr: String, defaultColor: Int): Int {
        return try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            defaultColor
        }
    }
}
