package com.mobileprogramming.tadainu.partnersFeat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobileprogramming.tadainu.partnersFeat.PartnersListSubFragment
import com.mobileprogramming.tadainu.partnersFeat.PartnersMapSubFragment

// For TabLayout
class PartnersAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    //Map, List 두 개의 하위 Fragment
    override fun getItemCount(): Int = 2

    // position에 따라 상응하는 Fragment 반환
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PartnersMapSubFragment()
            1 -> PartnersListSubFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}