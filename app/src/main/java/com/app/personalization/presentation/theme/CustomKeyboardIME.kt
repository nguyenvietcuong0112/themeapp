package com.app.personalization.presentation.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.app.personalization.R
import com.app.personalization.di.ServiceLocator
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.CustomKeyStyle
import com.app.personalization.data.KeyConfig
import com.app.personalization.data.ThemeConfig
import com.app.personalization.data.FontConfig
import com.app.personalization.data.PopupConfig
import com.app.personalization.domain.model.KeyDef
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class CustomKeyboardIME : InputMethodService() {

    private lateinit var prefs: SharedPreferences
    private lateinit var audioManager: AudioManager
    private var vibrator: Vibrator? = null

    // Layout Containers
    private lateinit var rootKeyboardLayout: LinearLayout
    private lateinit var suggestionBarView: SuggestionBarView
    private lateinit var keyboardContainer: FrameLayout
    private var keyboardView: KeyboardView? = null
    private var emojiPanel: EmojiPanelContainer? = null

    // Keyboard configurations & state
    var activeTheme: KeyboardTheme? = null
    var isShifted = false
    var isSymbolsMode = false
    var isEmojiMode = false
    var currentLayoutName = "qwerty.txt"

    var parsedRows = ArrayList<ArrayList<KeyDef>>()
    var popupMap = HashMap<String, List<String>>()

    private val composingWord = StringBuilder()

    private val themeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.app.personalization.ACTION_THEME_CHANGED") {
                KeyboardSwitcher.getInstance().updateKeyboardTheme(this@CustomKeyboardIME, this@CustomKeyboardIME)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        currentLayoutName = prefs.getString("active_layout", "qwerty.txt") ?: "qwerty.txt"

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        loadLanguagePopups()

        val filter = IntentFilter("com.app.personalization.ACTION_THEME_CHANGED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(themeReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(themeReceiver, filter)
        }
    }

    override fun onCreateInputView(): View {
        loadActiveTheme()
        com.app.personalization.data.DefaultColors.loadBackgroundKey(this, activeTheme)
        loadKeyboardLayout()

        rootKeyboardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        suggestionBarView = SuggestionBarView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(44)
            )
            onSuggestionClickListener = { word ->
                commitSuggestion(word)
            }
        }
        val locale = prefs.getString("active_locale", "en") ?: "en"
        suggestionBarView.loadDictionaryAsync(locale)
        rootKeyboardLayout.addView(suggestionBarView)

        keyboardContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        rootKeyboardLayout.addView(keyboardContainer)

        val kView = KeyboardView(this)
        keyboardView = kView
        kView.initKeyboard(parsedRows, activeTheme)
        keyboardContainer.addView(kView)

        return rootKeyboardLayout
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        
        composingWord.clear()
        suggestionBarView.updateSuggestions("")

        val layoutFromPrefs = prefs.getString("active_layout", "qwerty.txt") ?: "qwerty.txt"
        if (layoutFromPrefs != currentLayoutName) {
            currentLayoutName = layoutFromPrefs
            loadKeyboardLayout()
        }
        
        val locale = prefs.getString("active_locale", "en") ?: "en"
        suggestionBarView.loadDictionaryAsync(locale)

        loadActiveTheme()
        com.app.personalization.data.DefaultColors.loadBackgroundKey(this, activeTheme)

        isEmojiMode = false
        showNormalKeyboard()
    }

    private fun loadActiveTheme() {
        val jsonTheme = prefs.getString("KEYBOARD_THEME", null)
        if (jsonTheme != null) {
            try {
                val decoded = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<KeyboardTheme>(jsonTheme)
                activeTheme = decoded
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val selectedThemeId = try {
            prefs.getString("selected_theme_id", null)
        } catch (e: Exception) {
            prefs.edit().remove("selected_theme_id").apply()
            null
        }
        activeTheme = if (selectedThemeId != null) {
            if (selectedThemeId.startsWith("default_")) {
                val path = selectedThemeId.substringAfter("default_")
                val cat = path.substringBefore("/")
                val name = path.substringAfter("/").replace("-", " ").replaceFirstChar { it.uppercase() }
                KeyboardTheme(
                    id = selectedThemeId,
                    categoryId = cat.lowercase(),
                    name = name,
                    path = path,
                    rawType = "default"
                )
            } else {
                ServiceLocator.getThemeDao(this).getThemeById(selectedThemeId)
            }
        } else {
            null
        } ?: getDefaultTheme()
    }

    fun reloadActiveTheme() {
        loadActiveTheme()
        com.app.personalization.data.DefaultColors.loadBackgroundKey(this, activeTheme)
        keyboardView?.let { kView ->
            kView.initKeyboard(parsedRows, activeTheme)
        }
    }

    private fun getDefaultTheme(): KeyboardTheme {
        val style = CustomKeyStyle(
            backgroundColor = "#1E1E2E",
            cornerRadius = 12f,
            borderWidth = 1f,
            borderColor = "#3300E5FF",
            blur = 1.0f
        )
        val config = ThemeConfig(
            key = KeyConfig(customStyle = style),
            font = FontConfig(fontColorString = "#00E5FF", fontFamily = "normal"),
            popup = PopupConfig(textColorString = "#00E5FF"),
            tintColor = "#12121A"
        )
        return KeyboardTheme(
            id = "default_theme",
            name = "Default Cyberpunk",
            rawType = "diy",
            themeConfig = config
        )
    }

    private fun loadLanguagePopups() {
        popupMap.clear()
        try {
            val locale = prefs.getString("active_locale", "en") ?: "en"
            val filename = "language_key_texts/$locale.txt"
            val assetsList = assets.list("language_key_texts") ?: emptyArray()
            val targetFile = if (assetsList.contains("$locale.txt")) filename else "language_key_texts/en.txt"

            val reader = BufferedReader(InputStreamReader(assets.open(targetFile)))
            var line: String?
            var inSection = false
            while (reader.readLine().also { line = it } != null) {
                val trimmed = line!!.trim()
                if (trimmed == "[popup_keys]") {
                    inSection = true
                    continue
                }
                if (inSection && trimmed.isNotEmpty()) {
                    val tokens = trimmed.split("\\s+".toRegex())
                    if (tokens.isNotEmpty()) {
                        val mainKey = tokens[0]
                        val list = tokens.subList(1, tokens.size)
                        popupMap[mainKey] = list
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadKeyboardLayout() {
        parsedRows.clear()
        val layoutPath = if (isSymbolsMode) "layouts/symbols.txt" else "layouts/$currentLayoutName"
        try {
            if (layoutPath.endsWith(".json")) {
                parseJsonLayout(layoutPath)
            } else {
                parseTxtLayout(layoutPath)
            }
        } catch (e: Exception) {
            fallbackLayout()
        }
        appendFunctionalKeys()
    }

    private fun parseTxtLayout(path: String) {
        val reader = BufferedReader(InputStreamReader(assets.open(path)))
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
                val popups = if (tokens.size > 1) tokens.subList(1, tokens.size) else (popupMap[label] ?: emptyList())
                currentRow.add(KeyDef(label = label, popup = popups))
            }
        }
        if (currentRow.isNotEmpty()) {
            parsedRows.add(currentRow)
        }
        reader.close()
    }

    private fun parseJsonLayout(path: String) {
        val json = assets.open(path).bufferedReader().use { it.readText() }
        val rootArray = JSONArray(json)
        for (i in 0 until rootArray.length()) {
            val rowArray = rootArray.getJSONArray(i)
            val currentRow = ArrayList<KeyDef>()
            for (j in 0 until rowArray.length()) {
                val keyObj = rowArray.get(j)
                if (keyObj is JSONObject) {
                    val label = if (keyObj.has("label")) {
                        keyObj.getString("label")
                    } else if (keyObj.has("default")) {
                        keyObj.getJSONObject("default").optString("label", "?")
                    } else {
                        "?"
                    }
                    val popups = ArrayList<String>()
                    if (keyObj.has("popup")) {
                        val mainObj = keyObj.getJSONObject("popup").optJSONObject("main")
                        mainObj?.optString("label")?.let { popups.add(it) }
                    }
                    if (popups.isEmpty()) {
                        popups.addAll(popupMap[label] ?: emptyList())
                    }
                    currentRow.add(KeyDef(label = label, popup = popups))
                } else if (keyObj is String) {
                    currentRow.add(KeyDef(label = keyObj, popup = popupMap[keyObj] ?: emptyList()))
                }
            }
            if (currentRow.isNotEmpty()) {
                parsedRows.add(currentRow)
            }
        }
    }

    private fun fallbackLayout() {
        val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        parsedRows.add(ArrayList(row1.map { KeyDef(it, popupMap[it] ?: emptyList()) }))
        parsedRows.add(ArrayList(row2.map { KeyDef(it, popupMap[it] ?: emptyList()) }))
        parsedRows.add(ArrayList(row3.map { KeyDef(it, popupMap[it] ?: emptyList()) }))
    }

    private fun appendFunctionalKeys() {
        if (parsedRows.isEmpty()) return
        if (parsedRows.size >= 3) {
            val row3 = parsedRows[parsedRows.size - 1]
            row3.add(0, KeyDef("▲", isFunctional = true, functionalType = "shift"))
            row3.add(KeyDef("⌫", isFunctional = true, functionalType = "delete"))
        }

        val row4 = ArrayList<KeyDef>()
        row4.add(KeyDef(if (isSymbolsMode) "ABC" else "?123", isFunctional = true, functionalType = "symbols_switch"))
        row4.add(KeyDef("😀", isFunctional = true, functionalType = "emoji_switch"))
        row4.add(KeyDef(" ", isFunctional = true, functionalType = "space"))
        row4.add(KeyDef(".", popup = listOf(".")))
        row4.add(KeyDef("⏎", isFunctional = true, functionalType = "enter"))
        parsedRows.add(row4)
    }

    fun playClick(key: KeyDef) {
        val type = when (key.functionalType) {
            "space" -> AudioManager.FX_KEYPRESS_SPACEBAR
            "delete" -> AudioManager.FX_KEYPRESS_DELETE
            "enter" -> AudioManager.FX_KEYPRESS_RETURN
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }
        audioManager.playSoundEffect(type)
    }

    fun vibrate() {
        vibrator?.let { vib ->
            if (vib.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(30)
                }
            }
        }
    }

    fun handleKeyPress(key: KeyDef) {
        val ic = currentInputConnection ?: return

        if (key.isFunctional) {
            when (key.functionalType) {
                "delete" -> {
                    if (composingWord.isNotEmpty()) {
                        composingWord.deleteCharAt(composingWord.length - 1)
                        ic.setComposingText(composingWord, 1)
                        suggestionBarView.updateSuggestions(composingWord.toString())
                    } else {
                        ic.deleteSurroundingText(1, 0)
                    }
                }
                "shift" -> {
                    isShifted = !isShifted
                    keyboardView?.toggleShift(isShifted)
                }
                "space" -> {
                    commitComposing()
                    ic.commitText(" ", 1)
                }
                "enter" -> {
                    commitComposing()
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
                "symbols_switch" -> {
                    commitComposing()
                    isSymbolsMode = !isSymbolsMode
                    loadKeyboardLayout()
                    keyboardView?.initKeyboard(parsedRows, activeTheme)
                }
                "emoji_switch" -> {
                    commitComposing()
                    isEmojiMode = !isEmojiMode
                    if (isEmojiMode) {
                        showEmojiPanel()
                    } else {
                        showNormalKeyboard()
                    }
                }
            }
        } else {
            val char = if (isShifted) key.label.uppercase() else key.label.lowercase()
            if (char.length == 1 && char[0].isLetter()) {
                composingWord.append(char)
                ic.setComposingText(composingWord, 1)
                suggestionBarView.updateSuggestions(composingWord.toString())
            } else {
                commitComposing()
                ic.commitText(char, 1)
            }

            if (isShifted) {
                isShifted = false
                keyboardView?.toggleShift(false)
            }
        }
    }

    private fun commitComposing() {
        if (composingWord.isNotEmpty()) {
            currentInputConnection?.commitText(composingWord, 1)
            composingWord.clear()
            suggestionBarView.updateSuggestions("")
        }
    }

    private fun commitSuggestion(word: String) {
        currentInputConnection?.commitText(word + " ", 1)
        composingWord.clear()
        suggestionBarView.updateSuggestions("")
    }

    private fun showEmojiPanel() {
        if (emojiPanel == null) {
            emojiPanel = EmojiPanelContainer(this).apply {
                onItemClickListener = { emoji ->
                    currentInputConnection?.commitText(emoji, 1)
                }
                onStickerClickListener = { assetSticker ->
                    currentInputConnection?.commitText("[Sticker: $assetSticker]", 1)
                }
            }
        }

        keyboardContainer.removeAllViews()
        keyboardContainer.addView(emojiPanel)
    }

    private fun showNormalKeyboard() {
        keyboardContainer.removeAllViews()
        keyboardView?.initKeyboard(parsedRows, activeTheme)
        keyboardContainer.addView(keyboardView)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(themeReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}
