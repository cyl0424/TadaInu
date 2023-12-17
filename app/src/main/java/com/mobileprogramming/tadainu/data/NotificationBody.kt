package com.mobileprogramming.tadainu.data

data class NotificationBody(
    val to: String,
    val data: NotificationData
) {
    data class NotificationData(
        val title: String,
        val userId : String,
        val message: String,
        val postId : String,
        val destinationUid : String,
        val type : String
    )
}