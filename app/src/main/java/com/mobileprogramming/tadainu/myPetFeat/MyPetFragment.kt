package com.mobileprogramming.tadainu.myPetFeat

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.DialogAddShotBinding
import com.mobileprogramming.tadainu.databinding.DialogUpdateBhBinding
import com.mobileprogramming.tadainu.databinding.FragmentMyPetBinding
import com.mobileprogramming.tadainu.myPetFeat.adapter.ShotListAdapter
import com.mobileprogramming.tadainu.model.ShotItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class MyPetFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentMyPetBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
    private val storageRef: StorageReference = storage.reference

    private val shotList = mutableListOf<ShotItem>()
    private lateinit var shotAdapter: ShotListAdapter

    // 센서
    private lateinit var sensorManager: SensorManager
    private var mLight: Sensor? = null
    private var shakeDialog: AlertDialog? = null

    val petId = prefs.getString("petId", "")
    var petImg = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPetBinding.inflate(inflater, container, false)

        if(petId != ""){
            val petCollection = db.collection("TB_PET")
            val docRef = petCollection.document(petId)
            if(docRef != null) {
                docRef.get()
                    .addOnSuccessListener { document ->
                        petImg = document["pet_img"].toString()
                    }.addOnFailureListener { exception ->
                        Log.d("MP", "get failed with ", exception)
                    }
            }
            petInfoUpdate()
        }


        sensorManager = binding.root.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickEventHandler()
        if(petId != ""){
            refreshUI()

            // Recycler View 계속 띄워놓기
            shotAdapter = ShotListAdapter(requireContext(), shotList as ArrayList<ShotItem>)
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.shotList.layoutManager = layoutManager
            binding.shotList.adapter = shotAdapter

            // 버튼을 눌렀을 때만 센서 작동
            binding.sensorBtn.setOnClickListener {
                showShakeDialog()
                Log.d("ITM", "흔들어서 초대하기를 클릭하여 센서를 초기화합니다.")
                sensorManager = binding.root.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                // 장치에 어떤 센서 있는지 확인
                val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
                Log.d("ITM", "$deviceSensors")

                // 자이로스코프 센서 초기화
                val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                sensor?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                    Log.d("ITM", "자이로스코프 센서 초기화 됨")
                } ?: Log.e("ITM", "자이로스코프 센서 초기화 안 됨")

                // 가속도계 센서 초기화
                mLight = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                mLight?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                    Log.d("ITM", "가속도계 센서 초기화 됨")
                } ?: Log.e("ITM", "가속도계 센서 초기화 안 됨")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // 센서 반응 처리
        // 자이로스코프 센서 처리
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val angularSpeed = sqrt(x * x + y * y + z * z)

            val threshold = 10.0f  // 자이로스코프 센서 임계값 설정

            if (angularSpeed > threshold) {
                Log.d("ITM", "자이로스코프 센서 감지")
                // JSON형식으로 넘겨주기
                val jsonData = "{\"pet_id\":\"4Jipcx2xHXmvcKNVc6cO\"}"
                // ShakeDialog 닫은 후에 QrDialog 띄움
                shakeDialog?.dismiss()
                showQRCodeDialog(jsonData)
            }
        }

        // 가속도계 센서 처리
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = sqrt(x * x + y * y + z * z)

            val threshold = 40.0f // 가속도계 센서 임계값 설정

            if (acceleration > threshold) {
                Log.d("ITM", "가속도계 센서 감지")
                // JSON형식으로 넘겨주기
                val jsonData = "{\"pet_id\":\"4Jipcx2xHXmvcKNVc6cO\"}"
                // ShakeDialog 닫은 후에 QrDialog 띄움
                shakeDialog?.dismiss()
                showQRCodeDialog(jsonData)
            }
        }
    }

    // 디바이스 흔들었을 때 뜨는 다이얼로그
    private fun showShakeDialog() {

        // Your existing code to show the Shake dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setMessage("휴대폰을 흔들어주세요!")
            .setPositiveButton("돌아가기") { _, _ ->
                // Unregister sensor listener when the Shake dialog is dismissed
                sensorManager.unregisterListener(this)
                shakeDialog = null // Set shakeDialog to null after dismissing the dialog
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }


    // 흔들었을 때 QR띄우기
    private fun generateQRCode(data: String): Bitmap {
        val writer = QRCodeWriter()
        try {
            val bitMatrix: BitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            return bmp
        } catch (e: WriterException) {
            e.printStackTrace()
            throw e
        }
    }
    private fun showQRCodeDialog(qrData: String) {
        // Generate QR code bitmap
        val qrCodeBitmap = generateQRCode(qrData)

        // Create an ImageView to display the QR code
        val imageView = ImageView(requireContext())
        imageView.setImageBitmap(qrCodeBitmap)

        // Create a dialog with the ImageView
        val dialog = AlertDialog.Builder(requireContext())
            .setView(imageView)
            .setPositiveButton("확인") { _, _ ->
            }
            .setCancelable(false)
            .create()

        // Show the dialog
        dialog.show()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }



    private fun clickEventHandler() {
        binding.mypetBeautyBackground.setOnClickListener {
            showHbDialog(requireContext(), "beauty", null)
        }
        binding.mypetHealthBackground.setOnClickListener {
            showHbDialog(requireContext(), "health",null)
        }
        binding.mypetShotBackground.setOnClickListener {
            showAddShotDialog()
        }
        binding.mypetLocationBackground.setOnClickListener {
            val intent = Intent(requireContext(), TrackLocationActivity::class.java)
            startActivity(intent)
        }
    }
    private fun refreshUI() {
        db.collection("TB_MYPET").document(petId)
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
                        val currentDate = Date()

                        // 미용 UI 업데이트
                        updateUIData("beauty", beautyDate, beautyLastDate, currentDate)
                        // 정기검진 UI 업데이트
                        updateUIData("health", healthDate, healthLastDate, currentDate)
                        // 접종기록 UI 업데이트
                        updateShotUI(snapshot)
                        // UI 업데이트
                        petInfoUpdate()
                    } else {
                        Log.d("ITM", "Current data: null")
                    }
                }
            }
    }
    // 접종History UI 업데이트
    private fun updateShotUI(snapshot: DocumentSnapshot) {
        // Fetch shot data from SUBTB_VACCINE subcollection
        db.collection("TB_MYPET").document(petId)
            .collection("SUBTB_VACCINE")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("ITM", "Error getting documents: ", error)
                    return@addSnapshotListener
                }
                shotList.clear()

                // Fetch the data from each document in the SUBTB_VACCINE subcollection
                for (document in value!!) {
                    val shotName = document.getString("shot_name") ?: ""
                    val shotNum = document.getString("shot_num") ?: ""
                    val shotDate = document.getString("shot_date") ?: ""

                    shotList.add(ShotItem(shotName, shotNum, shotDate))
                }

                if (!::shotAdapter.isInitialized) {
                    shotAdapter = ShotListAdapter(requireContext(), shotList as ArrayList<ShotItem>)

                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.shotList.layoutManager = layoutManager
                    binding.shotList.adapter = shotAdapter
                }
                shotAdapter.notifyDataSetChanged()
            }
    }

    private fun updateUIData(
        field: String,
        date: Timestamp?,
        lastDate: Timestamp?,
        currentDate: Date
    ) {
        val calendar = Calendar.getInstance()
        val timeZone = TimeZone.getDefault()
        calendar.timeZone = timeZone

        // Set the current date
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val currentDateMillis = calendar.timeInMillis

        // Set the selected date
        date?.let {
            calendar.time = it.toDate()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        val dateMillis = calendar.timeInMillis

        val isSameDayAndBeforeMidnight = dateMillis == currentDateMillis ||
                (dateMillis > currentDateMillis && dateMillis - currentDateMillis < 24 * 60 * 60 * 1000)

        val differenceInDays = if (isSameDayAndBeforeMidnight) {
            0
        } else {
            max(0, ((dateMillis - currentDateMillis) / (24 * 60 * 60 * 1000)).toInt())
        }

        Log.d("ITM", "Days remaining ($field): $differenceInDays")

        when (field) {
            "beauty" -> binding.mypetBeautyLeft.text = differenceInDays.toString()
            "health" -> binding.mypetHealthLeft.text = differenceInDays.toString()
        }

        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val formattedDate = lastDate?.toDate()?.let { sdf.format(it) } ?: ""
        when (field) {
            "beauty" -> binding.mypetBeautyDate.text = formattedDate
            "health" -> binding.mypetHealthDate.text = formattedDate
        }
    }



    //   미용, 정기검진에서 DatePicker띄우고 파이어베이스에 저장까지
    private fun showHbDialog(context: Context, field: String, shotDate: TextView?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_bh, null)
        val dialogBinding = DialogUpdateBhBinding.bind(dialogView)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialogBinding.bhCalenderIcon.setOnClickListener {
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
                    updateBeautyHealthDate(field, timestamp)

                    dialog.dismiss() // 날짜 선택 후 다이얼로그 닫기
                },
                year, month, day
            )
            datePickerDialog.datePicker.minDate = currentDate.timeInMillis
            datePickerDialog.show()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    // showDatePicker()활용 - 추가 정보가 필요하기 때문이다.
    private fun showAddShotDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_shot, null)
        val dialogBinding = DialogAddShotBinding.bind(dialogView)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        val spinnerItems = resources.getStringArray(R.array.my_array)

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.custom_spinner_items,
            spinnerItems.toList().subList(1, spinnerItems.size)
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        dialogBinding.shotSpinnerInput.prompt = "어떤 접종인가요?"

        dialogBinding.shotSpinnerInput.adapter = spinnerAdapter


        dialogBinding.shotCalenderIcon.setOnClickListener {
            // Use the existing code of showDatePicker here
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val day = currentDate.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                    val formattedDate = sdf.format(selectedDate.time)

                    dialogBinding.shotDateInput.text = formattedDate

                    val timestamp = selectedDate.timeInMillis
                    updateBeautyHealthDate("", timestamp)
                },
                year, month, day
            )
            datePickerDialog.datePicker.minDate = currentDate.timeInMillis
            datePickerDialog.show()
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
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    /***
     *  showDatePicker에서 Date를 파이어베이스에 어떻게 저장하는지
     *  최신 값을 field_last로 옮기고 최신 field값을 DatePicker에서 받은 값으로 함.
     */
    private fun updateBeautyHealthDate(field: String, timestamp: Long) {
        val documentRef = db.collection("TB_MYPET").document(petId)

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


    // showAddShotDialog()에서 얻은 값을 파이어베이스에 저장하는 로직(sub table에 저장)
    private fun saveShotDataToFirebase(shotItem: ShotItem) {
        // Get reference to the pet document using the pet_id
        val petRef = db.collection("TB_MYPET").document(petId)
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

    private fun petInfoUpdate() {
        val petCollection = db.collection("TB_PET")

        val docRef = petCollection.document(petId)
        if(docRef != null){
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        binding.mypetDogName.text = document["pet_name"].toString()
                        storageRef.child(document["pet_img"].toString()).downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                if(isAdded){
                                    Glide.with(binding.root.context)
                                        .load(task.result)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .apply(RequestOptions().circleCrop())
                                        .thumbnail(0.1f)
                                        .into(binding.mypetDogPicture)
                                }
                            } else {
                            }
                        }

                        binding.mypetDogPicture.clipToOutline = true

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        sensorManager.unregisterListener(this)
    }
}