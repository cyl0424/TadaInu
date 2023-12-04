package com.mobileprogramming.tadainu.settingFeat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.MainActivity
import com.mobileprogramming.tadainu.accountFeat.SignInActivity
import com.mobileprogramming.tadainu.databinding.FragmentSettingBinding
class SettingFragment : Fragment() {
    val db = Firebase.firestore

    private var mBinding: FragmentSettingBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSettingBinding.inflate(inflater, container, false)
        val view = binding.root

        val user = prefs.getString("currentUser", "")
        Log.d("유저", user.toString())
        if (user != "") {
            binding.completeBtnTxt.text = "로그아웃"
        } else {
            binding.completeBtnTxt.text = "로그인"
            binding.signOut.visibility = View.GONE
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        binding.completeBtn.setOnClickListener {
            if(binding.completeBtnTxt.text.equals("로그인")){
                val intent = Intent(requireContext(), SignInActivity::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }else{
                prefs.setString("currentUser", "")
                prefs.setString("petId", "")
                Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }
        }
    }

}