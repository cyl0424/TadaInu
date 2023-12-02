package com.mobileprogramming.tadainu.model

import android.net.Uri

data class Feed(
    val feedId: String = "",
    val feedCreatedAt: com.google.firebase.Timestamp,
    val feedDescription: String = "",
    val feedImgPaths: List<String>,
    val feedLike: Int = 0,
    val feedPet: String? = null,
    val feedWriterId: String? = null
)