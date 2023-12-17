package com.mobileprogramming.tadainu.partnersFeat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityChatListBinding
import com.mobileprogramming.tadainu.databinding.ActivityChatRoomBinding

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

        binding.example.chatTitle.setOnClickListener {
            val intent = Intent(this, ChatRoomActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.example.chatContent.text = "안녕하세요 보호자님!"
        binding.example.chatTitle.text = "호텔 19"
        binding.example.chatTime.text = "12월 16일"

        setContentView(view)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }


}