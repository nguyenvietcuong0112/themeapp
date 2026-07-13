package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.ChargingAnimation

@Dao
interface ChargingAnimationDao {
    @Query("SELECT * FROM charging_animations")
    fun getAllAnimations(): List<ChargingAnimation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnimations(animations: List<ChargingAnimation>)

    @Update
    fun updateAnimation(animation: ChargingAnimation)
}
