package com.mobileprogramming.tadainu.homeFeat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "TB_DATE_ITEMS", indices = [Index(value = ["date"], unique = true)])
data class DateItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val date: Int,
    @ColumnInfo(name = "photoPath") val photoPath: String
)
