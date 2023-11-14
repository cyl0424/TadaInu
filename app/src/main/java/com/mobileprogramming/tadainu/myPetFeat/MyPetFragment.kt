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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.DialogAddShotBinding
import com.mobileprogramming.tadainu.databinding.FragmentMyPetBinding
import com.mobileprogramming.tadainu.myPetFeat.adapter.ShotListAdapter
import com.mobileprogramming.tadainu.myPetFeat.model.ShotItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Integer.max
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

        loadPetDataFromFirestore()
        loadPetImageFromStorage()
        clickEventHandler()
        setupShotList()
    }

    private fun clickEventHandler() {
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
    private fun setupShotList() {
        // Set up a snapshot listener to observe changes in the TB_MYPET document
        db.collection("TB_MYPET").document(PET_ID)
            .addSnapshotListener { snapshot, e ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (e != null) {
                        Log.e("ITM", "Listen failed.", e)
                        return@launch
                    }

                    if (snapshot != null && snapshot.exists()) {
                        // Fetch beauty and health dates
                        val beautyDate = snapshot.getTimestamp("beauty")
                        val beautyLastDate = snapshot.getTimestamp("beauty_last")
                        val healthDate = snapshot.getTimestamp("health")
                        val healthLastDate = snapshot.getTimestamp("health_last")
                        // Update UI with beauty and health data
                        updateUIWithPetData(beautyDate, beautyLastDate, healthDate, healthLastDate)
                        // Update UI with shotItem data
                        loadShotDataFromSnapshot(snapshot)
                    } else {
                        Log.d("ITM", "Current data: null")
                    }
                }
            }
    }
    private fun loadShotDataFromSnapshot(snapshot: DocumentSnapshot) {
        // Fetch shot data from SUBTB_VACCINE subcollection
        db.collection("TB_MYPET").document(PET_ID)
            .collection("SUBTB_VACCINE")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("ITM", "Error getting documents: ", error)
                    return@addSnapshotListener
                }

                // Clear the existing shotList
                shotList.clear()

                // Fetch the data from each document in the SUBTB_VACCINE subcollection
                for (document in value!!) {
                    val shotName = document.getString("shot_name") ?: ""
                    val shotNum = document.getString("shot_num") ?: ""
                    val shotDate = document.getString("shot_date") ?: ""

                    // Add the data to the shotList
                    shotList.add(ShotItem(shotName, shotNum, shotDate))
                }

                // Set up the adapter if not initialized
                if (!::shotAdapter.isInitialized) {
                    shotAdapter = ShotListAdapter(requireContext(), shotList as ArrayList<ShotItem>)

                    // Set the layout manager to horizontal
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.shotList.layoutManager = layoutManager

                    // Set the adapter to the RecyclerView
                    binding.shotList.adapter = shotAdapter
                }

                // Notify the adapter that the data set has changed
                shotAdapter.notifyDataSetChanged()
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
        val differenceInDays = max(0, ((dateMillis - currentDate.time) / (24 * 60 * 60 * 1000)).toInt())

        Log.d("ITM", "Days remaining ($field): $differenceInDays")

        // Update the corresponding TextView with the calculated difference in days
        when (field) {
            "beauty" -> binding.mypetBeautyLeft.text = differenceInDays.toString()
            "health" -> binding.mypetHealthLeft.text = differenceInDays.toString()
        }

        // Format the last date and update the corresponding TextView
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
    private fun saveShotDataToFirebase(shotItem: ShotItem) {
        // Get reference to the pet document using the pet_id
        val petRef = db.collection("TB_MYPET").document(PET_ID)
        val vaccineCollection = petRef.collection("SUBTB_VACCINE")
        Log.d("ITM","${shotItem.shotName},${shotItem.shotNum},${shotItem.date}")
        // Create a new document for each shot in the SUBTB_VACCINE subcollection
        vaccineCollection.add(
            mapOf(
                "shot_name" to shotItem.shotName,
                "shot_num" to shotItem.shotNum,
                "shot_date" to shotItem.date
            )
        )
            .addOnSuccessListener { documentReference ->
                Log.d("Firebase", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error adding document", exception)
            }
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
        dialogBinding.shotSpinnerInput.adapter = spinnerAdapter

        dialogBinding.shotCalenderIcon.setOnClickListener {
            showDatePicker(requireContext(), "", dialogBinding.shotDateInput)
        }

        dialogBinding.shotSave.setOnClickListener {
            // Create a ShotItem with the selected data
            val selectedShotName = dialogBinding.shotSpinnerInput.selectedItem as String
            val shotNum = dialogBinding.shotNumInput.text.toString()
            val shotDate = dialogBinding.shotDateInput.text.toString()

            val shotItem = ShotItem(selectedShotName, shotNum, shotDate)
            // Save the shot data to Firebase
            saveShotDataToFirebase(shotItem)
            dialog.dismiss()
        }
        dialog.show()
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