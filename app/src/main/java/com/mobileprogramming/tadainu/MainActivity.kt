package com.mobileprogramming.tadainu

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.databinding.ActivityMainBinding
import com.mobileprogramming.tadainu.databinding.RecyclerPetItemBinding
import com.mobileprogramming.tadainu.homeFeat.HomeFragment
import com.mobileprogramming.tadainu.myPetFeat.MyPetFragment
import com.mobileprogramming.tadainu.notiFeat.NotiFragment
import com.mobileprogramming.tadainu.settingFeat.SettingFragment
import com.mobileprogramming.tadainu.partnersFeat.PartnerMainFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val fragmentHome by lazy { HomeFragment() }
    private val fragmentMyPet by lazy { MyPetFragment() }
    private val fragmentPartners by lazy { PartnerMainFragment() }
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
       // prefs.setString("petId", "c66910b7-289c-4976-a18f-97ad10619b5f")
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, callback)

        val currentFragmentType = prefs.getString("currentFragmentType", null)
        val petChange = prefs.getString("petChange", "false")
        val currentFragment = when (currentFragmentType) {
            "home" -> fragmentHome
            "my_pet" -> fragmentMyPet
            "petcare" -> fragmentPartners
            else -> null
        }
        if (currentFragment != null && petChange!="false") {
            change(currentFragment)
            prefs.setString("petChange", "false")
        }else{
            change(fragmentHome)
        }

        initNavigationBar()

        val petListAdapter = PetListAdapter(this)
        binding.petRecycler.layoutManager = LinearLayoutManager(this)
        binding.petRecycler.adapter = petListAdapter

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

    @OptIn(DelicateCoroutinesApi::class)
    inner class PetListAdapter(private val context: Context):
        RecyclerView.Adapter<PetListAdapter.PetViewHolder>() {
        val userId = prefs.getString("currentUser", "")
        var petList = mutableListOf<MutableMap<String, String>>()

        val db = FirebaseFirestore.getInstance()
        private val storage: FirebaseStorage =
            FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
        private val storageRef: StorageReference = storage.reference

        init {
            if(userId != ""){
                GlobalScope.launch {
                    db.collection("TB_USER").document(userId)
                        .get()
                        .addOnSuccessListener { doc ->

                            val petMapList =
                                doc["user_pet"] as MutableList<MutableMap<String, String>>
                            for (pet in petMapList) {
                                val petId = pet.keys.first()
                                val petCollection = db.collection("TB_PET")
                                val petDocRef = petCollection.document(petId)
                                if (petDocRef != null) {
                                    petDocRef.get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val petMap = mutableMapOf<String, String>()
                                                petMap["pet_name"] = document["pet_name"].toString()
                                                petMap["pet_img"] = document["pet_img"].toString()
                                                petMap["pet_id"] = document["pet_id"].toString()

                                                petMap["pet_day"] = "${calculateDDay(document["pet_adopt_day"].toString())}일"
                                                petList.add(petMap)
                                                notifyDataSetChanged()
                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PetListAdapter.PetViewHolder {
            val binding = RecyclerPetItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return PetViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PetListAdapter.PetViewHolder, position: Int) {
            val petItem = petList[position]
            holder.bind(petItem)
        }

        override fun getItemCount(): Int {
            return petList.size
        }

        inner class PetViewHolder(private val binding: RecyclerPetItemBinding) :
            RecyclerView.ViewHolder(binding.root) {


            fun bind(petItem: MutableMap<String, String>) {
                binding.apply {
                    storageRef.child(petItem["pet_img"].toString()).downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Glide.with(itemView)
                                .load(task.result)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .apply(RequestOptions().circleCrop())
                                .override(100, 100)
                                .thumbnail(0.1f)
                                .into(binding.profileImg)

                        }
                    }
                    binding.petName.text = petItem["pet_name"].toString()
                    binding.petDay.text = petItem["pet_day"]
                }

                binding.root.setOnClickListener {
                    prefs.setString("petId", petItem["pet_id"].toString())
                    prefs.setString("petChange", "true")

                    val currentFragmentType = when (this@MainActivity.binding.bottomNavi.selectedItemId) {
                        R.id.home_menu -> {
                            prefs.setString("currentFragmentType", "home")
                        }
                        R.id.my_pet_menu -> {
                            prefs.setString("currentFragmentType", "my_pet")
                        }
                        R.id.petcare_menu -> {
                            prefs.setString("currentFragmentType", "petcare")
                        }
                        else -> null
                    }

                    if (currentFragmentType != null) {
                        Log.d("프래그먼트", "호출 ${currentFragmentType.toString()}")
                        this@MainActivity.recreate()
                    }

                    val drawerLayout = this@MainActivity.findViewById<DrawerLayout>(R.id.whole_layout)
                    drawerLayout?.let {
                        if (it.isDrawerOpen(GravityCompat.END)) {
                            it.closeDrawer(GravityCompat.END)
                        }
                    }

                    Log.d("펫아이디", prefs.getString("petId", ""))
                }
            }
        }
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

