package com.app.personalization.presentation.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.app.personalization.R
import com.app.personalization.di.ServiceLocator
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.CustomKeyStyle
import com.app.personalization.data.KeyConfig
import com.app.personalization.data.ThemeConfig
import com.app.personalization.data.FontConfig
import com.app.personalization.data.PopupConfig
import com.app.personalization.domain.model.KeyDef
import com.app.personalization.presentation.theme.KeyboardView

class ThemeBuilderActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var previewKeyboardView: KeyboardView? = null

    private lateinit var etThemeName: EditText
    private lateinit var etBgColor: EditText
    private lateinit var etKeyBgColor: EditText
    private lateinit var etKeyTextColor: EditText
    private lateinit var etKeyShadowColor: EditText
    private lateinit var tvRadiusLabel: TextView
    private lateinit var sbBorderRadius: SeekBar
    private lateinit var spinnerFont: Spinner
    private lateinit var btnSaveTheme: Button

    private val previewRows = listOf(
        listOf(KeyDef("q"), KeyDef("w"), KeyDef("e"), KeyDef("r"), KeyDef("t"), KeyDef("y"), KeyDef("u"), KeyDef("i"), KeyDef("o"), KeyDef("p")),
        listOf(KeyDef("a"), KeyDef("s"), KeyDef("d"), KeyDef("f"), KeyDef("g"), KeyDef("h"), KeyDef("j"), KeyDef("k"), KeyDef("l")),
        listOf(KeyDef("▲", isFunctional = true, functionalType = "shift"), KeyDef("z"), KeyDef("x"), KeyDef("c"), KeyDef("v"), KeyDef("b"), KeyDef("n"), KeyDef("m"), KeyDef("⌫", isFunctional = true, functionalType = "delete")),
        listOf(KeyDef("?123", isFunctional = true, functionalType = "symbols_switch"), KeyDef("😀", isFunctional = true, functionalType = "emoji_switch"), KeyDef(" ", isFunctional = true, functionalType = "space"), KeyDef("⏎", isFunctional = true, functionalType = "enter"))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_theme)

        prefs = getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

        previewKeyboardView = findViewById(R.id.themeView)
        etThemeName = findViewById(R.id.etThemeName)
        etBgColor = findViewById(R.id.etBgColor)
        etKeyBgColor = findViewById(R.id.etKeyBgColor)
        etKeyTextColor = findViewById(R.id.etKeyTextColor)
        etKeyShadowColor = findViewById(R.id.etKeyShadowColor)
        tvRadiusLabel = findViewById(R.id.tvRadiusLabel)
        sbBorderRadius = findViewById(R.id.sbBorderRadius)
        spinnerFont = findViewById(R.id.spinnerFont)
        btnSaveTheme = findViewById(R.id.btnSaveTheme)

        findViewById<View>(R.id.ivClose)?.setOnClickListener { finish() }

        setupPreviewKeyboard()
        setupListeners()
    }

    private fun setupPreviewKeyboard() {
        updateLivePreview()
    }

    private fun setupListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLivePreview()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etBgColor.addTextChangedListener(watcher)
        etKeyBgColor.addTextChangedListener(watcher)
        etKeyTextColor.addTextChangedListener(watcher)
        etKeyShadowColor.addTextChangedListener(watcher)

        sbBorderRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvRadiusLabel.text = "Key Corner Rounding: ${progress}dp"
                updateLivePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        try {
            val fonts = mutableListOf("normal")
            val assetFonts = assets.list("fonts")?.filter { it.endsWith(".ttf") || it.endsWith(".otf") } ?: emptyList()
            fonts.addAll(assetFonts)

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fonts)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFont.adapter = adapter

            spinnerFont.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    updateLivePreview()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        btnSaveTheme.setOnClickListener {
            saveThemeAndApply()
        }
    }

    private fun buildThemeConfig(): ThemeConfig {
        val bgStr = etBgColor.text.toString().trim().ifEmpty { "#12121A" }
        val keyBgStr = etKeyBgColor.text.toString().trim().ifEmpty { "#1E1E2E" }
        val keyTextStr = etKeyTextColor.text.toString().trim().ifEmpty { "#00E5FF" }
        val keyShadowStr = etKeyShadowColor.text.toString().trim().ifEmpty { "#3300E5FF" }

        val fontName = spinnerFont.selectedItem?.toString() ?: "normal"
        val borderRadius = sbBorderRadius.progress.toFloat()

        val style = CustomKeyStyle(
            backgroundColor = keyBgStr,
            cornerRadius = borderRadius,
            borderWidth = 1f,
            borderColor = keyShadowStr,
            blur = 1.0f
        )
        return ThemeConfig(
            key = KeyConfig(customStyle = style),
            font = FontConfig(fontColorString = keyTextStr, fontFamily = fontName),
            popup = PopupConfig(textColorString = keyTextStr),
            tintColor = bgStr
        )
    }

    private fun updateLivePreview() {
        val theme = KeyboardTheme(
            name = etThemeName.text.toString().trim().ifEmpty { "DIY Preview" },
            rawType = "diy",
            themeConfig = buildThemeConfig()
        )
        previewKeyboardView?.initKeyboard(previewRows, theme)
    }

    private fun saveThemeAndApply() {
        val themeName = etThemeName.text.toString().trim()
        if (themeName.isEmpty()) {
            Toast.makeText(this, "Please enter a theme name", Toast.LENGTH_SHORT).show()
            return
        }

        val config = buildThemeConfig()
        val newTheme = KeyboardTheme(
            id = java.util.UUID.randomUUID().toString(),
            name = themeName,
            rawType = "diy",
            themeConfig = config
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ServiceLocator.getThemeDao(this@ThemeBuilderActivity).insertTheme(newTheme)
                withContext(Dispatchers.Main) {
                    prefs.edit().apply {
                        putString("selected_theme_id", newTheme.id)
                        putString("current_theme_path", newTheme.path)
                        putString("current_theme_type", newTheme.rawType)
                        apply()
                    }

                    // Notify keyboard IME service to reload theme instantly
                    val intent = Intent("com.app.personalization.ACTION_THEME_CHANGED").apply {
                        setPackage(packageName)
                    }
                    sendBroadcast(intent)

                    Toast.makeText(this@ThemeBuilderActivity, "Theme '$themeName' saved & applied!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ThemeBuilderActivity, "Failed to save theme: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
