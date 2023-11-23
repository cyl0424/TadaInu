package com.mobileprogramming.tadainu.model

data class Feed(
    val feedId: String = "",
    val feedCreatedAt: Long = System.currentTimeMillis(),
    val feedDescription: String = "",
    val feedImg: List<String> = emptyList(),
    val feedLike: Int = 0,
    val feedPet: String? = null,
    val feedWriterId: String? = null
)