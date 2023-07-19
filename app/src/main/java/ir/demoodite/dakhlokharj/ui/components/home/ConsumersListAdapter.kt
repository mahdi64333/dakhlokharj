package ir.demoodite.dakhlokharj.ui.components.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.databinding.ItemConsumerBinding
import ir.demoodite.dakhlokharj.data.room.models.Resident

class ConsumersListAdapter(
    private val onClickListener: (Resident) -> Unit,
) : ListAdapter<Resident, ConsumersListAdapter.ViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemConsumerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item) {
            onClickListener(item)
        }
    }

    class ViewHolder(private val binding: ItemConsumerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(consumer: Resident, onClickListener: () -> Unit) {
            binding.chipName.text = consumer.name
            binding.chipName.setOnCloseIconClickListener {
                onClickListener()
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