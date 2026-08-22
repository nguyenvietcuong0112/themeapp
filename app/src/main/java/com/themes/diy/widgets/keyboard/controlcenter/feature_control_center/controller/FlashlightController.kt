package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.controller

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build

class FlashlightController(context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null
    var isFlashlightOn: Boolean = false
        private set

    private var onStateChangedListener: ((Boolean) -> Unit)? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(camId: String, enabled: Boolean) {
            super.onTorchModeChanged(camId, enabled)
            if (camId == cameraId) {
                isFlashlightOn = enabled
                onStateChangedListener?.invoke(enabled)
            }
        }

        override fun onTorchModeUnavailable(camId: String) {
            super.onTorchModeUnavailable(camId)
            if (camId == cameraId) {
                isFlashlightOn = false
                onStateChangedListener?.invoke(false)
            }
        }
    }

    init {
        try {
            cameraManager?.let { manager ->
                for (id in manager.cameraIdList) {
                    val characteristics = manager.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id
                        break
                    }
                }
                manager.registerTorchCallback(torchCallback, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOnStateChangedListener(listener: (Boolean) -> Unit) {
        this.onStateChangedListener = listener
    }

    fun toggle(): Boolean {
        val target = !isFlashlightOn
        return setTorchMode(target)
    }

    fun setTorchMode(enabled: Boolean): Boolean {
        val id = cameraId ?: return false
        return try {
            cameraManager?.setTorchMode(id, enabled)
            isFlashlightOn = enabled
            onStateChangedListener?.invoke(enabled)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun release() {
        try {
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
