package com.mobileprogramming.tadainu.model

data class PartnerInfo(
    val petcareId: String = "",
    val petcareName: String = "",
    val petcareType: String = "",
    val petcareOpening: String = "",
    val petcareClosing: String = "",
    val petcareAddress: String = "",
    val position: com.naver.maps.geometry.LatLng
)
