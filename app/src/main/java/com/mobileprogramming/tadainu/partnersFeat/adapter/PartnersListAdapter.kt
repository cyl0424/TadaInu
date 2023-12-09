package com.mobileprogramming.tadainu.partnersFeat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobileprogramming.tadainu.databinding.FragmentPartnersListItemBinding
import com.mobileprogramming.tadainu.model.PetcareItem

class PartnersListAdapter(val petcareList: MutableList<PetcareItem>) :
    RecyclerView.Adapter<PartnersListAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PartnersListAdapter.CustomViewHolder {
        val binding = FragmentPartnersListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartnersListAdapter.CustomViewHolder, position: Int) {
        val petcareItem = petcareList[position]
        holder.bind(petcareItem)
    }

    override fun getItemCount(): Int {
        return petcareList.size
    }

    inner class CustomViewHolder(private val binding: FragmentPartnersListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isExpanded = false

        init {
            // Add click listener to the entire item view
            binding.root.setOnClickListener {
                toggleVisibility()
            }

            // Add click listener to the '접기' button
            binding.foldButton.setOnClickListener {
                toggleVisibility()
            }
        }

        private fun toggleVisibility() {
            // Toggle the visibility of the 'invisible' section
            val invisibleLayout = binding.invisible
            isExpanded = !isExpanded
            invisibleLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

            Log.d("ITM", "Visibility is now ${if (isExpanded) "VISIBLE" else "GONE"}")
        }


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

    // Inside your PartnersListAdapter class
    fun updateList(newList: List<PetcareItem>) {
        petcareList.clear()
        petcareList.addAll(newList)
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<PetcareItem>() {
        override fun areItemsTheSame(oldItem: PetcareItem, newItem: PetcareItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PetcareItem, newItem: PetcareItem): Boolean {
            return oldItem == newItem
        }
    }
}