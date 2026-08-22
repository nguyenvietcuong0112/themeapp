package com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.data.dao

import androidx.room.*
import com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.data.entity.Template

@Dao
interface TemplateDao {
    @Query("SELECT * FROM diy_templates")
    fun getAllTemplates(): List<Template>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTemplates(templates: List<Template>)
}
