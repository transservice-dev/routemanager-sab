package ru.transservice.routemanager.ui.routesettings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.transservice.routemanager.data.local.RegionItem
import ru.transservice.routemanager.data.local.VehicleItem
import ru.transservice.routemanager.databinding.ItemRegionListBinding
import ru.transservice.routemanager.databinding.ItemVehicleListBinding

class VehicleListAdapter(val listener: (VehicleItem) -> Unit) : RecyclerView.Adapter<VehicleListAdapter.VehicleItemViewHolder>() {

    var items: List<VehicleItem> = listOf()

    class VehicleItemViewHolder(val binding: ItemVehicleListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VehicleItem, listener: (VehicleItem) -> Unit) {
            binding.tvName.text = item.name
            binding.tvName.setOnClickListener {
                listener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleItemViewHolder {
        val binding =
            ItemVehicleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleItemViewHolder, position: Int) =
        holder.bind(items[position], listener)

    override fun getItemCount(): Int = items.size

    fun updateItems(data: List<VehicleItem>) {

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