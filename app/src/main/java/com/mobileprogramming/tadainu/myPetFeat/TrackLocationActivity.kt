package com.mobileprogramming.tadainu.myPetFeat

import android.content.Context
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobileprogramming.tadainu.BuildConfig
import com.mobileprogramming.tadainu.GlobalApplication
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityTrackLocationBinding
import com.mobileprogramming.tadainu.model.PetLocation
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header

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
    private val realtimeDb = FirebaseDatabase.getInstance().getReference()
    private var canTrack = false
    private lateinit var infoWindow: InfoWindow
    private val kakaoApiManager = KakaoApiManager(BuildConfig.KAKAO_API_KEY)

    private val petId = prefs.getString("petId", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
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

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        dogLocation = Marker()
        val redCircleBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(redCircleBitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawCircle(25f, 25f, 25f, paint)
        paint.color = Color.RED
        canvas.drawCircle(25f, 25f, 20f, paint)
        dogLocation.icon = OverlayImage.fromBitmap(redCircleBitmap)

        realtimeDb.child("PetLocation").child(petId).addValueEventListener(object : ValueEventListener {
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
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        infoWindow = InfoWindow()

        dogLocation.setOnClickListener { overlay ->
            val position = (overlay as? Marker)?.position
            position?.let {
                kakaoApiManager.getAddressFromCoordinates(it.longitude, it.latitude) { address ->
                    runOnUiThread {
                        val content = "현재 주소: $address"
                        infoWindow.position = it
                        infoWindow.adapter = CustomInfoWindowAdapter(this, content)
                        infoWindow.open(dogLocation)
                    }
                }
            }
            true
        }
        binding.toolbar.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
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
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setCanTrack(value: Boolean) {
        val updates = hashMapOf<String, Any>("canTrack" to value)
        realtimeDb.child("PetLocation").child(petId).updateChildren(updates)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }

    class CustomInfoWindowAdapter(context: Context, private val content: String) : InfoWindow.DefaultTextAdapter(context) {
        override fun getText(p0: InfoWindow): CharSequence {
            return content
        }
    }

    interface KakaoApiService {
        @GET("/v2/local/geo/coord2address.json")
        fun reverseGeocoding(
            @Header("Authorization") authorization: String,
            @Query("x") longitude: Double,
            @Query("y") latitude: Double,
            @Query("input_coord") inputCoord: String = "WGS84"
        ): Call<ApiResponse<AddressResponse>>
    }
    data class ApiResponse<T>(
        val documents: List<T>
    )

    data class AddressResponse(
        val address: Address
    )

    data class Address(
        val address_name: String
    )

    class KakaoApiManager(private val apiKey: String) {
        private val retrofit: Retrofit
        private val kakaoApiService: KakaoApiService

        init {
            retrofit = Retrofit.Builder()
                .baseUrl("https://dapi.kakao.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            kakaoApiService = retrofit.create(KakaoApiService::class.java)
        }

        fun getAddressFromCoordinates(longitude: Double, latitude: Double, callback: (String?) -> Unit) {
            val authorizationHeader = "KakaoAK $apiKey"
            val call = kakaoApiService.reverseGeocoding(authorizationHeader, longitude, latitude, "WGS84")
            call.enqueue(object : Callback<ApiResponse<AddressResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<AddressResponse>>,
                    response: Response<ApiResponse<AddressResponse>>
                ) {
                    if (response.isSuccessful) {
                        val address = response.body()?.documents?.firstOrNull()?.address?.address_name
                        callback(address)
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e("ITM", "API 요청 실패: $errorBody")
                        } catch (e: Exception) {
                            Log.e("ITM", "API 요청 실패: Unable to parse error body")
                        }
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<ApiResponse<AddressResponse>>, t: Throwable) {
                    callback(null)
                    Log.e("ITM", "API 요청 실패: ${t.message}")
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setCanTrack(false)
    }
}
