package com.mobileprogramming.tadainu.partnersFeat

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.FragmentPartnersMapSubBinding
import com.mobileprogramming.tadainu.model.NaverItem
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import ted.gun0912.clustering.naver.TedNaverClustering

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PartnersMapSubFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentPartnersMapSubBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000
    private val firestore = FirebaseFirestore.getInstance()


    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPartnersMapSubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasPermission(requireContext())) {
            // 사용자에게 런타임 중 권한 요청할 때 쓰임.
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // 권한 있으면 지도 띄움.
            initMapView()
        }
    }

    private fun initMapView() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().replace(R.id.map, it).commit()
            }

        // 비동기로 맵 가져옴.
        mapFragment.getMapAsync { naverMap ->
            this.naverMap = naverMap
            locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

            naverMap.locationSource = locationSource

            //현재 위치 버튼이 활성화
            naverMap.uiSettings.isLocationButtonEnabled = true
            // 자동으로 사용자의 위치를 중앙에 맞추고 이동에 따라 사용자의 위치를 계속 추적
            naverMap.locationTrackingMode = LocationTrackingMode.Follow

//            val marker = Marker()
//            marker.position = com.naver.maps.geometry.LatLng(37.5666102, 126.9783881)
//            marker.map = naverMap
//
//            naverMap.moveCamera(CameraUpdate.scrollTo(marker.position))
            getMarkers()
        }
    }

    // firestore의 유치원 및 호텔 좌표 받아서 찍어주기
    private fun getMarkers() {
        firestore.collection("TB_PETCARE")
            .get()
            .addOnSuccessListener { documents ->
                val items = mutableListOf<NaverItem>()

                for (document in documents) {
                    val lat = document.getDouble("petcare_lat")
                    val lng = document.getDouble("petcare_lng")

                    if (lat != null && lng != null) {
                        val position = com.naver.maps.geometry.LatLng(lat, lng)
                        items.add(NaverItem(position))
                    }
                }

                // TedNaverClustering으로 찍기
                TedNaverClustering.with<NaverItem>(requireContext(), naverMap)
                    .items(items)
                    .make()
            }
    }


    private fun hasPermission(context: Context): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PartnersMapSubFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
