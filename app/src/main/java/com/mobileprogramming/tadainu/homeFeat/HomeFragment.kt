package com.mobileprogramming.tadainu.homeFeat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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

        val testCollection = db.collection("test")

        val docRef = testCollection.document("testDocument")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    binding.firebaseTest.text = "firebase data: ${document["text"]}"
                    Log.d("MP", "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d("MP", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MP", "get failed with ", exception)
            }

        binding.toolbar.phoneBtn.visibility = View.VISIBLE

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}
