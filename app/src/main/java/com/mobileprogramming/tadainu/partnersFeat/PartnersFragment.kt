package com.mobileprogramming.tadainu.partnersFeat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.FragmentPartnersBinding
import com.mobileprogramming.tadainu.partnersFeat.adapter.PartnersAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PartnersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PartnersFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentPartnersBinding
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
        binding = FragmentPartnersBinding.inflate(inflater, container, false)
        // PartnersFragment 뷰 반환한 것
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val adapter = PartnersAdapter(requireActivity())

        viewPager.adapter = adapter
        // TabLayout과 ViewPager연결
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // 각 탭마다 텍스트 지정
            tab.text = when (position) {
                0 -> "지도"
                1 -> "리스트"
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PartnersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}