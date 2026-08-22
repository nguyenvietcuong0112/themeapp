package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

class VolumeController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var onVolumeChangedListener: ((current: Int, max: Int, percent: Float) -> Unit)? = null

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION" ||
                intent?.action == "android.media.EXTRA_VOLUME_STREAM_VALUE"
            ) {
                notifyVolume()
            }
        }
    }

    init {
        try {
            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            context.registerReceiver(volumeReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOnVolumeChangedListener(listener: (current: Int, max: Int, percent: Float) -> Unit) {
        this.onVolumeChangedListener = listener
        notifyVolume()
    }

    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    }

    fun getCurrentVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun getVolumePercent(): Float {
        val max = getMaxVolume()
        val current = getCurrentVolume()
        return (current.toFloat() / max).coerceIn(0f, 1f)
    }

    fun setVolumePercent(percent: Float) {
        val max = getMaxVolume()
        val target = (percent.coerceIn(0f, 1f) * max).toInt()
        setVolume(target)
    }

    fun setVolume(volume: Int) {
        val max = getMaxVolume()
        val safeVolume = volume.coerceIn(0, max)
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, safeVolume, 0)
            notifyVolume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun notifyVolume() {
        val max = getMaxVolume()
        val current = getCurrentVolume()
        val percent = (current.toFloat() / max).coerceIn(0f, 1f)
        onVolumeChangedListener?.invoke(current, max, percent)
    }

    fun release() {
        try {
            context.unregisterReceiver(volumeReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
