package com.mobileprogramming.tadainu.partnersFeat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.mobileprogramming.tadainu.databinding.ActivityPurchaseListBinding
import com.mobileprogramming.tadainu.databinding.RecyclerPaymentsListBinding
import com.mobileprogramming.tadainu.partnersFeat.data.Payments
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PurchaseListActivity : AppCompatActivity() {
    private var mBinding: ActivityPurchaseListBinding? = null
    private val binding get() = mBinding!!
    val petId = prefs.getString("petId", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPurchaseListBinding.inflate(layoutInflater)
        val view = binding.root

        binding.toolbar.toolbarTitle.text = "결제내역"

        binding.toolbar.backBtn.setOnClickListener {
            onBackPressed()
            overridePendingTransition(0, 0)
        }

        if(petId != ""){
            val purchaseListAdapter = PurchaseListAdapter()
            binding.paymentsRecycler.layoutManager = LinearLayoutManager(this)
            binding.paymentsRecycler.adapter = purchaseListAdapter
        }

        setContentView(view)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    @OptIn(DelicateCoroutinesApi::class)
    inner class PurchaseListAdapter() :
        RecyclerView.Adapter<PurchaseListAdapter.CustomViewHolder>() {
        var paymentsList : MutableList<Payments> = mutableListOf()

        val db = FirebaseFirestore.getInstance()
        private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://tadainu-2023.appspot.com/")
        private val storageRef: StorageReference = storage.reference


        init {
            GlobalScope.launch {
                db.collection("TB_PAYMENTS")
                    .whereEqualTo("payments_pet_id", petId)
                    .get()
                    .addOnSuccessListener { result ->
                        if(!result.isEmpty){
                            binding.emptyTxt.visibility = View.GONE
                        }
                        for (doc in result) {
                            val payments = Payments(
                                payments_id = doc["payments_id"] as String,
                                payments_created_at = doc["payments_at"] as Timestamp,
                                payments_user_id = doc["payments_user_id"] as String,
                                payments_partner_id  = doc["payments_partner_id"] as String,
                                payments_pet_id  = doc["payments_pet_id"] as String,
                                payments_amount = doc["payments_amount"] as Long,
                            )

                            paymentsList.add(payments)
                            notifyDataSetChanged()
                        }
                    }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PurchaseListAdapter.CustomViewHolder {
            val binding = RecyclerPaymentsListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return CustomViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PurchaseListAdapter.CustomViewHolder, position: Int) {
            val paymentItem = paymentsList[position]
            holder.bind(paymentItem)
        }

        override fun getItemCount(): Int {
            return paymentsList.size
        }

        inner class CustomViewHolder(private val binding: RecyclerPaymentsListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(paymentItem: Payments) {
                binding.apply {
                    db.collection("TB_PETCARE")
                        .document(paymentItem.payments_partner_id)
                        .get()
                        .addOnSuccessListener { doc ->
                            binding.payPartner.text = doc["petcare_name"].toString()
                        }


                    binding.payAmount.text = "${paymentItem.payments_amount}원"

                    val timestamp : Timestamp = paymentItem.payments_created_at
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("MM월 dd일", Locale.KOREA)
                    val dateString = formatter.format(date)

                    binding.payTime.text = dateString
                }
            }
        }
    }

}