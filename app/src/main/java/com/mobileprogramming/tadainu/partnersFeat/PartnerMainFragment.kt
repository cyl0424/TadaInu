package com.mobileprogramming.tadainu.partnersFeat

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication
import com.mobileprogramming.tadainu.accountFeat.SignInActivity
import com.mobileprogramming.tadainu.databinding.FragmentPartnerMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class PartnerMainFragment : Fragment() {
    val db = Firebase.firestore

    private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
    private val storageRef: StorageReference = storage.reference

    private var mBinding: FragmentPartnerMainBinding? = null
    private val binding get() = mBinding!!

    private val user = GlobalApplication.prefs.getString("currentUser", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentPartnerMainBinding.inflate(inflater, container, false)  // 바인딩 초기화
        val view = binding.root

        if (user != "") {
            binding.profileView.visibility = View.VISIBLE
            binding.loginView.visibility = View.INVISIBLE

            val userCollection = db.collection("TB_USER")
            var petId = ""
            var petName = ""
            var petRelation = ""

            val userDocRef = userCollection.document(user)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstPetMap = document["user_pet"] as MutableList<Map<String, String>>
                        val firstPetKey = firstPetMap.first().keys.first()
                        petRelation = firstPetMap.first().values.first()
                        petId = firstPetKey
                        GlobalApplication.prefs.setString("petId", petId)
                        Log.d("MP", "DocumentSnapshot data: ${document.data}")
                    } else {
                        Log.d("MP", "No such document")
                    }

                    val petCollection = db.collection("TB_PET")

                    val docRef = petCollection.document(petId)
                    if(docRef != null){
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

                                    Log.d("MP", "DocumentSnapshot data: ${document.data}")
                                } else {
                                    Log.d("MP", "No such document")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d("MP", "get failed with ", exception)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("MP", "get failed with ", exception)
                }

            Log.d(ContentValues.TAG, "User is signed in")
        } else {
            binding.profileView.visibility = View.INVISIBLE
            binding.loginView.visibility = View.VISIBLE
        }

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


        binding.searchList.setOnClickListener {
            val intent = Intent(requireContext(), PartnersActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        binding.chatList.setOnClickListener {
            val intent = Intent(requireContext(), ChatListActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        binding.reservationList.setOnClickListener {
            val intent = Intent(requireContext(), ReservationListActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        binding.creditList.setOnClickListener {
            val intent = Intent(requireContext(), PurchaseListActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        binding.feedList.setOnClickListener {
            val intent = Intent(requireContext(), FeedListActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
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