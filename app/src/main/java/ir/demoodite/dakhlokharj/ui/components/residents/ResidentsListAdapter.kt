package ir.demoodite.dakhlokharj.ui.components.residents

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.ItemResidentBinding
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.utils.UiUtil

class ResidentsListAdapter :
    ListAdapter<Resident, ResidentsListAdapter.ViewHolder>(diffCallback) {
    private var editing = false
    private var editingIndex = -1
    private var editingName: String? = null
    private var editingViewHolder: ViewHolder? = null
    lateinit var onActivationChangedListener: (resident: Resident, active: Boolean) -> Unit
    lateinit var onNameChangedListener: (resident: Resident, newName: String, editText: EditText, viewHolder: ViewHolder) -> Unit

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
        holder.setEditing(editing && position == editingIndex)
        holder.itemView.setOnLongClickListener {
            if (editing) {
                editingViewHolder?.setEditing(false)
            }
            holder.setEditing(true)
            editingViewHolder = holder
            holder.requestFocus()
            editingIndex = holder.adapterPosition
            editing = true
            true
        }
        holder.setOnActivationChangedListener {
            onActivationChangedListener(getItem(position), it)
        }
        holder.setOnNameChangedListener { newName, editText ->
            onNameChangedListener(getItem(position), newName, editText, holder)
            editing = false
        }
    }

    fun endEditing() {
        editing = false
        editingViewHolder?.setEditing(false)
    }

    class ViewHolder(private val binding: ItemResidentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resident: Resident, editingName: String?) {
            binding.tvResidentName.text = resident.name
            binding.textInputEditTextResidentName.setText(editingName ?: resident.name)
            binding.checkBoxActive.isChecked = resident.active
        }

        fun setEditing(editing: Boolean) {
            binding.tvResidentName.isGone = editing
            binding.textInputLayoutResidentName.isInvisible = !editing
        }

        fun setOnActivationChangedListener(listener: (active: Boolean) -> Unit) {
            binding.checkBoxActive.setOnCheckedChangeListener { _, isChecked ->
                listener(isChecked)
            }
        }

        fun setOnNameChangedListener(listener: (newName: String, editText: EditText) -> Unit) {
            binding.textInputLayoutResidentName.setEndIconOnClickListener {
                val name = binding.textInputEditTextResidentName.text.toString()
                if (name.isEmpty()) {
                    binding.textInputLayoutResidentName.error =
                        binding.root.context.getString(R.string.its_empty)
                    UiUtil.removeErrorOnType(binding.textInputEditTextResidentName)
                } else {
                    binding.tvResidentName.text = name
                    listener(name, binding.textInputEditTextResidentName)
                }
            }
        }

        fun requestFocus() {
            binding.textInputEditTextResidentName.apply {
                requestFocus()
                setSelection(text.toString().length)
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