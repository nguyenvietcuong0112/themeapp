package com.app.personalization.presentation.editor

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.personalization.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SetWallpaperBottomSheet(private val bitmap: Bitmap) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_set_wallpaper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wallpaperManager = WallpaperManager.getInstance(requireContext())

        view.findViewById<View>(R.id.btnHomeScreen)?.setOnClickListener {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
                Toast.makeText(context, "Applied to Home Screen successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to apply wallpaper", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<View>(R.id.btnLockScreen)?.setOnClickListener {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                } else {
                    Toast.makeText(context, "Lock screen setting not supported on this Android version", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(context, "Applied to Lock Screen successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to apply wallpaper", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<View>(R.id.btnBoth)?.setOnClickListener {
            try {
                wallpaperManager.setBitmap(bitmap)
                Toast.makeText(context, "Applied to both screens successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to apply wallpaper", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
