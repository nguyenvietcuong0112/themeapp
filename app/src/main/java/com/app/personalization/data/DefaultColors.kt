package com.app.personalization.data

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.app.personalization.data.database.entity.KeyboardTheme
import com.bumptech.glide.Glide
import java.io.File

enum class ColorType {
    KEY_TEXT,
    KEY_BG,
    TINT,
    POPUP_TEXT,
    POPUP_BG
}

object DefaultColors {
    private val keyBackgroundCache = HashMap<Int, Drawable?>()
    private var activeTheme: KeyboardTheme? = null

    fun loadBackgroundKey(context: Context, theme: KeyboardTheme?) {
        keyBackgroundCache.clear()
        activeTheme = theme
        if (theme == null) return

        val keyCodes = listOf(10, 66, 12289, -5, -1, -2, 32, 0)
        for (code in keyCodes) {
            val drawable = theme.getKeyBackground(context, code)
            keyBackgroundCache[code] = drawable
        }
    }

    fun getKeyBackground(code: Int): Drawable? {
        return keyBackgroundCache[code] ?: keyBackgroundCache[0]
    }

    fun get(colorType: ColorType, context: Context): Int {
        val theme = activeTheme ?: return getDefaultColor(colorType)
        return when (colorType) {
            ColorType.KEY_TEXT -> theme.keyTextColor(context)
            ColorType.KEY_BG -> theme.getConfig(context)?.key?.customStyle?.backgroundColor?.let {
                try { Color.parseColor(it) } catch (e: Exception) { Color.WHITE }
            } ?: Color.WHITE
            ColorType.TINT -> theme.tintColor(context)
            ColorType.POPUP_TEXT -> theme.previewTextColor(context)
            ColorType.POPUP_BG -> Color.WHITE
        }
    }

    private fun getDefaultColor(colorType: ColorType): Int {
        return when (colorType) {
            ColorType.KEY_TEXT -> Color.BLACK
            ColorType.KEY_BG -> Color.WHITE
            ColorType.TINT -> Color.WHITE
            ColorType.POPUP_TEXT -> Color.BLACK
            ColorType.POPUP_BG -> Color.WHITE
        }
    }

    fun setBackground(view: View, theme: KeyboardTheme?) {
        if (theme == null) {
            view.setBackgroundColor(Color.parseColor("#12121A"))
            return
        }

        val localBgFile = if (theme.path.isNotEmpty()) {
            java.io.File(view.context.filesDir, "keyboard_themes/${theme.path}/keyboard_background.png")
        } else {
            null
        }

        if (localBgFile != null && localBgFile.exists()) {
            view.background = ColorDrawable(Color.TRANSPARENT)
            if (view is ImageView) {
                Glide.with(view.context)
                    .load(localBgFile)
                    .into(view)
            } else {
                Glide.with(view.context)
                    .load(localBgFile)
                    .into(object : com.bumptech.glide.request.target.CustomViewTarget<View, Drawable>(view) {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            view.setBackgroundColor(Color.parseColor("#12121A"))
                        }
                        override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
                            view.background = resource
                        }
                        override fun onResourceCleared(placeholder: Drawable?) {
                            view.background = null
                        }
                    })
            }
        } else {
            val bgUrl = theme.getBackground()
            if (bgUrl != null) {
                view.background = ColorDrawable(Color.TRANSPARENT)
                if (view is ImageView) {
                    Glide.with(view.context)
                        .load(bgUrl)
                        .into(view)
                } else {
                    Glide.with(view.context)
                        .load(bgUrl)
                        .into(object : com.bumptech.glide.request.target.CustomViewTarget<View, Drawable>(view) {
                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                view.setBackgroundColor(Color.parseColor("#12121A"))
                            }
                            override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
                                view.background = resource
                            }
                            override fun onResourceCleared(placeholder: Drawable?) {
                                view.background = null
                            }
                        })
                }
            } else {
                // DIY custom background
                val customBgFile = File(view.context.filesDir, "custom_background_image")
                if (customBgFile.exists()) {
                    try {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(customBgFile.absolutePath)
                        if (bitmap != null) {
                            view.background = BitmapDrawable(view.resources, bitmap)
                        } else {
                            view.setBackgroundColor(Color.parseColor("#12121A"))
                        }
                    } catch (e: Exception) {
                        view.setBackgroundColor(Color.parseColor("#12121A"))
                    }
                } else {
                    view.setBackgroundColor(Color.parseColor("#12121A"))
                }
            }
        }
    }
}
