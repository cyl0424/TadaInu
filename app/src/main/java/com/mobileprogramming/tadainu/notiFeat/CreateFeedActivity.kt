package com.mobileprogramming.tadainu.notiFeat

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityCameraPreviewBinding
import com.mobileprogramming.tadainu.databinding.ActivityCreateFeedBinding

class CreateFeedActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var galleryButton: Button
    private lateinit var cameraButton: Button
    private lateinit var previewImageTextView: TextView
    lateinit var binding: ActivityCreateFeedBinding

    private lateinit var photoFile: File
    private var imageCaptureUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_feed)
        binding = ActivityCreateFeedBinding.inflate(layoutInflater)

        cameraExecutor = Executors.newSingleThreadExecutor()
        galleryButton = findViewById(R.id.galleryButton)
        cameraButton = findViewById(R.id.cameraButton)
        previewImageTextView = findViewById(R.id.previewImage)

        // Set up the Camera capture
        imageCapture = ImageCapture.Builder().build()

        // Set up button click listeners
        galleryButton.setOnClickListener {
            openGallery()
        }
        cameraButton.setOnClickListener {
            val intent = Intent(this@CreateFeedActivity, CameraPreviewActivity::class.java)
            startActivity(intent)
        }
    }


    private fun takePhoto() {
        // Create a timestamped file to save the photo
        val photoFile = createPhotoFile()

        // Set up ImageCapture metadata
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = false
        }

        // Create output options for ImageCapture
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        // Capture the image
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    imageCaptureUri = Uri.fromFile(photoFile)
                    previewImageTextView.text = "Image saved: ${photoFile.absolutePath}"
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun createPhotoFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            photoFile = this
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }

    private val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                // Handle the selected image URI, e.g., display in ImageView
                previewImageTextView.text = "Selected image from gallery: $selectedImageUri"
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