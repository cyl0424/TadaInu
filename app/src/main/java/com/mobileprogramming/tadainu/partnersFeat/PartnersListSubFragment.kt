package com.mobileprogramming.tadainu.partnersFeat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.mobileprogramming.tadainu.databinding.FragmentPartnersListSubBinding
import com.mobileprogramming.tadainu.model.PetcareItem
import com.mobileprogramming.tadainu.partnersFeat.adapter.PartnersAdapter
import com.mobileprogramming.tadainu.partnersFeat.adapter.PartnersListAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PartnersListSubFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PartnersListSubFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private val petcareList = mutableListOf<PetcareItem>()

    private lateinit var binding: FragmentPartnersListSubBinding
    private lateinit var partnersListAdapter: PartnersListAdapter

//    private var _binding: FragmentPartnersListSubBinding? = null
//    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()

//    private lateinit var recyclerView: RecyclerView
//    private lateinit var partnersAdapter: PartnersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        _binding = FragmentPartnersListSubBinding.inflate(inflater, container, false)
//        val view = binding.root
        binding = FragmentPartnersListSubBinding.inflate(inflater, container, false)
        return binding.root

//        recyclerView = binding.viewList
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        partnersAdapter = PartnersAdapter(requireActivity())
//        recyclerView.adapter = partnersAdapter
//        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sample data (replace this with your actual data source)
        petcareList.add(
            PetcareItem(
                "반려견 유치원",
                "왈독 강아지 유치원",
                "영업 중",
                "20:00에 영업 종료",
                "서울 도봉구 노해로 65길 7-12 7층",
                "https://png.pngtree.com/thumb_back/fh260/background/20230609/pngtree-three-puppies-with-their-mouths-open-are-posing-for-a-photo-image_2902292.jpg"
            )
        )
        // Sample data (replace this with your actual data source)
        petcareList.add(
            PetcareItem(
                "반려견 유치원",
                "골드퍼피 애견 유치원",
                "영업 종료",
                "08:00에 영업 시작",
                "서울 노원구 동일로 1014 3층",
                "https://image.utoimage.com/preview/cp872722/2022/12/202212008462_500.jpg"
            )
        )
        // Sample data (replace this with your actual data source)
        petcareList.add(
            PetcareItem(
                "반려견 유치원",
                "땡일리 강아지 유치원",
                "영업 중",
                "20:00에 영업 종료",
                "서울 성북구 월계로32길 24 302호",
                "https://img.freepik.com/premium-photo/cute-puppy-of-maltipoo-dog-posing-running-isolated-over-white-studio-background-playful-animal_756748-85193.jpg"
            )
        )
        // Sample data (replace this with your actual data source)
        petcareList.add(
            PetcareItem(
                "반려견 호텔",
                "마이단독 강아지 유치원",
                "영업 중",
                "20:00에 영업 종료",
                "서울 도봉구 노해로 273 3층, 마이단독",
                "https://images.mypetlife.co.kr/content/uploads/2023/07/20162542/%EC%A6%9D%EB%AA%85%EC%82%AC%EC%A7%84_%EB%A3%A8%ED%94%BC-1024x640.png"
            )
        )

        getItems()

        // Set up RecyclerView
        partnersListAdapter = PartnersListAdapter(petcareList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = partnersListAdapter
    }

    private fun getItems() {
        Log.d("ITM", "getItems() called")
        firestore.collection("TB_PETCARE")
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    val petcare_type = when (document.getString("petcare_type")) {
                        "k" -> "애견 유치원"
                        "h" -> "애견 호텔"
                        else -> "Unknown Type"
                    }
                    val petcare_name = document.getString("petcare_name")
                    val petcare_opening = document.getString("petcare_opening")
                    val petcare_closing = document.getString("petcare_closing")
                    val petcare_addr = document.getString("petcare_addr")
                    val petcare_img = document.getString("petcare_img")

                    val currentTime = System.currentTimeMillis()
                    val openingTime = parseTimeString(petcare_opening.toString())
                    val closingTime = parseTimeString(petcare_closing.toString())

                    var petcare_isopen = ""
                    var petcare_open_close = ""

                    if (currentTime in openingTime..closingTime) {
                        petcare_isopen = "영업 중"
                        petcare_open_close = "$petcare_closing 에 영업 종료"
                    } else {
                        petcare_isopen = "영업 종료"
                        petcare_open_close = "$petcare_opening 에 영업 시작"
                    }

                    petcareList.add(
                        PetcareItem(
                            petcare_type,
                            petcare_name,
                            petcare_isopen,
                            petcare_open_close,
                            petcare_addr,
                            petcare_img
                        )
                    )
                    Log.d("ITM", "$petcare_name Items() added")


                }
                // Set up RecyclerView
                partnersListAdapter = PartnersListAdapter(petcareList)
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.recyclerView.adapter = partnersListAdapter
            }


    }

    // 영업 시간 처리용 함수
    private fun parseTimeString(timeString: String): Long {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val parsedDate = dateFormat.parse(timeString)
        return parsedDate?.time ?: 0L
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PartnersListSubFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PartnersListSubFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}