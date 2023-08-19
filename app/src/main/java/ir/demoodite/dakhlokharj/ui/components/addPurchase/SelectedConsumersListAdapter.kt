package ir.demoodite.dakhlokharj.ui.components.addPurchase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.databinding.ItemConsumerBinding

class SelectedConsumersListAdapter(
    private val onCloseIconClickListener: (Resident) -> Unit,
) : ListAdapter<Resident, SelectedConsumersListAdapter.ViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemConsumerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            consumer = getItem(position),
            onCloseIconClickListener = { onCloseIconClickListener(getItem(position)) }
        )
    }

    class ViewHolder(private val binding: ItemConsumerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(consumer: Resident, onCloseIconClickListener: () -> Unit) {
            binding.chipName.text = consumer.name
            binding.chipName.setOnCloseIconClickListener {
                onCloseIconClickListener()
            }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Resident>() {
            override fun areItemsTheSame(oldItem: Resident, newItem: Resident): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Resident, newItem: Resident): Boolean {
                return oldItem == newItem
            }
        }
    }
}