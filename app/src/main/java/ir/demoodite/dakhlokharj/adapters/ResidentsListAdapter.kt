package ir.demoodite.dakhlokharj.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.ItemResidentBinding
import ir.demoodite.dakhlokharj.models.database.Resident
import ir.demoodite.dakhlokharj.utils.UiUtil

class ResidentsListAdapter() :
    ListAdapter<Resident, ResidentsListAdapter.ViewHolder>(diffCallback) {
    private var editing = false
    private var editingIndex = -1
    private var editingName: String? = null
    private var editingViewHolder: ViewHolder? = null
    lateinit var onActivationChangedListener: (resident: Resident, active: Boolean) -> Unit
    lateinit var onNameChangedListener: (resident: Resident, newName: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemResidentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), editingName)
        holder.setEditing(holder.adapterPosition == editingIndex)
        holder.itemView.setOnLongClickListener {
            if (editing) {
                editingViewHolder?.setEditing(false)
            }
            holder.setEditing(true)
            holder.setOnActivationChangedListener {
                onActivationChangedListener(getItem(position), it)
            }
            holder.setOnNameChangedListener {
                onNameChangedListener(getItem(position), it)
                holder.setEditing(false)
                editing = false
            }
            editingViewHolder = holder
            editingIndex = holder.adapterPosition
            editing = true
            true
        }
    }

    class ViewHolder(private var binding: ItemResidentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resident: Resident, editingName: String?) {
            binding.tvResidentName.text = resident.name
            binding.textInputEditTextResidentName.setText(editingName ?: resident.name)
            binding.checkBoxActive.isChecked = resident.active
        }

        fun setEditing(editing: Boolean) {
            binding.tvResidentName.isGone = editing
            binding.textInputLayoutResidentName.isVisible = editing
        }

        fun setOnActivationChangedListener(listener: (active: Boolean) -> Unit) {
            binding.checkBoxActive.setOnClickListener {
                listener(binding.checkBoxActive.isChecked)
            }
        }

        fun setOnNameChangedListener(listener: (newName: String) -> Unit) {
            binding.textInputLayoutResidentName.setEndIconOnClickListener {
                val name = binding.textInputEditTextResidentName.text.toString()
                if (name.isEmpty()) {
                    binding.textInputLayoutResidentName.error =
                        binding.root.context.getString(R.string.its_empty)
                    UiUtil.removeErrorOnType(binding.textInputEditTextResidentName)
                } else {
                    listener(name)
                }
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