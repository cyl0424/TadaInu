package com.mobileprogramming.tadainu.accountFeat

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.google.zxing.qrcode.encoder.QRCode
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.MainActivity
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.accountFeat.data.PetData
import com.mobileprogramming.tadainu.accountFeat.data.UserData
import com.mobileprogramming.tadainu.databinding.ActivityMoreInfoBinding
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class MoreInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoreInfoBinding
    private var gender: String? = null
    private var relation: String? = null
    private var profileImageUri: Uri? = null
    private var profileImage: Bitmap? = null
    private var profileImageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.togetherSpace.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%04d%02d%02d", year, month + 1, dayOfMonth)
                    binding.togetherTxt.text = selectedDate
                    binding.togetherTxt.setTextColor(Color.parseColor("#606060"))
                    validateInput()
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        binding.birthSpace.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%04d%02d%02d", year, month + 1, dayOfMonth)
                    binding.birthTxt.text = selectedDate
                    binding.birthTxt.setTextColor(Color.parseColor("#606060"))
                    validateInput()
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

//        val relationItems = arrayOf("누나", "형", "오빠", "언니", "삼촌", "이모", "고모")
//        val relationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, relationItems)
//        binding.relationSpinner.adapter = relationAdapter
//        binding.relationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                binding.etcButton.text = relationItems[position]
//                validateInput()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//
//        binding.etcButton.setOnClickListener {
//            binding.relationSpinner.performClick()
//        }
//
//        binding.etcButton.setOnClickListener {
//            binding.relationSpinner.performClick()
//        }

        binding.genderGroup.setOnCheckedChangeListener { _, checkedId ->
            gender = when (checkedId) {
                R.id.male_button -> "남자"
                R.id.female_button -> "여자"
                else -> null
            }
            validateInput()
        }

        binding.relationGroup.setOnCheckedChangeListener { _, checkedId ->
            relation = when (checkedId) {
                R.id.dad_button -> "아빠"
                R.id.mom_button -> "엄마"
                else -> binding.etcButton.text.toString()
            }
            validateInput()
        }

        binding.profileImg.setOnClickListener {
            chooseProfileImage()
        }
        binding.tempProfile.setOnClickListener {
            chooseProfileImage()
        }

        binding.completeBtn.setOnClickListener {
            saveData(false, null)
        }

        binding.inviteCheck.setOnClickListener {
            scanQRCode()
        }
    }

    private fun chooseProfileImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

        startActivityForResult(intent, REQUEST_CODE_CHOOSE_PROFILE_IMAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    chooseProfileImage()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE_PROFILE_IMAGE && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.data
            val inputStream: InputStream? = contentResolver.openInputStream(profileImageUri!!)
            profileImage = BitmapFactory.decodeStream(inputStream)

            val roundedBitmapDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, profileImage)
            roundedBitmapDrawable.isCircular = true

            binding.profileImg.setImageDrawable(roundedBitmapDrawable)
            binding.tempProfile.visibility = View.GONE

            profileImageName = "pet/profile/${UUID.randomUUID()}.png"
            uploadProfileImage(profileImage!!, profileImageName!!)
        }else{
            val result: IntentResult =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if(result != null) {
                if (result.contents == null) {
                    Log.d("this", "잘못된 QR코드입니다.")
                } else {
                    val pet_id = result.contents.toString()
                    Log.d("this", pet_id)
                    if(pet_id.contains("pet_id")){
                        val gson = Gson()
                        val jsonMap = gson.fromJson(pet_id, Map::class.java) as Map<*, *>
                        val petId = jsonMap["pet_id"].toString()
//                        Toast.makeText(this, petId, Toast.LENGTH_SHORT).show()
                        showRelationshipDialog(petId)
                    }

                }
            }
            else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun showRelationshipDialog(pet_id:String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_relationship_selector, null)
        val spinnerRelationship = dialogView.findViewById<Spinner>(R.id.spinnerRelationship)
        val btnCancel = dialogView.findViewById<ConstraintLayout>(R.id.cancel_btn)
        val btnOK = dialogView.findViewById<ConstraintLayout>(R.id.ok_btn)

        val relationships = arrayOf("엄마", "아빠", "언니", "누나", "오빠", "형", "삼촌", "이모", "할머니", "할아버지")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, relationships)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRelationship.adapter = adapter

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnOK.setOnClickListener {
            relation = spinnerRelationship.selectedItem.toString()
            saveData(true, pet_id)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun uploadProfileImage(bitmap: Bitmap, imageName: String) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageData = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child(imageName)
        imageRef.putBytes(imageData).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                validateInput()
            }
        }
    }

    private fun validateInput() : Boolean{
        val isInputValid = profileImageName != null &&
                binding.nameTxt.text.isNotEmpty() &&
                binding.togetherTxt.text.length == 8 &&
                gender != null &&
                relation != null
        if (isInputValid) {
            binding.completeBtn.isEnabled = true
            binding.completeBtnTxt.text = "시작하기"
            binding.completeBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ED7D31"))
        } else {
            binding.completeBtn.isEnabled = false
            binding.completeBtnTxt.text = "필수 정보를 모두 입력해주세요"
            binding.completeBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFC1C1C1"))
        }
        return binding.completeBtn.isEnabled
    }

    private fun saveData(hasPetId: Boolean, pet_id: String?) {
        if (validateInput() || hasPetId) {
            var userId = prefs.getString("currentUser","")
            if(userId == ""){
                userId =  UUID.randomUUID().toString()
            }
            val userData = UserData(
                user_id = userId,
                user_email = intent.getStringExtra("user_email").toString(),
            )

            var petId = ""

            if(!hasPetId){
                petId = UUID.randomUUID().toString()
                val petData = PetData(
                    pet_adopt_day = binding.togetherTxt.text.toString(),
                    pet_img = profileImageName.toString(),
                    pet_id = petId,
                    pet_birthday = binding.birthTxt.text.toString(),
                    pet_gender = gender.toString(),
                    pet_name = binding.nameTxt.text.toString()
                )
                FirebaseFirestore.getInstance()
                    .collection("TB_PET")
                    .document(petId)
                    .set(petData)
                    .addOnSuccessListener {
                    }

                userData.user_pet.add(mapOf(petId to relation.toString()))

                FirebaseFirestore.getInstance()
                    .collection("TB_USER")
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        moveToMainActivity(userId, petId)
                    }
            }else{
                petId = pet_id.toString()
                userData.user_pet.add(mapOf(petId to relation.toString()))

                FirebaseFirestore.getInstance()
                    .collection("TB_USER")
                    .document(userId)
                    .update("user_pet", FieldValue.arrayUnion(mapOf(petId to relation.toString())))
                    .addOnSuccessListener {
                        moveToMainActivity(userId, petId)
                    }

            }
        }
    }

    private fun moveToMainActivity(userId : String, petId: String) {
        val intent = Intent(this, MainActivity::class.java)
        prefs.setString("currentUser", userId)
        prefs.setString("petId", petId)
        startActivity(intent)
        Toast.makeText(this, "가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
    }



    companion object {
        private const val REQUEST_CODE_CHOOSE_PROFILE_IMAGE = 1
        private const val REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 2
    }

    fun scanQRCode(){
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("QR코드를 스캔해주세요")
        integrator.setOrientationLocked(false)
        integrator.setBeepEnabled(false)
        integrator.setCaptureActivity(QRScanActivity::class.java) // 추가된 라인
        integrator.initiateScan()
    }
}
