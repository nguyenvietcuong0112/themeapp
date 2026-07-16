package com.app.personalization.presentation.theme

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.app.personalization.R
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.ResourceConfig
import com.app.personalization.databinding.ActivityKeyboardThemeDetailBinding
import com.app.personalization.domain.model.KeyDef
import kotlinx.coroutines.launch
import java.io.File

class KeyboardThemeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeyboardThemeDetailBinding
    private lateinit var theme: KeyboardTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyboardThemeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        theme = intent.getSerializableExtra("selected_theme") as? KeyboardTheme
            ?: return finish()

        // Setup Toolbar
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
        binding.toolbar.titleTextView.text = theme.name

        // Load preview image using corrected mapping
        val previewUrl = ResourceConfig.getKeyboardPreviewUrl(theme.name, theme.path)
        Glide.with(this)
            .load(previewUrl)
            .placeholder(R.drawable.bg_default_placeholder)
            .error(R.drawable.bg_default_placeholder)
            .into(binding.imageView)

        // Setup button background color and state
        val typedValue = android.util.TypedValue()
        getTheme().resolveAttribute(R.attr.primaryColor, typedValue, true)
        val primaryColor = typedValue.data
        binding.setKeyboardButton.setBackgroundResource(R.drawable.bg_corner_8)
        binding.setKeyboardButton.backgroundTintList = android.content.res.ColorStateList.valueOf(primaryColor)

        binding.llAction.visibility = View.GONE
        binding.setKeyboardButton.visibility = View.VISIBLE
        binding.tvSetKeyboard.text = if (KeyboardThemeDownloader.isDownloaded(this, theme.id)) {
            "Apply Theme"
        } else {
            "Download & Apply"
        }

        // Configure interactive typing test
        val innerParent = binding.recyclerView.parent as ViewGroup
        binding.recyclerView.visibility = View.GONE
        setupTypingTest(theme, innerParent)

        binding.setKeyboardButton.setOnClickListener {
            applyThemeFlow()
        }
    }

    private fun applyThemeFlow() {
        val progress = ProgressDialog(this).apply {
            setMessage("Downloading assets...")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            val success = KeyboardThemeDownloader.downloadTheme(this@KeyboardThemeDetailActivity, theme) { pct ->
                progress.progress = pct
            }

            progress.dismiss()

            if (success) {
                // Apply the theme configuration
                val prefs = getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("selected_theme_id", theme.id).apply()

                // Notify IME service of the update
                sendBroadcast(Intent("com.app.personalization.ACTION_THEME_CHANGED"))

                if (isKeyboardEnabled()) {
                    Toast.makeText(this@KeyboardThemeDetailActivity, "Theme applied successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Lead to system setup steps
                    val intent = Intent(this@KeyboardThemeDetailActivity, SetupKeyboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this@KeyboardThemeDetailActivity, "Failed to download resources from CDN.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val list = imm.enabledInputMethodList ?: return false
        for (info in list) {
            if (info.id.contains(packageName) && info.id.contains("CustomKeyboardIME")) {
                return true
            }
        }
        return false
    }

    private fun setupTypingTest(theme: KeyboardTheme, parent: ViewGroup) {
        val density = resources.displayMetrics.density

        // 1. EditText view for draft typing
        val editText = android.widget.EditText(this).apply {
            hint = "Tap here to try typing..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 16f
            setBackgroundResource(R.drawable.bg_corner_8)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1E1E2E"))
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
            }
            showSoftInputOnFocus = false // Prevent system keyboard from opening
        }
        parent.addView(editText)

        // 2. KeyboardView showing theme keys
        val mockKeyboardView = KeyboardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (8 * density).toInt()
                bottomMargin = (16 * density).toInt()
            }
            visibility = View.GONE
        }

        val rows = loadDefaultKeyboardLayout(this)
        mockKeyboardView.initKeyboard(rows, theme)
        parent.addView(mockKeyboardView)

        editText.setOnFocusChangeListener { _, hasFocus ->
            mockKeyboardView.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
        editText.setOnClickListener {
            mockKeyboardView.visibility = View.VISIBLE
        }

        // Handle typing events
        var isShifted = false
        mockKeyboardView.onKeyClickListener = { key ->
            if (key.isFunctional) {
                when (key.functionalType) {
                    "delete" -> {
                        val txt = editText.text
                        if (txt.isNotEmpty()) {
                            editText.setText(txt.subSequence(0, txt.length - 1))
                            editText.setSelection(editText.text.length)
                        }
                    }
                    "space" -> {
                        editText.append(" ")
                    }
                    "enter" -> {
                        editText.append("\n")
                    }
                    "shift" -> {
                        isShifted = !isShifted
                        mockKeyboardView.isShiftedState = isShifted
                    }
                }
            } else {
                val label = key.label
                val char = if (isShifted) label.uppercase() else label.lowercase()
                editText.append(char)
                if (isShifted) {
                    isShifted = false
                    mockKeyboardView.isShiftedState = false
                }
            }
        }
    }

    private fun loadDefaultKeyboardLayout(context: Context): List<List<KeyDef>> {
        val parsedRows = ArrayList<ArrayList<KeyDef>>()
        try {
            val reader = java.io.BufferedReader(java.io.InputStreamReader(context.assets.open("layouts/qwerty.txt")))
            var line: String?
            var currentRow = ArrayList<KeyDef>()
            while (reader.readLine().also { line = it } != null) {
                val trimmed = line!!.trim()
                if (trimmed.isEmpty()) {
                    if (currentRow.isNotEmpty()) {
                        parsedRows.add(currentRow)
                        currentRow = ArrayList()
                    }
                } else {
                    val tokens = trimmed.split("\\s+".toRegex())
                    val label = tokens[0]
                    currentRow.add(KeyDef(label = label))
                }
            }
            if (currentRow.isNotEmpty()) {
                parsedRows.add(currentRow)
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback rows
            val r1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
            val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
            val r3 = listOf("z", "x", "c", "v", "b", "n", "m")
            parsedRows.add(ArrayList(r1.map { KeyDef(it) }))
            parsedRows.add(ArrayList(r2.map { KeyDef(it) }))
            parsedRows.add(ArrayList(r3.map { KeyDef(it) }))
        }

        if (parsedRows.isNotEmpty()) {
            val row3 = parsedRows[parsedRows.size - 1]
            row3.add(0, KeyDef("▲", isFunctional = true, functionalType = "shift"))
            row3.add(KeyDef("⌫", isFunctional = true, functionalType = "delete"))

            val row4 = ArrayList<KeyDef>()
            row4.add(KeyDef("?123", isFunctional = true, functionalType = "symbols_switch"))
            row4.add(KeyDef("😀", isFunctional = true, functionalType = "emoji_switch"))
            row4.add(KeyDef(" ", isFunctional = true, functionalType = "space"))
            row4.add(KeyDef(".", popup = listOf(".")))
            row4.add(KeyDef("⏎", isFunctional = true, functionalType = "enter"))
            parsedRows.add(row4)
        }

        return parsedRows
    }
}
