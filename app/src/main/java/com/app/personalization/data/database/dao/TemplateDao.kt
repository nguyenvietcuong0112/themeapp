package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.Template

@Dao
interface TemplateDao {
    @Query("SELECT * FROM diy_templates")
    fun getAllTemplates(): List<Template>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTemplates(templates: List<Template>)
}
