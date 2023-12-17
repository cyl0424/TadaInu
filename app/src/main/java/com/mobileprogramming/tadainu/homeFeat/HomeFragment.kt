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
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.MainActivity
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.accountFeat.MoreInfoActivity
import com.mobileprogramming.tadainu.accountFeat.SignInActivity
import com.mobileprogramming.tadainu.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    val db = Firebase.firestore

    private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
    private val storageRef: StorageReference = storage.reference

    private var mBinding: FragmentHomeBinding? = null
    private val binding get() = mBinding!!

    private val user = prefs.getString("currentUser", "")
    private var petId = prefs.getString("petId", "")
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)  // 바인딩 초기화
        val view = binding.root

        Log.d("유저", user.toString())
        if (user != "") {
            binding.profileView.visibility = View.VISIBLE
            binding.loginView.visibility = View.INVISIBLE

            val userCollection = db.collection("TB_USER")
            var petName = ""
            var petRelation = ""

            val userDocRef = userCollection.document(user)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        if(petId == ""){
                            val firstPetMap = document["user_pet"] as MutableList<Map<String, String>>
                            val firstPetKey = firstPetMap.first().keys.first()
                            petRelation = firstPetMap.first().values.first()
                            petId = firstPetKey
                            prefs.setString("petId", petId)
                        }else{
                            val petList = document["user_pet"] as MutableList<Map<String, String>>
                            for (petMap in petList) {
                                val map_value = petMap[petId]
                                if (map_value != null){
                                    petRelation = map_value.toString()
                                    break
                                }

                            }
                        }

                        Log.d("MP", "DocumentSnapshot data: ${document.data}")
                    } else {
                        Log.d("MP", "No such document")
                    }

                    val petCollection = db.collection("TB_PET")

                    val docRef = petCollection.document(petId)

                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                petName = document["pet_name"].toString()
                                val together = document["pet_adopt_day"].toString()
                                binding.petDay.text = "${calculateDDay(together)}일"
                                binding.petName.text = petName
                                storageRef.child(document["pet_img"].toString()).downloadUrl.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        if(isAdded){
                                            Glide.with(binding.root.context)
                                                .load(task.result)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .apply(RequestOptions().circleCrop())
                                                .thumbnail(0.1f)
                                                .into(binding.profileImg)
                                        }
                                    } else {
                                    }
                                }

                                binding.profileImg.clipToOutline = true

                                Log.d("집", document["pet_at_home"].toString())

                                if(document["pet_at_home"] as Boolean){
                                    binding.kinderTitle.text= "반려견을 어딘가에 맡겨야할 때,"
                                    binding.kinderSubTitle.text = "$petName, $petRelation 다녀올개!"
                                }
                                else{
                                    binding.kinderImg.setImageResource(R.drawable.pet_home_false)
                                    binding.kinderTitle.text= "${petName}은(는) 지금 유치원에서 공부 중,"
                                    binding.kinderSubTitle.text = "$petRelation 안심하고 다녀오개!"
                                }
                                Log.d("MP", "DocumentSnapshot data: ${document.data}")
                            } else {
                                Log.d("MP", "No such document")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("MP", "get failed with ", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.d("MP", "get failed with ", exception)
                }

            Log.d(TAG, "User is signed in")
        } else {
            binding.profileView.visibility = View.INVISIBLE
            binding.loginView.visibility = View.VISIBLE
            binding.kinderSubTitle.text = "오늘도 안심하고 다녀올개!"
            Log.d(TAG, "User is signed out")
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

        binding.morePet.setOnClickListener {
            val intent = Intent(requireContext(), MoreInfoActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        binding.toolbar.menuBtn.setOnClickListener {
            val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.whole_layout)
            drawerLayout?.let {
                if (!it.isDrawerOpen(GravityCompat.END)) {
                    it.openDrawer(GravityCompat.END)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    fun calculateDDay(targetDateString : String): String{
        // 현재 날짜 가져오기
        val currentDate = Calendar.getInstance().time

        // 문자열을 Date 객체로 변환
        val targetDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val targetDate = targetDateFormat.parse(targetDateString)

        // 차이 계산벼
        val differenceInMillis = targetDate.time - currentDate.time
        val differenceInDays = (-differenceInMillis / (1000 * 60 * 60 * 24)).toString()

        return differenceInDays
    }
}
