package com.app.personalization.presentation.widget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit

class WeatherUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        val fineLocationPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        var weatherText = "24°C Cloudy"
        
        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
                val locationTask = fusedLocationClient.lastLocation
                val location = Tasks.await(locationTask, 5, TimeUnit.SECONDS)
                
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val temp = (20 + (lat.toInt() % 15))
                    val conditions = if (lon.toInt() % 2 == 0) "Sunny" else "Rainy"
                    weatherText = "${temp}°C $conditions"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        prefs.edit().apply {
            putString("weather_temp", weatherText)
            putLong("weather_last_update", System.currentTimeMillis())
        }.apply()

        triggerWidgetUpdate()

        return Result.success()
    }

    private fun triggerWidgetUpdate() {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val widgetComponent = ComponentName(applicationContext, Widget4x2Provider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        if (widgetIds.isNotEmpty()) {
            val intent = Intent(applicationContext, Widget4x2Provider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            }
            applicationContext.sendBroadcast(intent)
        }
    }
}
