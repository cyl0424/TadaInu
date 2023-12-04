package com.mobileprogramming.tadainu.myPetFeat.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobileprogramming.tadainu.databinding.ShotItemBinding
import com.mobileprogramming.tadainu.model.ShotItem

class ShotListAdapter(
    private val context: Context,
    private val shotList: List<ShotItem>
) : RecyclerView.Adapter<ShotListAdapter.ShotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShotViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ShotItemBinding.inflate(inflater, parent, false)
        return ShotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShotViewHolder, position: Int) {
        val shotItem = shotList[position]
        holder.bind(shotItem)
    }

    override fun getItemCount(): Int {
        return shotList.size
    }

    inner class ShotViewHolder(private val binding: ShotItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(shotItem: ShotItem) {
            binding.shotName.text = shotItem.shotName
            binding.num.text = shotItem.shotNum
            binding.date.text = shotItem.date
        }
    }
}
