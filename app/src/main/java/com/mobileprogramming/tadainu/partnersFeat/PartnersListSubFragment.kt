package com.mobileprogramming.tadainu.partnersFeat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.mobileprogramming.tadainu.databinding.FragmentPartnersListSubBinding
import com.mobileprogramming.tadainu.model.PetcareItem
import com.mobileprogramming.tadainu.partnersFeat.adapter.PartnersListAdapter
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

    private val petcareList = mutableListOf<PetcareItem>()

    private lateinit var binding: FragmentPartnersListSubBinding
    private lateinit var partnersListAdapter: PartnersListAdapter

    private val firestore = FirebaseFirestore.getInstance()

    private var isKindergartenSelected = false
    private var isHotelSelected = false

    private lateinit var searchView: SearchView
    private lateinit var linearSearchHistoryView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun initSearchView() {
        binding.search.isSubmitButtonEnabled = false
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                updateFilter()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    resetFilter()
                }
                return true
            }
        })

        binding.search.setOnCloseListener {
            resetFilter()
            false
        }
    }

    private fun resetFilter() {
        // Update the adapter with the original list (no filtering)
        partnersListAdapter = PartnersListAdapter(petcareList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = partnersListAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPartnersListSubBinding.inflate(inflater, container, false)

        return binding.root
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

        binding.buttonK.setOnCheckedChangeListener { _, isChecked ->
            isKindergartenSelected = isChecked
            updateFilter()
        }

        binding.buttonH.setOnCheckedChangeListener { _, isChecked ->
            isHotelSelected = isChecked
            updateFilter()
        }

        initSearchView()
    }


    private fun updateFilter() {
        val query = when {
            isKindergartenSelected && isHotelSelected -> "둘다"
            isKindergartenSelected -> "반려견 유치원"
            isHotelSelected -> "애견 호텔"
            else -> null
        }

        val petcareFilteredByType = filterResultsByType(query)
        val petcareFilteredBySearch = filterResultsBySearch(binding.search.query.toString())

        val finalFilteredList = if (query != null) {
            petcareFilteredByType.filter { it in petcareFilteredBySearch }
        } else {
            petcareFilteredBySearch
        }

        partnersListAdapter = PartnersListAdapter(finalFilteredList as MutableList<PetcareItem>)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = partnersListAdapter
    }

    private fun filterResultsByType(query: String?): List<PetcareItem> {
        return when (query) {
            "반려견 유치원" -> petcareList.filter { it.petcare_type == "반려견 유치원" }
            "애견 호텔" -> petcareList.filter { it.petcare_type == "애견 호텔" }
            "둘다" -> petcareList.filter { it.petcare_type == "반려견 유치원" || it.petcare_type == "애견 호텔" }
            else -> petcareList
        }
    }

    private fun filterResultsBySearch(query: String): List<PetcareItem> {
        return if (query.isNotBlank()) {
            val result = petcareList.filter {
                it.petcare_name?.contains(query, ignoreCase = true) == true
            }
            if (result.isEmpty()) {
                showToast("Please enter a valid query")
            }
            result
        } else {
            petcareList
        }
    }

    private fun filterResults(query: String?) {
        var filteredList = petcareList
        Log.d("ITM", "filterResults Start: $query")
        filteredList = when (query) {
            "반려견 유치원" -> filteredList.filter { it.petcare_type == "반려견 유치원" } as MutableList<PetcareItem>
            "애견 호텔" -> filteredList.filter { it.petcare_type == "애견 호텔" } as MutableList<PetcareItem>
            "둘다" -> filteredList.filter { it.petcare_type == "반려견 유치원" || it.petcare_type == "애견 호텔" } as MutableList<PetcareItem>
            else -> petcareList
        }

        // Update the adapter with the filtered list
        partnersListAdapter = PartnersListAdapter(filteredList)
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
                        "k" -> "반려견 유치원"
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
//                    Log.d("ITM", "$petcare_name Items() added")

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