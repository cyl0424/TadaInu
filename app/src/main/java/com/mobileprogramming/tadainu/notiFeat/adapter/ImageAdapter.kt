package com.mobileprogramming.tadainu.notiFeat.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mobileprogramming.tadainu.R


class ImageAdapter(
    private val context: Context,
    private val ImageList: List<Uri>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private var imageUris: MutableList<Uri> = ImageList.toMutableList() // Initialize with ImageList
    private var onItemClickListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (position == 0) {
            holder.imageView.setImageResource(R.drawable.feed_addition_image)
            holder.itemView.setOnClickListener {
                onItemClickListener?.invoke()
            }
        } else {
            val uri = imageUris[position - 1]
            holder.imageView.setImageURI(uri)
        }
    }

    override fun getItemCount(): Int {
        return imageUris.size + 1
    }

    fun setImageUris(uris: List<Uri>) {
        this.imageUris = uris.toMutableList()
        notifyDataSetChanged()
    }

    fun addImageUri(uri: Uri) {
        imageUris.add(uri)
        notifyItemInserted(imageUris.size)
    }

    fun setOnItemClickListener(listener: () -> Unit) {
        this.onItemClickListener = listener
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageItem)
    }
}