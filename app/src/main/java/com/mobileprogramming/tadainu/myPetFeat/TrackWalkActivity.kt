package com.mobileprogramming.tadainu.myPetFeat

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityTrackWalkBinding
import com.mobileprogramming.tadainu.model.PetLocation
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.overlay.PathOverlay
import java.text.SimpleDateFormat
import java.util.*

private lateinit var binding: ActivityTrackWalkBinding
private lateinit var naverMap: NaverMap
private lateinit var locationSource: FusedLocationSource
private lateinit var dogLocation: Marker
private val realtimeDb = FirebaseDatabase.getInstance().getReference()
private var isStart = false
private var startTime: Long? = null
private var endTime: Long? = null
private var totalTimeMillis: Long? = null
private var totalDistance: Double = 0.0
private val pendingPathPoints: MutableList<LatLng> = mutableListOf()

// 초코밖에 테스트가 안돼요 ㅠ
private val petId = "4Jipcx2xHXmvcKNVc6cO"

class TrackWalkActivity : AppCompatActivity(), OnMapReadyCallback {
    val db = Firebase.firestore
    private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
    private val storageRef: StorageReference = storage.reference

    private lateinit var naverMap: NaverMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000
    private lateinit var pathOverlay: PathOverlay

//    private val petId = GlobalApplication.prefs.getString("petId", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackWalkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.toolbarTitle.text = "산책트래킹"

        val petCollection = db.collection("TB_PET")

        val docRef = petCollection.document(petId)

        binding.toolbar.backBtn.setOnClickListener {
            onBackPressed()
        }

        if(docRef != null){
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        storageRef.child(document["pet_img"].toString()).downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Glide.with(binding.root.context)
                                    .load(task.result)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .apply(RequestOptions().circleCrop())
                                    .thumbnail(0.1f)
                                    .into(binding.toolbar.petImg)
                            }
                        }

                        Log.d("MP", "DocumentSnapshot data: ${document.data}")
                    } else {
                        Log.d("MP", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("MP", "get failed with ", exception)
                }
        }

        // 시작 or 종료
        binding.controlBtn.setOnClickListener {
            Log.d("ITM", "controlBtn Clicked")

            // 현재 start 중인 상태였으면 종료 = 산책 종료
            if (isStart) {
                // Ending the walk
                binding.controlBtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.partners_clicked)

                binding.startOrEnd.text = "시작"
                binding.startOrEnd.setTextColor(ContextCompat.getColor(this, R.color.white))
                setCanTrack(false)
                isStart = false
                endTime = System.currentTimeMillis()

                // 총 산책 시간 계산
                totalTimeMillis = endTime!! - startTime!!

                // 총 산책 거리 계산
                calculateTotalDistanceBetweenPoints(pendingPathPoints)
                val formattedTime = totalTimeMillis?.let { it1 -> formatMillisToTime(it1) }
                val avgSpeed = calculateAverageSpeed(totalDistance, totalTimeMillis!!)
                // 결과 로그용
                Log.d("ITM", "Total Time: $formattedTime")
                Log.d("ITM", "Total Distance: $totalDistance")
                Log.d("ITM", "Average Speed: $avgSpeed")

                if (startTime != null && endTime != null) {
                    // 위치 그리기
                    drawPathOnMap(pendingPathPoints)
                }
                showWalkSummaryDialog()
            } else {
                // Starting the walk
                binding.controlBtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, android.R.color.white)
                resetWalkData()
                binding.startOrEnd.text = "그만"
                binding.startOrEnd.setTextColor(ContextCompat.getColor(this, R.color.base_black))
                setCanTrack(true)
                isStart = true
                startTime = System.currentTimeMillis()
            }
        }

        initMapView()
    }
    private fun calculateTotalDistanceBetweenPoints(points: List<LatLng>): Double {
        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            val lat1 = points[i].latitude
            val lon1 = points[i].longitude
            val lat2 = points[i + 1].latitude
            val lon2 = points[i + 1].longitude
            totalDistance += calculateDistance(lat1, lon1, lat2, lon2)
        }
        return totalDistance
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 * 1000 // Radius of the Earth in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distanceInMeters = R * c
        return distanceInMeters
    }


    private fun calculateAverageSpeed(distance: Double, timeInMillis: Long): Double {
        // Convert time to minutes
        val timeInMinutes = timeInMillis / (1000.0 * 60.0)
        return if (timeInMinutes > 0) distance / timeInMinutes else 0.0
    }
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // PathOverlay 초기화
        pathOverlay = PathOverlay()
        pathOverlay.width = resources.getDimensionPixelSize(R.dimen.path_overlay_width)
        pathOverlay.color = ContextCompat.getColor(this, R.color.red005)

        dogLocation = Marker()
        val redCircleBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(redCircleBitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawCircle(25f, 25f, 25f, paint)
        paint.color = Color.RED
        canvas.drawCircle(25f, 25f, 20f, paint)
        // 강아지용 마커
        dogLocation.icon = OverlayImage.fromBitmap(redCircleBitmap)

        // 실시간 DB 값 바뀔때마다 좌표 위치 변경해주고 추후 거리 및 속력 계산시 활용
        realtimeDb.child("PetLocation").child(petId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val petLocation = dataSnapshot.getValue(PetLocation::class.java)
                    petLocation?.let {
                        val lat = it.lat
                        val lng = it.lng
                        val canTrack = it.canTrack

                        Log.d("ITM", "Lat: $lat, Lng: $lng, Can Track: $canTrack")

                        runOnUiThread {
                            dogLocation.position = LatLng(lat, lng)
                            dogLocation.map = naverMap

                            // 산책 중일 때만 경로 추가
                            if (isStart) {
                                pendingPathPoints.add(LatLng(lat, lng))
                                Log.d("ITM","$pendingPathPoints")
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    // 그리기
    private fun drawPathOnMap(pathPoints: List<LatLng>) {
        if (pathPoints.size >= 2) {
            // PathOverlay의 좌표 설정
            pathOverlay.coords = pathPoints
            pathOverlay.map = naverMap // Set the map after setting the coords
        }
    }

    // 처음에 지도 초기화 용
    private fun initMapView() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().replace(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    // TrackingLocation Appplication위해서
    private fun setCanTrack(value: Boolean) {
        val updates = hashMapOf<String, Any>("canTrack" to value)
        realtimeDb.child("PetLocation").child(petId).updateChildren(updates)
            .addOnSuccessListener {
                Log.d("ITM", "canTrack 값을 $value 로 업데이트 성공")
            }
            .addOnFailureListener {
                Log.e("ITM", "canTrack 값을 $value 로 업데이트 실패: ${it.message}")
            }
    }

    // 산책 종료 후 통계 Dialog
    private fun showWalkSummaryDialog() {
        val formattedTime = totalTimeMillis?.let { formatMillisToTime(it) }
        val formattedDistance = String.format("%.2f", totalDistance)
        val formattedSpeed = String.format("%.2f", calculateAverageSpeed(totalDistance, totalTimeMillis!!))

        val message =
            "총 산책 시간: $formattedTime\n총 산책 거리: $formattedDistance m\n평균 속력: $formattedSpeed m/min"

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("산책 기록")
            .setMessage(message)

            .setNegativeButton("산책 경로 확인하기") { _, _ ->
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
    private fun formatMillisToTime(millis: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss")
        formatter.timeZone = TimeZone.getTimeZone("GMT")
        return formatter.format(Date(millis))
    }

    private fun resetWalkData() {
        pendingPathPoints.clear()
        totalDistance = 0.0
        startTime = null
        endTime = null
        totalTimeMillis = null
    }
}
