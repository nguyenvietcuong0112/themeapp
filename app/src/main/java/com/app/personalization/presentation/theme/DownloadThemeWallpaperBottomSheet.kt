package com.app.personalization.presentation.theme

import android.app.WallpaperManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.personalization.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DownloadThemeWallpaperBottomSheet : BottomSheetDialogFragment() {

    interface Callback {
        fun onApply(flag: Int)
    }

    private var callback: Callback? = null

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_set_wallpaper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnHomeScreen)?.setOnClickListener {
            callback?.onApply(WallpaperManager.FLAG_SYSTEM)
            dismiss()
        }

        view.findViewById<View>(R.id.btnLockScreen)?.setOnClickListener {
            callback?.onApply(WallpaperManager.FLAG_LOCK)
            dismiss()
        }

        view.findViewById<View>(R.id.btnBoth)?.setOnClickListener {
            callback?.onApply(0) // 0 means both
            dismiss()
        }
    }
}