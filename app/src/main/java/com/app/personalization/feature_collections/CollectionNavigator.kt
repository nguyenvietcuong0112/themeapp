package com.app.personalization.feature_collections

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.app.personalization.feature_control_center.ControlCenterPreviewBottomSheet
import com.app.personalization.feature_icon.DownloadIconActivity
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme
import com.app.personalization.feature_theme.ThemePreviewActivity
import com.app.personalization.feature_wallpaper.DownloadWallpaperActivity
import com.app.personalization.feature_widget.SelectWidgetBottomSheet
import com.app.personalization.feature_collections.data.CollectionItem

object CollectionNavigator {

    fun navigateToDetail(activity: Activity, item: CollectionItem) {
        val categoryKey = item.category.lowercase()
        when {
            categoryKey.contains("theme") -> {
                val intent = Intent(activity, ThemePreviewActivity::class.java).apply {
                    putExtra("theme_id", item.id)
                    putExtra("theme_name", item.name)
                    putExtra("theme_path", item.targetPath)
                    putExtra("theme_type", item.rawType.ifEmpty { "theme" })
                }
                activity.startActivity(intent)
            }

            categoryKey.contains("icon") -> {
                val intent = Intent(activity, DownloadIconActivity::class.java).apply {
                    putExtra("theme_id", item.id)
                    putExtra("theme_name", item.name)
                    putExtra("theme_path", item.targetPath)
                    putExtra("theme_type", "icon_theme")
                }
                activity.startActivity(intent)
            }

            categoryKey.contains("control") -> {
                val bottomSheet = ControlCenterPreviewBottomSheet().apply {
                    setItem(item)
                }
                if (activity is FragmentActivity) {
                    bottomSheet.show(activity.supportFragmentManager, "control_center_preview")
                }
            }

            categoryKey.contains("widget") -> {
                val widgetType = item.rawType.ifEmpty { "clock" }
                val size = item.extra ?: if (widgetType == "calendar") "4x2" else "2x2"
                val sheet = SelectWidgetBottomSheet()
                val dummyTheme = KeyboardTheme(
                    id = "widget_${item.targetPath.replace('/', '_')}",
                    name = item.name,
                    path = item.targetPath,
                    rawType = "widget_theme"
                )
                sheet.setParams(
                    dummyTheme,
                    widgetType,
                    size,
                    item.previewPath,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (activity is FragmentActivity) {
                    sheet.show(activity.supportFragmentManager, "select_widget")
                }
            }

            categoryKey.contains("wallpaper") -> {
                val intent = Intent(activity, DownloadWallpaperActivity::class.java).apply {
                    val fullWallpaperUrl = if (item.previewPath.contains("preview.png")) {
                        item.previewPath.replace("preview.png", "wallpaper.png")
                    } else {
                        item.previewPath
                    }
                    putExtra("wallpaper_path", fullWallpaperUrl)
                    putExtra("wallpaper_title", item.name)
                    putExtra("source_type", "collection")
                    putExtra("collection_id", item.id)
                }
                activity.startActivity(intent)
            }
        }
    }
}
