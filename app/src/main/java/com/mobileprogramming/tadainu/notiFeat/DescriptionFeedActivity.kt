package com.mobileprogramming.tadainu.notiFeat

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ActivityDescriptionFeedBinding
import com.mobileprogramming.tadainu.model.Feed
import com.yalantis.ucrop.UCrop
import java.util.UUID

class DescriptionFeedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDescriptionFeedBinding
    private lateinit var feedId: String

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUris = intent.getParcelableArrayListExtra<Uri>("croppedImageUris") as ArrayList<Uri>?
        Log.d("ITM", "Passed Uris : ${imageUris?.toTypedArray()}")

        if (imageUris != null) {
            // Process and display images
            for (i in 0 until imageUris.size) {
                val imageViewId = resources.getIdentifier("uploadImage${i + 1}", "id", packageName)
                val imageView = findViewById<ImageView>(imageViewId)
                imageView.setImageURI(imageUris[i])
            }
        } else {
            Log.e("ITM", "Image URIs are null")
        }

        binding.uploadBtn.setOnClickListener {
            uploadPhotosToFirebaseStorage(imageUris)
        }
    }
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
                }
        }
    }

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
                // Document added successfully
                Log.d("ITM", "Feed data added to Firestore")
            }
            .addOnFailureListener { e ->
                // Handle errors
                Log.e("ITM", "Error adding feed data to Firestore: $e")
            }
    }
    private fun generateFeedId(): String {
        // You can implement your logic to generate a unique feed ID
        return UUID.randomUUID().toString()
    }
}