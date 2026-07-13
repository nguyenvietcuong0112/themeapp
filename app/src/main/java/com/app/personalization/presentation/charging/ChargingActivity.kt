package com.app.personalization.presentation.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.app.personalization.R
import com.app.personalization.data.ResourceConfig

class ChargingActivity : AppCompatActivity() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private val powerConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_DISCONNECTED) {
                finish()
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen setup
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        setContentView(R.layout.activity_charging)

        playerView = findViewById(R.id.playerView)

        // Read animation configuration
        val prefs = getSharedPreferences("charging_prefs", MODE_PRIVATE)
        val folder = prefs.getString("applied_charging_folder", "charging/charging_1") ?: "charging/charging_1"

        val config = resources.configuration
        val isFold = config.smallestScreenWidthDp >= 600
        val videoUrl = ResourceConfig.getChargingVideoUrl(folder, isFold)

        setupPlayer(videoUrl)

        // Register power disconnected receiver
        val filter = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(powerConnectionReceiver, filter)

        // Tap to dismiss
        findViewById<View>(R.id.rootView).setOnClickListener {
            finish()
        }
        playerView.setOnClickListener {
            finish()
        }
    }

    private fun setupPlayer(url: String) {
        try {
            val player = ExoPlayer.Builder(this).build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
            }
            exoPlayer = player
            playerView.player = player
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to initialize video player", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(powerConnectionReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        releasePlayer()
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            player.release()
            exoPlayer = null
        }
    }
}
