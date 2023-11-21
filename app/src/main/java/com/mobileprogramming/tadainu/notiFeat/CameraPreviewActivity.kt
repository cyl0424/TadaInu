package com.mobileprogramming.tadainu.notiFeat

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import com.mobileprogramming.tadainu.databinding.FragmentNotiBinding

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

        // Check camera permissions
        if (allPermissionsGranted()) {
            // Start the camera when the activity is created
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                CreateFeedActivity.REQUIRED_PERMISSIONS,
                CreateFeedActivity.REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the Camera capture
        imageCapture = ImageCapture.Builder().build()  // Initialize imageCapture

        binding.captureButton.setOnClickListener {
            Log.d("ITM"," 캡쳐캡쳐~")
            takePhoto()
        }

        binding.goBackButton.setOnClickListener {
            finish()
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Bind the preview
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        // Set up the preview use case
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // Unbind use cases before rebinding
        cameraProvider.unbindAll()

        // Bind use cases to camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val camera = cameraProvider.bindToLifecycle(
            this, cameraSelector, preview, imageCapture
        )
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

                    // Save the image to the gallery
                    saveImageToGallery(photoFile)
//
//                    // Optionally, you can update the UI or show a message
//                    previewImageTextView.text = "Image saved: ${photoFile.absolutePath}"
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

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
//                previewImageTextView.text = "Selected image from gallery: $selectedImageUri"
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