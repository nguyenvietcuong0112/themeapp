package com.app.personalization.presentation.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import java.util.Random

class DiyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return ParticleEngine()
    }

    inner class ParticleEngine : Engine() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        private val particles = ArrayList<Particle>()
        private val random = Random()
        private var visible = false
        private val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (visible) {
                    handler.postDelayed(this, 16) // ~60fps
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(drawRunnable)
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                // Spawn glowing particles on tap/drag
                for (i in 0 until 5) {
                    particles.add(
                        Particle(
                            x = event.x,
                            y = event.y,
                            vx = (random.nextFloat() - 0.5f) * 6,
                            vy = (random.nextFloat() - 0.5f) * 6,
                            radius = random.nextFloat() * 8 + 4,
                            color = getRandomSparkleColor(),
                            alpha = 255
                        )
                    )
                }
            }
        }

        private fun getRandomSparkleColor(): Int {
            val colors = listOf("#00E5FF", "#7C4DFF", "#FF4081", "#FFD700", "#FFFFFF")
            return Color.parseColor(colors[random.nextInt(colors.size)])
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    // Draw base gradient/color background
                    canvas.drawColor(Color.parseColor("#12121A"))
                    
                    // Update and draw particles
                    val iterator = particles.iterator()
                    while (iterator.hasNext()) {
                        val p = iterator.next()
                        
                        // Update position and alpha fade
                        p.x += p.vx
                        p.y += p.vy
                        p.alpha -= 5
                        
                        if (p.alpha <= 0) {
                            iterator.remove()
                        } else {
                            paint.color = p.color
                            paint.alpha = p.alpha
                            canvas.drawCircle(p.x, p.y, p.radius, paint)
                        }
                    }
                    
                    // Spawn random ambient sparkles falling from the top
                    if (random.nextFloat() < 0.15f && canvas.width > 0) {
                        particles.add(
                            Particle(
                                x = random.nextFloat() * canvas.width,
                                y = 0f,
                                vx = (random.nextFloat() - 0.5f) * 2,
                                vy = random.nextFloat() * 3 + 1,
                                radius = random.nextFloat() * 4 + 2,
                                color = Color.WHITE,
                                alpha = 200
                            )
                        )
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        var color: Int,
        var alpha: Int
    )
}
