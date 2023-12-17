package com.mobileprogramming.tadainu.partnersFeat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityChatListBinding

class ChatListActivity : AppCompatActivity() {
    private var mBinding: ActivityChatListBinding? = null
    private val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChatListBinding.inflate(layoutInflater)
        val view = binding.root

        binding.toolbar.toolbarTitle.text = "문의내역"

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