package com.mobileprogramming.tadainu.partnersFeat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mobileprogramming.tadainu.databinding.FragmentPartnersListItemBinding
import com.mobileprogramming.tadainu.model.PetcareItem

class ClusterClickVer2Adapter(
    private val context: Context,
    private val petcareList: MutableList<PetcareItem>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ClusterClickVer2Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = FragmentPartnersListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petcareItem = petcareList[position]
        holder.bind(petcareItem)
    }

    override fun getItemCount(): Int {
        return petcareList.size
    }

    inner class ViewHolder(private val binding: FragmentPartnersListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(partner: PetcareItem) {
            // Update the binding views with the data
            binding.petcatrType.text = partner.petcare_type
            binding.petcareName.text = partner.petcare_name
            binding.petcareOpening.text = partner.petcare_opening
            binding.petcareClosing.text = partner.petcare_closing
            binding.petcareAddr.text = partner.petcare_addr

            // RecyclerView의 개별 Item Click event 처리
            binding.petcareName.setOnClickListener {
                Toast.makeText(
                    context,
                    "Clicked on ${partner.petcare_name}",
                    Toast.LENGTH_SHORT
                ).show()
                itemClickListener.onItemClick(partner)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(partner: PetcareItem)
    }
}