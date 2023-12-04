package com.mobileprogramming.tadainu.model

data class PetLocation(
    val canTrack: Boolean = false,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)