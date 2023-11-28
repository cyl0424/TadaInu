package com.mobileprogramming.tadainu.homeFeat

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.MainActivity
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.accountFeat.SignInActivity
import com.mobileprogramming.tadainu.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    val db = Firebase.firestore

    private var mBinding: FragmentHomeBinding? = null
    private val binding get() = mBinding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)  // 바인딩 초기화
        val view = binding.root

        val user = prefs.getString("currentUser", "")
        Log.d("유저", user.toString())
        if (user != "") {
            binding.profileView.visibility = View.VISIBLE
            binding.loginView.visibility = View.INVISIBLE
            Log.d(TAG, "User is signed in")
        } else {
            binding.profileView.visibility = View.INVISIBLE
            binding.loginView.visibility = View.VISIBLE
            Log.d(TAG, "User is signed out")
        }

        val petCollection = db.collection("TB_PET")
        val petId = "4Jipcx2xHXmvcKNVc6cO"

        val docRef = petCollection.document(petId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    binding.petName.text = "${document["pet_name"]}"
                    Log.d("MP", "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d("MP", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MP", "get failed with ", exception)
            }

        val tabLayout = binding.tabLayout
        val photoCalendarView = binding.photoCalendar
        val diaryView = binding.homeDiary

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        photoCalendarView.visibility = View.VISIBLE
                        diaryView.visibility = View.GONE
                    }
                    1 -> {
                        photoCalendarView.visibility = View.GONE
                        diaryView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // 이미 선택된 탭이 다시 선택됐을 때의 동작
            }
        })

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        binding.loginView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 버튼이 눌렸을 때
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(200).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 버튼이 눌리지 않았을 때
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }
            }
            false
        }

        binding.loginView.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        binding.kinderBtn.setOnClickListener {
            (activity as MainActivity).apply {
                selectBottomNavigationItem(R.id.petcare_menu)
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}
