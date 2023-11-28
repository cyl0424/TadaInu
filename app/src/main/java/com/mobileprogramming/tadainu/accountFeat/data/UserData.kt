package com.mobileprogramming.tadainu.accountFeat.data

import com.google.firebase.Timestamp

data class UserData(
    var user_blocking: MutableList<String> = mutableListOf(),
    var user_id: String = "",
    var user_follower: MutableList<String> = mutableListOf(),
    var user_following: MutableList<String> = mutableListOf(),
    var user_type: String = "USER",
    var user_email: String = "",
    var user_pet: MutableMap<String, String> = mutableMapOf(),
    var user_created_at: Timestamp = Timestamp.now(),
    var user_phone: String = "",
    var user_name: String = ""
)