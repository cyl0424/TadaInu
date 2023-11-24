package com.mobileprogramming.tadainu.notiFeat

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mobileprogramming.tadainu.databinding.ActivityCameraGalleryBinding
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID


class CameraGalleryActivity : AppCompatActivity() {
    private lateinit var galleryButton: Button
    private lateinit var imageView: ImageView
    private lateinit var cameraButton: Button
    private lateinit var previewImageTextView: TextView
    private lateinit var binding: ActivityCameraGalleryBinding
    private var selectedImageUris: ArrayList<Uri> = ArrayList()
    private var croppedImageUris: ArrayList<Uri> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        galleryButton = binding.galleryButton
        cameraButton = binding.cameraButton
        previewImageTextView = binding.previewImage
        // 툴바설정
        val toolbar = binding.mypetToolbar
        val toolbarTitle = binding.mypetToolbar.toolbarTitle
        toolbarTitle.text = "피드 사진 선택하기"
        toolbar.backBtn.setOnClickListener {
            onBackPressed()
        }

        // 갤러리 선택 시
        galleryButton.setOnClickListener {
            openGallery()
        }
        // 카메라 선택 시
        cameraButton.setOnClickListener {
            val intent = Intent(this@CameraGalleryActivity, CameraPreviewActivity::class.java)
            startActivity(intent)
        }
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

    private val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // RESULT_OK는 -1임
            if (result.resultCode == RESULT_OK) {
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
    private fun processSelectedImages(selectedImageUris: ArrayList<Uri>) {
        // Pass the list of URIs directly to startCropActivity
        startCropActivity(selectedImageUris, 0)
    }
    private var processedImageCount = 0;
    private fun startCropActivity(sourceUris: ArrayList<Uri>, index: Int) {
        if (index < sourceUris.size) {
            Log.d("ITM","startcropactivity$index")
            val sourceUri = sourceUris[index]
            val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image_${UUID.randomUUID()}.jpg"))
            val uCrop = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)

            val options = UCrop.Options()
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
            options.setCompressionQuality(80)
            uCrop.withOptions(options)

            val uCropIntent = uCrop.getIntent(this)
            cropActivityResultLauncher.launch(uCropIntent)
        } else {
            Log.d("ITM","startcropactivity$index")
            // 모든 이미지가 다 처리되어야만 다음 엑티비티로 넘어감
            val intent = Intent(this, UploadFeedActivity::class.java)
            intent.putParcelableArrayListExtra("croppedImageUris", croppedImageUris)
            Log.d("ITM", "Before Passing Uris : ${croppedImageUris}")
            startActivity(intent)

            processedImageCount = 0
        }
    }

    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val resultUri: Uri? = UCrop.getOutput(data)
                    Log.d("ITM", "resultUri: $resultUri")
                    if (resultUri != null) {
                        // CROP한 사진 ArrayList에 추가
                        croppedImageUris.add(resultUri)
                    }
                    processedImageCount++
                    startCropActivity(selectedImageUris, processedImageCount)
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val error: Throwable? = UCrop.getError(result.data!!)
                Log.e(TAG, "Error cropping image: ${error?.message}", error)
            }
        }
    companion object {
        const val TAG = "CreateFeedActivity"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val UCROP_REQUEST_CODE = 123
    }
}
