package com.mobileprogramming.tadainu.myPetFeat

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.DialogAddShotBinding
import com.mobileprogramming.tadainu.databinding.FragmentMyPetBinding
import com.mobileprogramming.tadainu.myPetFeat.adapter.ShotListAdapter
import com.mobileprogramming.tadainu.myPetFeat.model.ShotItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val PET_ID = "4Jipcx2xHXmvcKNVc6cO"
private const val PET_STORAGE_PATH = "pet/profile/20231110145312.png"

class MyPetFragment : Fragment() {
    private var _binding: FragmentMyPetBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val shotList = mutableListOf<ShotItem>()
    private lateinit var shotAdapter: ShotListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadPetDataFromFirestore()
        loadPetImageFromStorage()
        setupShotList()
    }

    private fun setupUI() {
        binding.mypetToolbar.toolbarTitle.text = "마이펫"
        binding.mypetToolbar.backBtn.visibility = View.INVISIBLE

        binding.mypetBeautyBackground.setOnClickListener {
            showDatePicker(requireContext(), "beauty", null)
        }

        binding.mypetHealthBackground.setOnClickListener {
            showDatePicker(requireContext(), "health",null)
        }

        binding.mypetShotBackground.setOnClickListener {
            showAddShotDialog()
        }

        binding.mypetLocationBackground.setOnClickListener {
            val intent = Intent(requireContext(), TrackLocation::class.java)
            startActivity(intent)
        }
    }

    private fun loadPetDataFromFirestore() {
        db.collection("TB_PET").document(PET_ID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val petName = document.getString("pet_name")
                        petName?.let {
                            Log.d("ITM", "Pet Name: $it")
                            binding.mypetDogName.text = it
                        }
                    } else {
                        Log.d("ITM", "Document does not exist")
                    }
                } else {
                    Log.e("ITM", "Error getting document", task.exception)
                }
            }
    }

    private fun loadPetImageFromStorage() {
        val storageReference = storage.reference.child(PET_STORAGE_PATH)

        storageReference.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
                    .circleCrop()
                    .into(binding.mypetDogPicture)
            } else {
                Log.e("ITM", "Error downloading image", task.exception)
            }
        }
    }

    private fun setupShotList() {
        shotAdapter = ShotListAdapter(requireContext(), shotList as ArrayList<ShotItem>)
        binding.shotList.adapter = shotAdapter

        db.collection("TB_MYPET").document(PET_ID)
            .addSnapshotListener { snapshot, e ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (e != null) {
                        Log.e("ITM", "Listen failed.", e)
                        return@launch
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val beautyDate = snapshot.getTimestamp("beauty")
                        val beautyLastDate = snapshot.getTimestamp("beauty_last")
                        val healthDate = snapshot.getTimestamp("health")
                        val healthLastDate = snapshot.getTimestamp("health_last")

                        updateUIWithPetData(beautyDate, beautyLastDate, healthDate, healthLastDate)

                        val shotCount = snapshot.getLong("shot_count") ?: 0
                        shotList.clear()
                        for (i in 1..shotCount) {
                            val shotField = "shot$i"
                            val shotTimestamp = snapshot.getTimestamp(shotField)
                            Log.d("ITM", "$shotField, $shotTimestamp")
                            if (shotTimestamp != null) {
                                val shotDate =
                                    SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(
                                        shotTimestamp.toDate()
                                    )
                                shotList.add(ShotItem("Shot $i", shotDate))
                            }
                        }
                        shotAdapter.notifyDataSetChanged()
                    } else {
                        Log.d("ITM", "Current data: null")
                    }
                }
            }
    }

    private fun updateUIWithPetData(
        beautyDate: Timestamp?,
        beautyLastDate: Timestamp?,
        healthDate: Timestamp?,
        healthLastDate: Timestamp?
    ) {
        val currentDate = Date()

        updateUIData("beauty", beautyDate, beautyLastDate, currentDate)
        updateUIData("health", healthDate, healthLastDate, currentDate)
    }

    private fun updateUIData(
        field: String,
        date: Timestamp?,
        lastDate: Timestamp?,
        currentDate: Date
    ) {
        val dateMillis = date?.toDate()?.time ?: 0
        val differenceInMillis = dateMillis - currentDate.time
        val differenceInDays = (differenceInMillis / (24 * 60 * 60 * 1000)) + 1
        Log.d("ITM", "Days remaining ($field): $differenceInDays")

        val leftTextView = when (field) {
            "beauty" -> binding.mypetBeautyLeft
            "health" -> binding.mypetHealthLeft
            else -> null
        }
        leftTextView?.text = "$differenceInDays"

        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val formattedDate = sdf.format(lastDate?.toDate())
        when (field) {
            "beauty" -> binding.mypetBeautyDate.text = formattedDate
            "health" -> binding.mypetHealthDate.text = formattedDate
        }
    }

    private fun updateFirebaseWithDate(field: String, timestamp: Long) {
        val documentRef = db.collection("TB_MYPET").document(PET_ID)

        val date = Date(timestamp)
        val firebaseTimestamp = Timestamp(date)

        Log.d("ITM", "Updating Firestore with timestamp: $firebaseTimestamp")

        db.runTransaction { transaction ->
            val documentSnapshot = transaction.get(documentRef)
            val lastValue = documentSnapshot.getTimestamp(field)

            Log.d("ITM", "Last $field value: $lastValue")

            transaction.update(documentRef, "${field}_last", lastValue)
            transaction.update(documentRef, field, firebaseTimestamp)
            null
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Transaction failed: ${exception.message}")
        }
    }

    private fun showDatePicker(context: Context, field: String, shotDate: TextView?) {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                // Format the selected date
                val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                val formattedDate = sdf.format(selectedDate.time)

                // Set the formatted date to the TextView
                shotDate?.text = formattedDate

                val timestamp = selectedDate.timeInMillis
                updateFirebaseWithDate(field, timestamp)
            },
            year, month, day
        )

        datePickerDialog.datePicker.minDate = currentDate.timeInMillis
        datePickerDialog.show()
    }

    private fun showAddShotDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_shot, null)
        val dialogBinding = DialogAddShotBinding.bind(dialogView)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val spinnerItems = resources.getStringArray(R.array.my_array)
        val spinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.shotSpinner.adapter = spinnerAdapter

        dialogBinding.shotCalenderIcon.setOnClickListener {
            showDatePicker(requireContext(), "",dialogBinding.shotDate)
        }

        dialogBinding.shotSave.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyPetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
