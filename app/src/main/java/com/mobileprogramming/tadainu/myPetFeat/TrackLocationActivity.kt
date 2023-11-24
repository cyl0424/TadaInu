package com.mobileprogramming.tadainu.myPetFeat

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityTrackLocationBinding
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource


private const val PET_ID = "Default_Value"
class TrackLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 5000

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var binding: ActivityTrackLocationBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            initMapView()
        }
    }

    private fun initMapView() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().replace(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }


    private fun hasPermission(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 강아지 위치 트래킹
        // 임시로 찍어서 좌표 디자인함
        val dogLocation = Marker()
        dogLocation.position = com.naver.maps.geometry.LatLng(37.5666102, 126.9783881)

        val redCircleBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(redCircleBitmap)
        val paint = Paint()

        // 강아지 좌표 디자인
        paint.color = Color.WHITE
        canvas.drawCircle(25f, 25f, 25f, paint)
        paint.color = Color.RED
        canvas.drawCircle(25f, 25f, 20f, paint)
        dogLocation.icon = OverlayImage.fromBitmap(redCircleBitmap)
        dogLocation.map = naverMap


        // 값이 변해야지 작동
//        val databaseReference = FirebaseDatabase.getInstance().getReference("PetLocation/4Jipcx2xHXmvcKNVc6cO")
//        databaseReference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                // Get the lat and lng values from the snapshot
//                val lat = snapshot.child("lat").getValue(Double::class.java) ?: 0.0
//                val lng = snapshot.child("lng").getValue(Double::class.java) ?: 0.0
//
//                dogLocation.position = com.naver.maps.geometry.LatLng(lat, lng)
//                Log.d("ITM","lat:$lat, lng:$lng")
//
//                dogLocation.map = naverMap
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("Firebase", "Error getting data", error.toException())
//            }
//        })

    }

}
