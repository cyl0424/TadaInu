package com.mobileprogramming.tadainu.myPetFeat

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityTrackLocationBinding
import com.mobileprogramming.tadainu.model.PetLocation
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource


class TrackLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    val db = Firebase.firestore
    private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
    private val storageRef: StorageReference = storage.reference

    private val LOCATION_PERMISSION_REQUEST_CODE = 5000
    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var binding: ActivityTrackLocationBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var dogLocation: Marker
    val realtimeDb = FirebaseDatabase.getInstance().getReference()
    private var canTrack = false
    private val petId = prefs.getString("petId", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // 권한 있을 때
            Log.d("ITM", "권한 있음")
            setCanTrack(true)
            initMapView()
            initFirebase()
            binding.toolbar.toolbarTitle.text = "위치추적"
        }

        val petCollection = db.collection("TB_PET")

        val docRef = petCollection.document(petId)

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
    }


    // 처음 강아지 위치 값 받아오기
    override fun onMapReady(naverMap: NaverMap) {
        Log.d("ITM", "onMapRead() Start")
        this.naverMap = naverMap

        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 강아지 위치 마커 초기화
        dogLocation = Marker()
        val redCircleBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(redCircleBitmap)
        val paint = Paint()
        // 강아지 좌표 디자인
        paint.color = Color.WHITE
        canvas.drawCircle(25f, 25f, 25f, paint)
        paint.color = Color.RED
        canvas.drawCircle(25f, 25f, 20f, paint)
        dogLocation.icon = OverlayImage.fromBitmap(redCircleBitmap)

        // 실시간으로 값 들고오기
        realtimeDb.child("PetLocation").child(petId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val petLocation = dataSnapshot.getValue(PetLocation::class.java)
                    if (petLocation != null) {
                        val lat = petLocation.lat
                        val lng = petLocation.lng
                        canTrack = petLocation.canTrack

                        Log.d("ITM", "Lat: $lat, Lng: $lng, Can Track: $canTrack")
                        runOnUiThread {
                            // Update marker position on the main thread
                            dogLocation.position = LatLng(lat, lng)
                            dogLocation.map = naverMap
                        }

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled if needed
            }
        })

        binding.toolbar.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    // 파이어베이스 초기화
    private fun initFirebase() {
        Log.d("ITM", "initFirebase() Start")
        // Initialize the FirebaseApp (if not already initialized)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }
    // 지도 View 초기화
    private fun initMapView() {
        Log.d("ITM","initMapView() Start")
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().replace(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        Log.d("ITM","initMapView() End")
    }

    // 권한 확인
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

    // 권한 요청 결과 처리
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

    // canTrack값 설정하기
    private fun setCanTrack(value: Boolean) {
        val updates = hashMapOf<String, Any>("canTrack" to value)
        realtimeDb.child("PetLocation").child(petId).updateChildren(updates)
            .addOnSuccessListener {
                // 성공적으로 업데이트된 경우
                // 원하는 작업 수행
                Log.d("ITM", "canTrack 값을 ${value}로 업데이트 성공")
            }
            .addOnFailureListener {
                // 업데이트 중 오류 발생한 경우
                // 오류 처리 코드 작성
                Log.e("ITM", "canTrack 값을 ${value}로 업데이트 실패: ${it.message}")
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        setCanTrack(false)
    }
}
