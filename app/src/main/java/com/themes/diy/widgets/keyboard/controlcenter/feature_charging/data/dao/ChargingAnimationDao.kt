package com.themes.diy.widgets.keyboard.controlcenter.feature_charging.data.dao

import androidx.room.*
import com.themes.diy.widgets.keyboard.controlcenter.feature_charging.data.entity.ChargingAnimation

@Dao
interface ChargingAnimationDao {
    @Query("SELECT * FROM charging_animations")
    fun getAllAnimations(): List<ChargingAnimation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnimations(animations: List<ChargingAnimation>)

    @Update
    fun updateAnimation(animation: ChargingAnimation)
}
