package com.app.personalization.presentation.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.ColorFilter
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class ImageFormatTransform(
    private val rotation: Float = 0f,
    private val isFlipped: Boolean = false,
    private val colorFilter: ColorFilter? = null
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val matrix = android.graphics.Matrix()
        if (rotation != 0f) {
            matrix.postRotate(rotation)
        }
        if (isFlipped) {
            matrix.postScale(-1f, 1f, toTransform.width / 2f, toTransform.height / 2f)
        }

        val transformed = Bitmap.createBitmap(
            toTransform, 0, 0, toTransform.width, toTransform.height, matrix, true
        )

        if (colorFilter != null) {
            val result = pool.get(transformed.width, transformed.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.colorFilter = colorFilter
            }
            canvas.drawBitmap(transformed, 0f, 0f, paint)
            return result
        }

        return transformed
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("ImageFormatTransform_rot_${rotation}_flip_${isFlipped}_filter_${colorFilter?.hashCode()}".toByteArray())
    }
}
