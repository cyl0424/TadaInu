package com.mobileprogramming.tadainu.homeFeat.data

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

object InitialDataUtil {
    fun initializeData(context: Context) {
        GlobalScope.launch {
            val dateItemDao = AppDatabase.getInstance(context).dateItemDao()
//            dateItemDao.deleteAll()

            // 이미 데이터가 존재하는지 확인
            if (dateItemDao.getAllDateItems().isEmpty()) {
                // 초기 데이터 생성
                val initialDateItems = mutableListOf<DateItem>()

                for (i in 1..31) {
                    // 각 날짜에 대한 초기 데이터 생성
                    val photoPath = "@null"
                    val dateItem = DateItem(date = i, photoPath = photoPath)
                    if(i==3 || i==6 || i==9 || i==17){
                        continue
                    }
                    initialDateItems.add(DateItem(date=3, photoPath = "pet/20231203.jpeg"))
                    initialDateItems.add(DateItem(date=6, photoPath = "pet/20231206.jpeg"))
                    initialDateItems.add(DateItem(date=9, photoPath = "pet/20231209.jpeg"))
                    initialDateItems.add(DateItem(date=17, photoPath = "pet/20231217.jpeg"))
                    initialDateItems.add(dateItem)
                }
                initialDateItems.sortBy { it.date }

                // 초기 데이터 삽입
                dateItemDao.insertAll(initialDateItems)
            }
        }
    }

    private fun getCurrentDate(): Int {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return day
    }
}
