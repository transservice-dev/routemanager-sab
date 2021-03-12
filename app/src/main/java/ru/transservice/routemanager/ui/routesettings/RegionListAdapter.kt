package ru.transservice.routemanager.ui.routesettings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.transservice.routemanager.data.local.RegionItem
import ru.transservice.routemanager.databinding.ItemRegionListBinding

class RegionListAdapter(val listener: (RegionItem) -> Unit) : RecyclerView.Adapter<RegionListAdapter.RegionItemViewHolder>() {

    var items: List<RegionItem> = listOf()

    class RegionItemViewHolder(val binding: ItemRegionListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RegionItem, listener: (RegionItem) -> Unit) {
            binding.tvName.text = item.name
            binding.tvName.setOnClickListener {
                listener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionItemViewHolder {
        val binding =
            ItemRegionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RegionItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegionItemViewHolder, position: Int) =
        holder.bind(items[position], listener)

    override fun getItemCount(): Int = items.size

    fun updateItems(data: List<RegionItem>) {

        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return items.size
            }

            override fun getNewListSize(): Int {
                return data.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].uid == data[newItemPosition].uid
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].hashCode() == data[newItemPosition].hashCode()
            }

        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items = data
        diffResult.dispatchUpdatesTo(this)

    }
}