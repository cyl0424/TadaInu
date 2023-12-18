package com.mobileprogramming.tadainu.notiFeat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.databinding.FragmentNotiBinding
import com.mobileprogramming.tadainu.databinding.RecyclerPaymentsListBinding
import com.mobileprogramming.tadainu.partnersFeat.data.Reservation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Locale

class NotiFragment : Fragment() {
    private var mBinding:FragmentNotiBinding? = null
    private val binding get() = mBinding!!

    val userId = prefs.getString("currentUser", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentNotiBinding.inflate(inflater, container, false)
        val view = binding.root

        if(userId != ""){
            val purchaseListAdapter = NotificationListAdapter()
            binding.notiRecycler.layoutManager = LinearLayoutManager(requireContext())
            binding.notiRecycler.adapter = purchaseListAdapter
        }

        binding.toolbar.backBtn.visibility = View.GONE
        binding.toolbar.toolbarTitle.text = "알림 내역"

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    @OptIn(DelicateCoroutinesApi::class)
    inner class NotificationListAdapter() :
        RecyclerView.Adapter<NotificationListAdapter.CustomViewHolder>() {
        var notificationList = mutableListOf<MutableMap<String, Any>>()

        val db = FirebaseFirestore.getInstance()
        private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
        private val storageRef: StorageReference = storage.reference


        init {
            GlobalScope.launch {
                db.collection("TB_NOTI")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { result ->
                        val contents = result["noti_contents"] as MutableList<MutableMap<String, Any>>
                        if(!contents.isEmpty()){
                            binding.emptyTxt.visibility = View.GONE
                        }
                        for (doc in contents) {
                            val newNoti = mutableMapOf<String, Any>()
                            newNoti["noti_msg"] = doc["noti_msg"] as String
                            newNoti["noti_by"] = doc["noti_by"] as String
                            newNoti["noti_at"] = doc["noti_at"] as Timestamp

                            notificationList.add(newNoti)
                            notifyDataSetChanged()
                        }
                    }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): NotificationListAdapter.CustomViewHolder {
            val binding = RecyclerPaymentsListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return CustomViewHolder(binding)
        }

        override fun onBindViewHolder(holder: NotificationListAdapter.CustomViewHolder, position: Int) {
            val paymentItem = notificationList[position]
            holder.bind(paymentItem)
        }

        override fun getItemCount(): Int {
            return notificationList.size
        }

        inner class CustomViewHolder(private val binding: RecyclerPaymentsListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(paymentItem: MutableMap<String, Any>) {
                binding.apply {
                    binding.payAmount.text = paymentItem["noti_msg"] as String
                    binding.payPartner.text = paymentItem["noti_by"] as String

                    val timestamp : Timestamp = paymentItem["noti_at"] as Timestamp
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("MM월 dd일", Locale.KOREA)
                    val dateString = formatter.format(date)

                    binding.payTime.text = dateString
                }
            }
        }
    }
}