package com.mobileprogramming.tadainu.partnersFeat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityReservationListBinding

class ReservationListActivity : AppCompatActivity() {
    private var mBinding: ActivityReservationListBinding? = null
    private val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityReservationListBinding.inflate(layoutInflater)
        val view = binding.root
        binding.toolbar.toolbarTitle.text = "예약내역"

        binding.toolbar.backBtn.setOnClickListener {
            onBackPressed()
            overridePendingTransition(0, 0)
        }

        setContentView(view)
    }
}