package com.mobileprogramming.tadainu.partnersFeat

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobileprogramming.tadainu.GlobalApplication
import com.mobileprogramming.tadainu.databinding.ActivityReservationListBinding
import com.mobileprogramming.tadainu.databinding.RecyclerPaymentsListBinding
import com.mobileprogramming.tadainu.partnersFeat.data.Reservation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ReservationListActivity : AppCompatActivity() {
    private var mBinding: ActivityReservationListBinding? = null
    private val binding get() = mBinding!!

    val petId = GlobalApplication.prefs.getString("petId", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityReservationListBinding.inflate(layoutInflater)
        val view = binding.root
        binding.toolbar.toolbarTitle.text = "예약내역"

        binding.toolbar.backBtn.setOnClickListener {
            onBackPressed()
            overridePendingTransition(0, 0)
        }

        if(petId != ""){
            val purchaseListAdapter = ReservationListAdapter()
            binding.reservationRecycler.layoutManager = LinearLayoutManager(this)
            binding.reservationRecycler.adapter = purchaseListAdapter
        }

        setContentView(view)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inner class ReservationListAdapter() :
        RecyclerView.Adapter<ReservationListAdapter.CustomViewHolder>() {
        var reservationList : MutableList<Reservation> = mutableListOf()

        val db = FirebaseFirestore.getInstance()
        private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
        private val storageRef: StorageReference = storage.reference


        init {
            GlobalScope.launch {
                db.collection("TB_RESERVATION")
                    .whereEqualTo("reservation_pet_id", petId)
                    .get()
                    .addOnSuccessListener { result ->
                        if(!result.isEmpty){
                            binding.emptyTxt.visibility = View.GONE
                        }
                        for (doc in result) {
                            val reservation = Reservation(
                                reservation_id = doc["reservation_id"] as String,
                                reservation_at = doc["reservation_at"] as Timestamp,
                                reservation_user_id = doc["reservation_user_id"] as String,
                                reservation_partner_id  = doc["reservation_partner_id"] as String,
                                reservation_pet_id  = doc["reservation_pet_id"] as String,
                                reservation_check_in = doc["reservation_check_in"] as Timestamp,
                                reservation_check_out = doc["reservation_check_out"] as Timestamp,
                            )

                            reservationList.add(reservation)
                            notifyDataSetChanged()
                        }
                    }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ReservationListAdapter.CustomViewHolder {
            val binding = RecyclerPaymentsListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return CustomViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ReservationListAdapter.CustomViewHolder, position: Int) {
            val paymentItem = reservationList[position]
            holder.bind(paymentItem)
        }

        override fun getItemCount(): Int {
            return reservationList.size
        }

        inner class CustomViewHolder(private val binding: RecyclerPaymentsListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(paymentItem: Reservation) {
                binding.apply {
                    db.collection("TB_PETCARE")
                        .document(paymentItem.reservation_partner_id)
                        .get()
                        .addOnSuccessListener { doc ->
                            binding.payPartner.text = doc["petcare_name"].toString()
                        }


                    val checkin_timestamp : Timestamp? = paymentItem.reservation_check_in
                    val checkin_date = checkin_timestamp?.toDate()

                    val checkout_timestamp : Timestamp? = paymentItem.reservation_check_out
                    val checkout_date = checkout_timestamp?.toDate()

                    val checkin_formatter = SimpleDateFormat("MM/dd hh:mm", Locale.KOREA)

                    val checkin_dateString = checkin_date?.let { checkin_formatter.format(it) }
                    val checkout_dateString = checkout_date?.let { checkin_formatter.format(it) }

                    binding.payAmount.text = "${checkin_dateString} ~ ${checkout_dateString}"
                    Log.d("예약날짜", "${checkin_dateString} ~ ${checkout_dateString}")

                    val timestamp : Timestamp = paymentItem.reservation_at
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("MM월 dd일", Locale.KOREA)
                    val dateString = formatter.format(date)

                    binding.payTime.text = dateString
                }
            }
        }
    }
}