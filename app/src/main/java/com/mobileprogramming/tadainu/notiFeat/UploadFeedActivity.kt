package com.mobileprogramming.tadainu.notiFeat

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mobileprogramming.tadainu.databinding.ActivityUploadFeedBinding
import com.mobileprogramming.tadainu.model.Feed
import java.util.UUID

class UploadFeedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadFeedBinding
    private lateinit var feedId: String
    private lateinit var progressBar: ProgressBar
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바설정
        val toolbar = binding.mypetToolbar
        val toolbarTitle = binding.mypetToolbar.toolbarTitle
        toolbarTitle.text = "피드 업로드"
        toolbar.backBtn.setOnClickListener {
            onBackPressed()
        }

        val imageUris = intent.getParcelableArrayListExtra<Uri>("croppedImageUris") as ArrayList<Uri>?
        Log.d("ITM", "Passed Uris : ${imageUris?.toTypedArray()}")

        if (imageUris != null) {
            for (i in 0 until imageUris.size) {
                val imageViewId = resources.getIdentifier("uploadImage${i + 1}", "id", packageName)
                val imageView = findViewById<ImageView>(imageViewId)
                imageView.setImageURI(imageUris[i])
            }
        } else {
            Log.e("ITM", "Image URIs are null")
        }

        progressBar = binding.progressBar

        binding.uploadBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            binding.uploadBtn.isEnabled = false
            uploadPhotosToFirebaseStorage(imageUris)
        }
    }

    // 파이어스토리지에 사진 저장
    private fun uploadPhotosToFirebaseStorage(imageUris: ArrayList<Uri>?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("care/feed")

        val feedId = generateFeedId()
        val timestamp = System.currentTimeMillis().toString()
        val imageUrls = mutableListOf<String>() // List to store image URLs

        imageUris?.forEachIndexed { index, uri ->
            val imageRef = storageRef.child("${feedId}_${timestamp}_$index")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    imageUrls.add(imageRef.path) // Save the path directly

                    if (imageUrls.size == imageUris.size) {
                        saveDataToFirestore(feedId, imageUrls)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle unsuccessful upload
                    Log.e("ITM", "Upload failed: $exception")
                    progressBar.visibility = View.INVISIBLE
                    binding.uploadBtn.isEnabled = true
                }
        }
    }

    // 파이어 스토어에 피드 내용 저장
    private fun saveDataToFirestore(feedId: String, imageUrls: List<String>) {
        val firestore = FirebaseFirestore.getInstance()
        val editText = binding.feedDetailDescription

        val feedData = Feed(
            feedId = feedId,
            feedCreatedAt = System.currentTimeMillis(),
            feedDescription = editText.text.toString(),
            feedImg = imageUrls, // Set the list of image URLs
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
    private fun generateFeedId(): String {
        return UUID.randomUUID().toString()
    }
}