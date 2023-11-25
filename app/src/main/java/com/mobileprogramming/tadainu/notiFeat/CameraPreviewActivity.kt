package com.mobileprogramming.tadainu.notiFeat

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityCameraPreviewBinding
import com.mobileprogramming.tadainu.notiFeat.UploadFeedActivity.Companion.sharedImageList
import com.yalantis.ucrop.UCrop

class CameraPreviewActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView
    lateinit var binding: ActivityCameraPreviewBinding


    private lateinit var photoFile: File
    private var imageCaptureUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()
        previewView = findViewById(R.id.previewView)

        // 카메라 권한 여부 판단
        if (allPermissionsGranted()) {
            // 권한 있으면 카메라 시작
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                NotiFragment.REQUIRED_PERMISSIONS,
                NotiFragment.REQUEST_CODE_PERMISSIONS
            )
        }

        // ImageCapture Initializing
        imageCapture = ImageCapture.Builder().build()

        // 캡쳐 버튼 누르면 사진 찍기
        binding.captureButton.setOnClickListener {
            Log.d("ITM"," 캡쳐캡쳐~")
            takePhoto()
        }

        // 뒤로가기 버튼
        binding.goBackButton.setOnClickListener {
            finish()
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        // Preview 선언
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val camera = cameraProvider.bindToLifecycle(
            this, cameraSelector, preview, imageCapture
        )
    }

    // 사진 찍기
    private fun takePhoto() {
        // 사진 파일 생성
        val photoFile = createPhotoFile()

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = false
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        // 캡쳐
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    imageCaptureUri = Uri.fromFile(photoFile)
                    // 저장
                    saveImageToGallery(photoFile)

                    // 찍은 사진 Ucrop
                    imageCaptureUri?.let {
                        UCrop.of(it, Uri.fromFile(photoFile))
                            .withAspectRatio(1f, 1f)
                            .withMaxResultSize(1000, 1000)
                            .start(this@CameraPreviewActivity)
                    } ?: run {
                        Log.e(TAG, "Image 찍은거 null")
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }
    // Ucrop의 결과 Uri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri: Uri? = UCrop.getOutput(data!!)
            if (resultUri != null) {
                // Companion Object에 담아줌
                sharedImageList.add(resultUri)

                // UploadFeedActivity로 넘어감
                val intent = Intent(this, UploadFeedActivity::class.java)
                startActivity(intent)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError: Throwable? = UCrop.getError(data!!)
            Log.e("ITM", "Error cropping image: $cropError")
        }
    }

    // Modify the saveImageToGallery function
    private fun saveImageToGallery(photoFile: File) {
        // Use MediaStore to insert the image into the gallery
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Camera")
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.also { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val photoBytes = photoFile.readBytes()
                outputStream.write(photoBytes)
            }
        }
        // Set the imageCaptureUri to the URI of the saved image
        imageCaptureUri = Uri.fromFile(photoFile)
        setResult(Activity.RESULT_OK)
    }
    private fun createPhotoFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            photoFile = this
        }
    }



    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val TAG = "CreateFeedActivity"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}