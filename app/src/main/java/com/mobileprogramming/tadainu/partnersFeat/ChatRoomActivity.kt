package com.mobileprogramming.tadainu.partnersFeat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mobileprogramming.tadainu.R

class ChatRoomActivity : AppCompatActivity() {
    var chatRoomId = ""

    companion object {

        var In_Room = false
        var In_Room_postId = ""

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
    }

    override fun onStart() {
        super.onStart()
        In_Room = true
        In_Room_postId = chatRoomId.toString()
    }
}