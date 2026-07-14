package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.AppIconItem
import com.app.personalization.data.database.entity.ThemeIconPack
import java.util.UUID

/**
 * Lớp truy vấn (DAO) cho các thao tác với thực thể ThemeIconPack và AppIconItem lẻ
 */
@Dao
interface IconDao {

    @Query("SELECT * FROM theme_icon_packs")
    fun getAllIconPacks(): List<ThemeIconPack>

    @Query("SELECT * FROM theme_icon_packs WHERE themeId = :themeId LIMIT 1")
    fun getIconPackByTheme(themeId: UUID): ThemeIconPack?

    @Query("SELECT * FROM app_icon_items WHERE iconPackId = :iconPackId")
    fun getIconsByPack(iconPackId: UUID): List<AppIconItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIconPack(pack: ThemeIconPack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIconItems(items: List<AppIconItem>)

    @Update
    fun updateIconItem(item: AppIconItem)
}
