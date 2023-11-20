package com.mobileprogramming.tadainu.partnersFeat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.FragmentMyPetBinding
import com.mobileprogramming.tadainu.model.PetcareItem
import com.mobileprogramming.tadainu.partnersFeat.adapter.PartnersAdapter
import kotlinx.coroutines.Dispatchers
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

    private var _binding: FragmentMyPetBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var recyclerView: RecyclerView
    private lateinit var partnersAdapter: PartnersAdapter


    private fun refreshUI() {
        db.collection("TB_PETCARE")
            .addSnapshotListener { snapshot, e ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (e != null) {
                        Log.e("ITM", "Listen failed.", e)
                        return@launch
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        // Process the data from the snapshot
                        val petcareList = mutableListOf<PetcareItem>()

                        for (document in snapshot.documents) {
                            val petcareType = document.getString("petcare_type") ?: ""
                            val petcareName = document.getString("petcare_name") ?: ""
                            val petcareOpening = document.getString("petcare_opening") ?: ""
                            val petcareClosing = document.getString("petcare_closing") ?: ""
                            val petcareAddr = document.getString("petcare_addr") ?: ""
                            val petcareImg = document.getString("pecare_img") ?: ""

                            val petcareItem = PetcareItem(
                                petcareType,
                                petcareName,
                                petcareOpening,
                                petcareClosing,
                                petcareAddr,
                                petcareImg
                            )

                            petcareList.add(petcareItem)
                        }

                        // Now you have a list of PetcareItem objects, you can update your UI or adapter
                        // For example, if you have a RecyclerView with an adapter:
                        partnersAdapter.updateDataInListFragment(petcareList)
                    } else {
                        Log.d("ITM", "Snapshot is null or empty.")
                    }
                }
            }
    }

    // Function to update data in the PartnersAdapter
    fun updateData(data: List<PetcareItem>) {
        partnersAdapter.updateDataInListFragment(data)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_partners_list_sub, container, false)

        // Initialize RecyclerView and set its layout manager
        recyclerView = view.findViewById(R.id.view_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the PartnersAdapter
        partnersAdapter = PartnersAdapter(requireContext() as FragmentActivity)

        // Set the adapter to the RecyclerView
        recyclerView.adapter = partnersAdapter

        return view
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