package com.mobileprogramming.tadainu.partnersFeat

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.FragmentPartnersMapSubBinding
import com.mobileprogramming.tadainu.databinding.PartnerMapDialogBinding
import com.mobileprogramming.tadainu.databinding.PartnerSlidingLayoutBinding
import com.mobileprogramming.tadainu.model.ClusteredPartnerName
import com.mobileprogramming.tadainu.model.NaverItem
import com.mobileprogramming.tadainu.model.PartnerInfo
import com.mobileprogramming.tadainu.partnersFeat.adapter.ClusterClickAdapter
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import ted.gun0912.clustering.naver.TedNaverClustering
import java.text.SimpleDateFormat
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PartnersMapSubFragment : Fragment(), ClusterClickAdapter.OnItemClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentPartnersMapSubBinding
    private lateinit var slidingBinding: PartnerSlidingLayoutBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000
    private val firestore = FirebaseFirestore.getInstance()
    private val firestorage = FirebaseStorage.getInstance()
    private lateinit var dialogBinding: PartnerMapDialogBinding
    private val items = mutableListOf<NaverItem>()
    private val partnerInfoList = mutableListOf<PartnerInfo>()
    // 마커에 정보 저장
    private var selectedPartnerInfo: PartnerInfo? = null
    //cluster
    private val clusteredPartnerNameList = mutableListOf<ClusteredPartnerName>()
    private lateinit var slidingLayout: SlidingUpPanelLayout

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
        dialogBinding = PartnerMapDialogBinding.inflate(layoutInflater) // Add this line
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
        slidingLayout = binding.slidingLayout  // 수정된 부분
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

            getMarkers()
        }
    }

    // firestore의 유치원 및 호텔 좌표 받아서 찍어주기
    private fun getMarkers() {
        firestore.collection("TB_PETCARE")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.getString("petcare_id")
                    val name = document.getString("petcare_name")
                    val type = document.getString("petcare_type")
                    val opening = document.getString("petcare_opening")
                    val closing = document.getString("petcare_closing")
                    val address = document.getString("petcare_addr")
                    val lat = document.getDouble("petcare_lat") ?: 0.0
                    val lng = document.getDouble("petcare_lng") ?: 0.0
                    if (id != null && name != null && type != null && opening != null && closing != null && address != null) {
                        val position = com.naver.maps.geometry.LatLng(lat, lng)
                        val partnerInfo = PartnerInfo(id, name, type, opening, closing, address, position)
                        items.add(NaverItem(position))
                        partnerInfoList.add(partnerInfo)
                    }
                }

                TedNaverClustering.with<NaverItem>(requireContext(), naverMap)
                    .customMarker {
                        Marker().apply {
                            icon = OverlayImage.fromResource(R.drawable.icon_map_marker)
                            width = 50
                            height = 70
                        }
                    }
                    .markerClickListener { marker ->
                        // 클릭한 마커의 위치를 가져오고 해당 위치에 대응되는 파트너 정보를 찾아 selectedPartnerInfo에 저장
                        val clickedPosition = marker.position
                        selectedPartnerInfo =
                            partnerInfoList.find { it.position == clickedPosition }

                        // 다이얼로그 업데이트 메소드 호출
                        updateDialogWithSelectedPartner()
                    }
                    .clusterClickListener { cluster ->
                        val clusteredPartnerNameList = mutableListOf<ClusteredPartnerName>()

                        // 클릭한 클러스터에 속한 파트너의 리스트를 가져옵니다.
                        for (item in cluster.items) {
                            for (partner in partnerInfoList) {
                                if (item.position == partner.position) {
                                    clusteredPartnerNameList.add(ClusteredPartnerName(partner.petcareName))
                                }
                            }
                        }

                        // Set up RecyclerView and adapter for the clustered partner list
                        binding.clusteredPartnerList.adapter = ClusterClickAdapter(requireContext(), clusteredPartnerNameList, this)
                        binding.clusteredPartnerList.layoutManager = LinearLayoutManager(requireContext())
                        binding.clusteredPartnerList.adapter?.notifyDataSetChanged()

                        // Show the sliding panel
                        binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                    }
                    .items(items)
                    .make()

                // Perform any UI updates or logic that depends on the fetched data here
                if (partnerInfoList.isNotEmpty()) {
                    Log.d("ITM", "${partnerInfoList[0].petcareName}")
                } else {
                    Log.d("ITM", "partnerInfoList is empty")
                }

                // Continue with the clustering or other logic here...
            }
    }
    private fun updateDialogWithSelectedPartner() {
        Log.d("ClusterClickAdapter","들어왔음.")
        Log.d("UpdateDialog", "selectedPartnerInfo: $selectedPartnerInfo")
        selectedPartnerInfo?.let { partnerInfo ->
            // 다이얼로그 업데이트 로직 수행
            val builder = AlertDialog.Builder(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.partner_map_dialog, null) // replace with your dialog layout

            // Initialize dialogBinding using the inflated view
            dialogBinding = PartnerMapDialogBinding.bind(dialogView)

            builder.setView(dialogView)
            val dialog = builder.create()

            // Continue with your dialog logic
            firestore.collection("TB_PETCARE")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Assuming you want the first document in the collection
                    val document = querySnapshot.documents.firstOrNull()

                    // Check if document is not null before accessing its fields
                    if (document != null) {
                        val partnerType = when (partnerInfo.petcareType) {
                            "k" -> "애견 유치원"
                            "h" -> "애견 호텔"
                            else -> "Unknown Type"
                        }
                        dialogBinding.dialogPartnerType.text = partnerType
                        dialogBinding.dialogPartnerName.text = partnerInfo.petcareName
                        val currentTime = System.currentTimeMillis()
                        val openingTime = parseTimeString(partnerInfo.petcareOpening)
                        val closingTime = parseTimeString(partnerInfo.petcareClosing)

                        if (currentTime in openingTime..closingTime) {
                            dialogBinding.dialogIsOpen.text = "영업 중"
                            dialogBinding.dialogOpenCloseTime.text = "${partnerInfo.petcareClosing}에 영업 종료"
                        } else {
                            dialogBinding.dialogIsOpen.text = "영업 종료"
                            dialogBinding.dialogOpenCloseTime.text = "${partnerInfo.petcareOpening}에 영업 시작"
                        }
                        dialogBinding.dialogPartnerAddress.text = partnerInfo.petcareAddress
                        // Continue updating other UI elements as needed
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting documents: ", exception)
                }

            dialog?.window?.attributes?.gravity = Gravity.BOTTOM
            dialog.show()
        }
    }

    // 클러스터 클릭 시 뜨는 리사이클러 뷰의 리스트 요소를 클릭 시 해당하는 업체의 다이얼로그 띄우기
    override fun onItemClick(partner: ClusteredPartnerName) {
        // Add logs to verify that onItemClick is called
        Log.d("ClusterClickAdapter", "Item Clicked: ${partner.clusteredpartnerName}")
        selectedPartnerInfo =
            partnerInfoList.find { it.petcareName == partner.clusteredpartnerName }
        updateDialogWithSelectedPartner()
    }

    private fun parseTimeString(timeString: String): Long {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val parsedDate = dateFormat.parse(timeString)
        return parsedDate?.time ?: 0L
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
