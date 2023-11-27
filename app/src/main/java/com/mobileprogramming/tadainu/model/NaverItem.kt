package com.mobileprogramming.tadainu.model

import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng

// TedNaverMapClustering 위한 데이터 클래스
data class NaverItem(val position: com.naver.maps.geometry.LatLng) : TedClusterItem {
    override fun getTedLatLng(): TedLatLng {
        return TedLatLng(position.latitude, position.longitude)
    }
}
