package com.mobileprogramming.tadainu.myPetFeat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.ShotItemBinding
import com.mobileprogramming.tadainu.myPetFeat.model.ShotItem

class ShotListAdapter(context: Context, private val shotList: ArrayList<ShotItem>) :
    ArrayAdapter<ShotItem>(context, R.layout.shot_item, shotList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ShotItemBinding

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            binding = ShotItemBinding.inflate(inflater, parent, false)
            binding.root.tag = binding
        } else {
            binding = convertView.tag as ShotItemBinding
        }

        val shotItem = getItem(position) as ShotItem
        binding.shotKind.text = shotItem.task
        binding.shotDate.text = shotItem.date

        return binding.root
    }
}
