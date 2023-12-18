package com.mobileprogramming.tadainu.homeFeat.adpter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.homeFeat.CalendarItem

class CustomAdapter(private val calendarItems: List<CalendarItem>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
    private val storageRef: StorageReference = storage.reference

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNumber: TextView = itemView.findViewById(R.id.textViewNumber)
        val imageViewPhoto: ImageView = itemView.findViewById(R.id.imageViewPhoto)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = calendarItems[position]

        // 데이터를 뷰에 바인딩
        holder.textViewNumber.text = item.number.toString()
        Log.d("photo",item.photoPath)
        storageRef.child(item.photoPath).downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(holder.itemView.context)
                    .load(task.result)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(0.1f)
                    .into(holder.imageViewPhoto)
            } else {
            }
        }

        // imageViewPhoto를 클릭했을 때 다이얼로그를 띄움
        holder.imageViewPhoto.setOnClickListener {
            showCustomDialog(holder.imageViewPhoto.context)
        }
    }

    fun showCustomDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_photo_cal, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnOK = dialogView.findViewById<Button>(R.id.btnOK)

        btnOK.setOnClickListener {
            // btnOK를 클릭하면 다이얼로그를 닫음
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_date, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return calendarItems.size
    }

    fun updateData(newList: MutableList<CalendarItem>) {
        notifyDataSetChanged()
    }
}
