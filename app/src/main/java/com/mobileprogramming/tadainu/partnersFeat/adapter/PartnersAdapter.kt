package com.mobileprogramming.tadainu.partnersFeat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobileprogramming.tadainu.partnersFeat.PartnersListSubFragment
import com.mobileprogramming.tadainu.partnersFeat.PartnersMapSubFragment

class PartnersAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2 // Assuming you have two tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PartnersMapSubFragment()
            1 -> PartnersListSubFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}