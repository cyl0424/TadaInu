package com.mobileprogramming.tadainu.partnersFeat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityChatRoomBinding
import com.mobileprogramming.tadainu.databinding.ActivityMainBinding

class ChatRoomActivity : AppCompatActivity() {
    var chatRoomId = ""

    companion object {

        var In_Room = false
        var In_Room_postId = ""

    }
    private var mBinding: ActivityChatRoomBinding? = null
    private val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityChatRoomBinding.inflate(layoutInflater)
        // prefs.setString("petId", "c66910b7-289c-4976-a18f-97ad10619b5f")
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbar.toolbarTitle.text = "호텔 19"
        binding.paymentsBtn.setOnClickListener {
            val intent = Intent(this, PaymentsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

    }

    override fun onStart() {
        super.onStart()
        In_Room = true
        In_Room_postId = chatRoomId.toString()
    }
}