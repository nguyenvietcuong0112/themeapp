package com.app.personalization.data

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.content.res.Resources
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.serialization.Serializable

@Serializable
data class ThemeConfig(
    val key: KeyConfig? = null,
    val font: FontConfig,
    val swipe: SwipeConfig? = null,
    val effect: String = "",
    val len: LenConfig? = null,
    val popup: PopupConfig,
    val tintColor: String = "",
    val primaryColor: String = "",
    var decorateKeys: ArrayList<String> = ArrayList(),
    val decorateKeyColor: String = ""
) : java.io.Serializable {

    fun getKeyShapeDrawable(): Drawable? {
        val customStyle = key?.customStyle ?: return null
        val blur = customStyle.blur
        val cornerDp = toDp(customStyle.cornerRadius)
        val bgColor = try {
            Color.parseColor(customStyle.backgroundColor)
        } catch (e: Exception) {
            Color.WHITE
        }

        val adjustedBgColor = adjustAlpha(bgColor, blur)

        val materialShapeDrawable = MaterialShapeDrawable()
        materialShapeDrawable.setCornerSize(cornerDp)
        materialShapeDrawable.fillColor = ColorStateList.valueOf(adjustedBgColor)

        val borderDp = toDp(customStyle.borderWidth)
        if (borderDp > 0.0f && customStyle.borderColor.isNotEmpty()) {
            try {
                val strokeColor = Color.parseColor(customStyle.borderColor)
                materialShapeDrawable.setStroke(borderDp, strokeColor)
            } catch (e: Exception) {
                // Ignore stroke parse error
            }
        }
        return materialShapeDrawable
    }

    private fun toDp(px: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, Resources.getSystem().displayMetrics)
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}
