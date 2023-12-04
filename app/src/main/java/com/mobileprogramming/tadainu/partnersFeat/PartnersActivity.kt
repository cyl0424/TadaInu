package com.mobileprogramming.tadainu.partnersFeat

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.mobileprogramming.tadainu.databinding.ActivityMainBinding
import com.mobileprogramming.tadainu.databinding.ActivityPartnersBinding
import com.mobileprogramming.tadainu.partnersFeat.adapter.PartnersAdapter


class PartnersActivity : AppCompatActivity() {
    private var mBinding: ActivityPartnersBinding? = null
    private val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityPartnersBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbar.toolbarTitle.text = "유치원/호텔 조회하기"

        binding.toolbar.backBtn.setOnClickListener {
            finish()
        }

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val adapter = PartnersAdapter(this)

        viewPager.adapter = adapter

        // 스와이프로 탭 전환 막음.(개인적인 이용 불편)
        viewPager.isUserInputEnabled = false

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

}