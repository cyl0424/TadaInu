package com.mobileprogramming.tadainu.partnersFeat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobileprogramming.tadainu.databinding.PartnerItemBinding
import com.mobileprogramming.tadainu.model.ClusteredPartnerName

class ClusterClickAdapter(
    private val context: Context,
    private val clusteredPartnerNameList: List<ClusteredPartnerName>
) : RecyclerView.Adapter<ClusterClickAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = PartnerItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partner = clusteredPartnerNameList[position]
        holder.bind(partner)
    }

    override fun getItemCount(): Int {
        return clusteredPartnerNameList.size
    }

    inner class ViewHolder(private val binding: PartnerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(partner: ClusteredPartnerName) {
            binding.partnerElementName.text = partner.clusteredpartnerName
        }
    }
}
