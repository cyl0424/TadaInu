package com.mobileprogramming.tadainu.partnersFeat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobileprogramming.tadainu.databinding.FragmentPartnersListItemBinding
import com.mobileprogramming.tadainu.model.PetcareItem

class PartnersListAdapter(val petcareList: MutableList<PetcareItem>) : RecyclerView.Adapter<PartnersListAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnersListAdapter.CustomViewHolder {
        val binding = FragmentPartnersListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartnersListAdapter.CustomViewHolder, position: Int) {
        val petcareItem = petcareList[position]
        holder.bind(petcareItem)
    }

    override fun getItemCount(): Int {
        return petcareList.size
    }

    class CustomViewHolder(private val binding: FragmentPartnersListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(petcareItem: PetcareItem) {
            binding.apply {
                petcatrType.text = petcareItem.petcare_type
                petcareName.text = petcareItem.petcare_name
                petcareOpening.text = petcareItem.petcare_opening
                petcareClosing.text = petcareItem.petcare_closing
                petcareAddr.text = petcareItem.petcare_addr

                // Load image using Glide
                Glide.with(root.context)
                    .load(petcareItem.petcare_img)
                    .into(petcareImg)
            }
        }
    }
}
