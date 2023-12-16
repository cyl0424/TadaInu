package com.mobileprogramming.tadainu.partnersFeat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityPurchaseListBinding

class PurchaseListActivity : AppCompatActivity() {
    private var mBinding: ActivityPurchaseListBinding? = null
    private val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPurchaseListBinding.inflate(layoutInflater)
        val view = binding.root

        binding.toolbar.toolbarTitle.text = "결제내역"

        binding.toolbar.backBtn.setOnClickListener {
            onBackPressed()
            overridePendingTransition(0, 0)
        }

        setContentView(view)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }
}