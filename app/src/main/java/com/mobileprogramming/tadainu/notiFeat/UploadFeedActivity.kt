package com.mobileprogramming.tadainu.notiFeat

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityUploadFeedBinding
import com.mobileprogramming.tadainu.model.Feed
import com.mobileprogramming.tadainu.notiFeat.adapter.ImageAdapter
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class UploadFeedActivity : AppCompatActivity() {

    private var REQUEST_CODE_CAMERA: Int = 0
    private lateinit var binding: ActivityUploadFeedBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var selectedImageUris: ArrayList<Uri> = ArrayList()
    private var processedImageCount = 0 // 갤러리에서 선택한 이미지 수 계산해서 Ucrop끝날 시점 파악
    companion object {
        val sharedImageList: MutableList<Uri> = mutableListOf()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("ITM","${sharedImageList}")
        val toolbar = binding.mypetToolbar
        val toolbarTitle = binding.mypetToolbar.toolbarTitle
        toolbarTitle.text = "피드 업로드"
        toolbar.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Recycler View 초기화( adapter에다가 sharedImageList 넣음 )
        recyclerView = binding.imageRecyclerView
        adapter = ImageAdapter(this, sharedImageList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        adapter.setImageUris(sharedImageList)
        // 첫번째 recyclerview항목인 + 누르면 다시 카메라, 갤러리 선택하는 dialog뜨도록
        adapter.setOnItemClickListener {
            showImageOptionsDialog()
        }
        progressBar = binding.progressBar

        binding.uploadBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            binding.uploadBtn.isEnabled = false
            uploadPhotosToFirebaseStorage(sharedImageList)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.getParcelableExtra("capturedImageUri")
            if (imageUri != null) {
                sharedImageList.add(imageUri)
                adapter.addImageUri(imageUri) // Add this line to update the adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
    }

    // 파이어 스토리지에 저장
    private fun uploadPhotosToFirebaseStorage(imageUris: List<Uri>) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("care/feed")

        val feedId = generateFeedId()

        // Paths of uploaded images
        val imagePaths = mutableListOf<String>()

        // 모든 이미지를 업로드하기 위한 작업 수
        val totalImages = imageUris.size
        var uploadedImages = 0

        // 모든 이미지를 처리
        for ((index, imageUri) in imageUris.withIndex()) {
            val imageRef = storageRef.child("${feedId}_${index}")

            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    val imageUrl = imageRef.path
                    imagePaths.add(imageUrl) // Add the path to the list

                    uploadedImages++

                    if (uploadedImages == totalImages) {
                        // 모든 이미지가 업로드되면 Firestore에 데이터 저장
                        saveDataToFirestore(feedId, imagePaths)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ITM", "Upload failed: $exception")
                    progressBar.visibility = View.INVISIBLE
                    binding.uploadBtn.isEnabled = true
                }
        }
    }

    private fun saveDataToFirestore(feedId: String, imagePaths: List<String>) {
        val firestore = FirebaseFirestore.getInstance()
        val editText = binding.feedDetailDescription.text.toString()
        val createdAtTimestamp = com.google.firebase.Timestamp.now()
        val feedData = Feed(
            feedId = feedId,
            feedCreatedAt = createdAtTimestamp,
            feedDescription = editText,
            feedImgPaths = imagePaths,
            feedLike = 0,
            feedPet = null,
            feedWriterId = null
        )

        firestore.collection("TB_FEED")
            .document(feedId)
            .set(feedData)
            .addOnSuccessListener {
                Log.d("ITM", "Feed data added to Firestore")
                progressBar.visibility = View.INVISIBLE
                binding.uploadBtn.isEnabled = true
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("ITM", "Error adding feed data to Firestore: $e")
                progressBar.visibility = View.INVISIBLE
                binding.uploadBtn.isEnabled = true
            }
    }

    // 이미지 추가 버튼 누르면 카메라, 갤러리 선택하도록
    private fun showImageOptionsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_camera_gallery)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val galleryButton = dialog.findViewById<Button>(R.id.galleryButton)
        galleryButton.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }

        val cameraButton = dialog.findViewById<Button>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            val intent = Intent(this, CameraPreviewActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
            dialog.dismiss()
        }

        dialog.show()
    }


    // 갤러리 열기
    private fun openGallery() {
        // 갤러리를 여는 Intent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        // 여러개 선택 가능 하도록
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        // 결과를 처리하기 위해 resultLauncher를 사용하여 갤러리 액티비티 시작
        resultLauncher.launch(galleryIntent)
    }
    // 갤러리 결과 처리
    private val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // RESULT_OK는 -1임
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data

                // 이미지 여러 개일 때 갤러리에서 선택한 이미지들을 처리
                if (data?.clipData != null) {
                    val clipData = data.clipData

                    // 선택한 이미지들을 담을 리스트 초기화
                    selectedImageUris = ArrayList()

                    // 모든 선택된 이미지에 대해 반복
                    for (i in 0 until clipData!!.itemCount) {
                        val selectedImageUri = clipData!!.getItemAt(i).uri

                        // 리스트에 선택한 이미지 추가
                        selectedImageUris.add(selectedImageUri)
                    }

                    // 선택된 모든 이미지들을 처리하는 함수 호출
                    processSelectedImages(selectedImageUris)
                } else if (data?.data != null) {
                    // 단일 이미지 일때
                    // 선택한 이미지의 URI를 가져옴
                    val selectedImageUri = data.data

                    // 리스트를 초기화하고 선택한 이미지를 추가
                    if (selectedImageUri != null) {
                        selectedImageUris = arrayListOf(selectedImageUri)
                        processSelectedImages(selectedImageUris)
                    }
                }
            }
        }

    // 갤러리에서 선택한 이미지 처리
    private fun processSelectedImages(selectedImageUris: ArrayList<Uri>) {
        // Reset processedImageCount and croppedImageUris
        processedImageCount = 0
        // Start processing the images
        startCropActivity(selectedImageUris, processedImageCount)
    }

    // Ucrop
    private fun startCropActivity(sourceUris: ArrayList<Uri>, index: Int) {
        if (index < sourceUris.size) {
            val sourceUri = sourceUris[index]
            val destinationUri =
                Uri.fromFile(File(cacheDir, "cropped_image_${UUID.randomUUID()}.jpg"))
            val uCrop = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)

            val options = UCrop.Options()
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
            options.setCompressionQuality(80)
            uCrop.withOptions(options)

            val uCropIntent = uCrop.getIntent(this)
            cropActivityResultLauncher.launch(uCropIntent)
        } else {
            // All images have been processed, start the UploadFeedActivity
            val intent = Intent(this, UploadFeedActivity::class.java)
            startActivity(intent)
        }
    }
    // 다이얼로그에서 crop한 사진 결과 처리
    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val resultUri: Uri? = UCrop.getOutput(data)
                    if (resultUri != null) {
                        // CROP한 사진 ArrayList에 추가
                        sharedImageList.add(resultUri)
                    }
                    processedImageCount++

                    startCropActivity(selectedImageUris, processedImageCount)
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val error: Throwable? = UCrop.getError(result.data!!)
                Log.e(NotiFragment.TAG, "Error cropping image: ${error?.message}", error)
            }
        }

    // 고유 식별자 만들기용
    private fun generateFeedId(): String {
        val currentTimestamp = System.currentTimeMillis()
        val formattedTimestamp = android.text.format.DateFormat.format("yyyyMMddHHmm", currentTimestamp)
        return "${UUID.randomUUID()}_$formattedTimestamp"
    }

}