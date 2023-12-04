package com.mobileprogramming.tadainu

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.mobileprogramming.tadainu.partnersFeat.PartnersFragment
import com.mobileprogramming.tadainu.databinding.ActivityMainBinding
import com.mobileprogramming.tadainu.homeFeat.HomeFragment
import com.mobileprogramming.tadainu.myPetFeat.MyPetFragment
import com.mobileprogramming.tadainu.notiFeat.NotiFragment
import com.mobileprogramming.tadainu.settingFeat.SettingFragment

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.common.util.Utility

class MainActivity : AppCompatActivity() {
    private val fragmentHome by lazy { HomeFragment() }
    private val fragmentMyPet by lazy { MyPetFragment() }
    private val fragmentPartners by lazy { PartnersFragment() }
    private val fragmentNoti by lazy { NotiFragment() }
    private val fragmentSetting by lazy { SettingFragment() }

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    private var lastTimeBackPressed: Long = 0

    // control backPressed action
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - lastTimeBackPressed >= 1500) {
                lastTimeBackPressed = System.currentTimeMillis()
                Toast.makeText(this@MainActivity, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else {
                finishAffinity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, callback)

        initNavigationBar()

    }

    private fun initNavigationBar() {
        binding.bottomNavi.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_menu -> {
                    try {
                        change(fragmentHome)
                    } catch (e: IllegalStateException) {
                        Handler().postDelayed({
                            change(fragmentHome)
                        }, 500L)
                    }
                }
                R.id.my_pet_menu -> {
                    try {
                        change(fragmentMyPet)
                    } catch (e: IllegalStateException) {
                        Handler().postDelayed({
                            change(fragmentMyPet)
                        }, 500L)
                    }
                }
                R.id.petcare_menu -> {
                    try {
                        change(fragmentPartners)
                    } catch (e: IllegalStateException) {
                        Handler().postDelayed({
                            change(fragmentPartners)
                        }, 500L)
                    }
                }
                R.id.noti_menu -> {
                    try {
                        change(fragmentNoti)
                    } catch (e: IllegalStateException) {
                        Handler().postDelayed({
                            change(fragmentNoti)
                        }, 500L)
                    }
                }
                R.id.setting_menu -> {
                    try {
                        change(fragmentSetting)
                    } catch (e: IllegalStateException) {
                        Handler().postDelayed({
                            change(fragmentSetting)
                        }, 500L)
                    }
                }
            }
            true
        }
        binding.bottomNavi.selectedItemId = R.id.home_menu
    }


    fun change(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentArea, fragment)
            .commitNow()
    }

    fun selectBottomNavigationItem(itemId: Int) {
        binding.bottomNavi.selectedItemId = itemId
    }
}