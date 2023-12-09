package com.mobileprogramming.tadainu.myPetFeat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.mobileprogramming.tadainu.GlobalApplication
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityTrackWalkBinding
import java.text.SimpleDateFormat
import java.util.*

private lateinit var binding: ActivityTrackWalkBinding
private var isStart = false
private var startTime: Long? = null
private var endTime: Long? = null
private var totalTimeMillis: Long? = null
private var totalDistance: Double = 0.0
private val pathPoints: MutableList<GeoPoint> = mutableListOf()
private val petId = "4Jipcx2xHXmvcKNVc6cO" //GlobalApplication.prefs.getString("petId", "")
private val db = FirebaseFirestore.getInstance()
private var canTrack = false
val realtimeDb = FirebaseDatabase.getInstance().getReference()
class TrackWalkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackWalkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.controlBtn.setOnClickListener {
            Log.d("ITM", "controlBtn Clicked")
            if (isStart) {
                // Ending the walk
                binding.controlBtn.backgroundTintList =
                    resources.getColorStateList(R.color.partners_clicked)

                binding.startOrEnd.text = "시작"
                binding.startOrEnd.setTextColor(resources.getColor(R.color.white))
                setCanTrack(false)
                isStart = false
                endTime = System.currentTimeMillis()
                totalTimeMillis = endTime!! - startTime!!

                val formattedTime = totalTimeMillis?.let { it1 -> formatMillisToTime(it1) }
                Log.d("ITM", "Total Time: $formattedTime")

                if (startTime != null && endTime != null) {
                    // Query Firestore for location data within the time range
                 //   queryFirestoreForLocations(petId, startTime!!, endTime!!)
                }
            } else {
                // Starting the walk
                binding.controlBtn.backgroundTintList =
                    resources.getColorStateList(android.R.color.white)

                binding.startOrEnd.text = "그만"
                binding.startOrEnd.setTextColor(resources.getColor(R.color.base_black))
                setCanTrack(true)
                isStart = true
                startTime = System.currentTimeMillis()

            }
        }
    }

    private fun formatMillisToTime(millis: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss")
        formatter.timeZone = TimeZone.getTimeZone("GMT")
        return formatter.format(Date(millis))
    }

    private fun calculateTotalDistanceBetweenPoints(points: List<GeoPoint>) {
        Log.d("ITM", "calculateTotalDistanceBetweenPoints: $points")
        totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            Log.d("ITM", "points: ${points[i]}")
            val lat1 = points[i].latitude
            val lon1 = points[i].longitude
            val lat2 = points[i + 1].latitude
            val lon2 = points[i + 1].longitude
            totalDistance += calculateDistance(lat1, lon1, lat2, lon2)
        }
    }
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Radius of the Earth in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun calculateAverageSpeed(distance: Double, timeInMillis: Long): Double {
        // Convert time to hours
        val timeInHours = timeInMillis / (1000.0 * 60.0 * 60.0)
        return if (timeInHours > 0) distance / timeInHours else 0.0
    }

    private fun drawPathOnMap(pathPoints: List<GeoPoint>) {
        // Implement your logic to draw the path on the map
        // You can use Google Maps API or any other mapping library
        // Example: https://developers.google.com/maps/documentation/android-sdk/polyline
    }

//    private fun queryFirestoreForLocations(petId: String, startTimestamp: Long, endTimestamp: Long) {
//        Log.d("ITM", "Here is queryFirestoreForLocations")
//        db.collection("TB_LOC")
//            .document(petId)
//            .whereGreaterThanOrEqualTo("timestamp", Timestamp(Date(startTimestamp)))
//            .whereLessThanOrEqualTo("timestamp", Timestamp(Date(endTimestamp)))
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    val geoPoint = document.getGeoPoint("geoPoint")
//                    geoPoint?.let { pathPoints.add(it) }
//                }
//                calculateTotalDistanceBetweenPoints(pathPoints)
//            }
//            .addOnFailureListener { exception ->
//                Log.w("ITM", "Error getting documents: ", exception)
//            }
//    }

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

}