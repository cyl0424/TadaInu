package com.mobileprogramming.tadainu.partnersFeat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.RecyclerFeedItemBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(DelicateCoroutinesApi::class)
class FeedListAdapter() :
    RecyclerView.Adapter<FeedListAdapter.CustomViewHolder>() {
    val petId = prefs.getString("petId", "")
    var feedList = mutableListOf<MutableMap<String, *>>()

    val db = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
    private val storageRef: StorageReference = storage.reference

    init {
        GlobalScope.launch {
            db.collection("TB_FEED")
                .whereEqualTo("feedPet", petId)
                .get()
                .addOnSuccessListener { result ->

                    for (document in result) {
                        Log.d("document", document.toString())
                        val feedWriterId = document.getString("feedWriterId")
                        db.collection("TB_PETCARE")
                            .document(feedWriterId!!)
                            .get()
                            .addOnSuccessListener { doc ->
                                val feedMap = mutableMapOf<String, Any>()
                                feedMap["profile_name"] = doc["petcare_name"].toString()
                                feedMap["profile_img"] = doc.getString("petcare_img") ?: ""
                                feedMap["feed_date"] = document["feedCreatedAt"] as Timestamp
                                feedMap["feed_txt"] = document.getString("feedDescription")!!
                                feedMap["feed_img"] = document["feedImgPaths"]!!

                                feedList.add(feedMap)
                                notifyDataSetChanged()
                            }
                    }
                }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FeedListAdapter.CustomViewHolder {
        val binding = RecyclerFeedItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedListAdapter.CustomViewHolder, position: Int) {
        val feedItem = feedList[position]
        holder.bind(feedItem)
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    inner class CustomViewHolder(private val binding: RecyclerFeedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(feedItem: MutableMap<String, *>) {
            binding.apply {
                storageRef.child(feedItem["profile_img"].toString()).downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Glide.with(itemView)
                            .load(task.result)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(50, 50)
                            .thumbnail(0.1f)
                            .into(binding.profileImg)

                    }
                }
                binding.profileName.text = feedItem["profile_name"].toString()
                binding.feedTxt.text = feedItem["feed_txt"].toString()

                val timestamp : Timestamp = feedItem["feed_date"] as Timestamp
                val date = timestamp.toDate()
                val formatter = SimpleDateFormat("MM월 dd일", Locale.KOREA)
                val dateString = formatter.format(date)
                binding.feedDate.text = dateString

                val imageList:List<String> = feedItem["feed_img"] as List<String>
                val indicator = binding.dotsIndicator
                binding.feedImg.adapter = ViewPagerAdapter(imageList)
                indicator.attachTo(binding.feedImg)
            }
        }
    }
}

class ViewPagerAdapter(private val imageList: List<String>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
    private val storageRef: StorageReference = storage.reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_img, parent, false)
        return PagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val imageUrl = imageList[position]
        storageRef.child(imageUrl).downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(holder.itemView)
                    .load(task.result)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(150, 150)
                    .thumbnail(0.1f)
                    .into(holder.imageView)

            }
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class PagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.feed_img)
    }
}