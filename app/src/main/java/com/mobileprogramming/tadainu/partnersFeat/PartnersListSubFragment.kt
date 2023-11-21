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

        getItems()

//        // Sample data (replace this with your actual data source)
//        petcareList.add(
//            PetcareItem(
//                "Type 1",
//                "Partner 1",
//                "9:00 AM",
//                "6:00 PM",
//                "123 Main St",
//                "https://example.com/partner1.jpg"
//            )
//        )

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
                    val petcare_type = document.getString("petcare_type")
                    val petcare_name = document.getString("petcare_name")
                    val petcare_opening = document.getString("petcare_opening")
                    val petcare_closing = document.getString("petcare_closing")
                    val petcare_addr = document.getString("petcare_addr")
                    val petcare_img = document.getString("petcare_img")

                    petcareList.add(
                        PetcareItem(
                            petcare_type,
                            petcare_name,
                            petcare_opening,
                            petcare_closing,
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