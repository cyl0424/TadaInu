package com.mobileprogramming.tadainu.myPetFeat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityTrackWalkBinding

private lateinit var binding: ActivityTrackWalkBinding
private var isStart = false // 시작 상태 여부를 나타내는 변수

class TrackWalkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackWalkBinding.inflate(layoutInflater)
        setContentView(binding.root) // Use binding.root instead of R.layout.activity_track_walk

        binding.controlBtn.setOnClickListener {
            Log.d("ITM", "controlBtn Clicked")
            if (isStart) {
                // 현재 산책 중인 경우(Start Status)
                // 배경 색상 변경
                binding.controlBtn.backgroundTintList =
                    resources.getColorStateList(R.color.partners_clicked)

                // 텍스트 변경
                // 그만 상태로 바뀌는 것이므로 그만 상태에서 시작할 수 있도록 ?
                binding.startOrEnd.text = "시작"
                binding.startOrEnd.setTextColor(resources.getColor(R.color.white))

                isStart = false // 상태 변경
            } else {
                // 처음 시작 전과 그만 상태인 경우
                binding.controlBtn.backgroundTintList =
                    resources.getColorStateList(android.R.color.white)

                // 시작 상태로 바뀌는 것이므로 그만할 수 있도록 버튼 바꿔서 띄우기
                binding.startOrEnd.text = "그만"
                binding.startOrEnd.setTextColor(resources.getColor(R.color.base_black))

                isStart = true // 상태 변경
            }
        }
    }

    private fun startWalk() {
        TODO("Not yet implemented")
    }
}