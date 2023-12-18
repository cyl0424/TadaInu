package com.mobileprogramming.tadainu.homeFeat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DateItemDao {
    @Query("SELECT * FROM TB_DATE_ITEMS")
    fun getAllDateItems(): List<DateItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(dateItems: List<DateItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dateItem: DateItem)

    @Query("DELETE FROM TB_DATE_ITEMS")
    fun deleteAll()
}