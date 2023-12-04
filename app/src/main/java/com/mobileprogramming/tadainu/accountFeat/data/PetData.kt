package com.mobileprogramming.tadainu.accountFeat.data

import com.google.firebase.Timestamp

data class PetData(
    var pet_adopt_day: String = "",
    var pet_at_home: Boolean = false,
    var pet_birthday: String = "",
    var pet_created_at: Timestamp = Timestamp.now(),
    var pet_gender: String = "",
    var pet_id: String = "",
    var pet_img: String = "",
    var pet_name: String = ""
)
