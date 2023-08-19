package ir.demoodite.dakhlokharj.ui.components.residents

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.databinding.ItemResidentBinding
import ir.demoodite.dakhlokharj.utils.UiUtil

class ResidentsListAdapter : ListAdapter<Resident, ResidentsListAdapter.ViewHolder>(diffCallback) {
    private var editingPosition = RecyclerView.NO_POSITION
    private var editingName: String? = null
    private var editingError: String? = null
    private var editingViewHolder: ViewHolder? = null
    lateinit var onActivationChangedListener: (resident: Resident, active: Boolean) -> Unit
    lateinit var onNameChangedListener: (resident: Resident, newName: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            binding = ItemResidentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            renameCallback = { resident, newName ->
                if (newName != null) {
                    onNameChangedListener(resident, newName)
                }
            }
        )
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        if (holder == editingViewHolder) {
            editingName = holder.editingName
            holder.stopEditing()
            editingViewHolder = null
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(resident = getItem(position),
            onActivationChanged = onActivationChangedListener,
            isEditing = editingPosition == holder.adapterPosition,
            editingName = editingName,
            onStartEditing = {
                stopEditing()
                editingPosition = holder.adapterPosition
                editingViewHolder = holder
            })

        if (holder.adapterPosition == editingPosition) {
            editingViewHolder = holder
            editingError?.let {
                editingViewHolder?.setError(it)
            }
        }
    }

    fun isEditing(): Boolean {
        return editingPosition != RecyclerView.NO_POSITION
    }

    fun stopEditing() {
        editingViewHolder?.stopEditing()
        editingPosition = RecyclerView.NO_POSITION
        editingName = null
        editingError = null
        editingViewHolder = null
    }

    fun setError(errorMessage: String) {
        editingError = errorMessage
        editingViewHolder?.setError(errorMessage)
    }

    class ViewHolder(
        private val binding: ItemResidentBinding,
        private val renameCallback: (Resident, newName: String?) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var resident: Resident
        private val context get() = binding.root.context
        private val dpUnit = UiUtil.dpToPixel(context, 1)
        val editingName get() = binding.textInputEditTextResidentName.text.toString().trim()

        fun bind(
            resident: Resident,
            onActivationChanged: (resident: Resident, active: Boolean) -> Unit,
            isEditing: Boolean,
            editingName: String?,
            onStartEditing: () -> Unit,
        ) {
            this.resident = resident
            binding.apply {
                textInputEditTextResidentName.setText(resident.name)
                if (isEditing) {
                    startEditing(editingName, onStartEditing)
                }
                textInputEditTextResidentName.setOnLongClickListener {
                    startEditing(null, onStartEditing)
                    it.requestFocus()
                    UiUtil.showKeyboard(it)
                    (it as EditText).setSelection(it.length())
                    true
                }
                checkBoxActive.isChecked = resident.active
                checkBoxActive.setOnCheckedChangeListener { _, isChecked ->
                    onActivationChanged(resident, isChecked)
                }
            }
        }

        private fun startEditing(editingText: String? = null, onStartEditing: () -> Unit) {
            onStartEditing()
            binding.textInputLayoutResidentName.apply {
                boxStrokeWidth = dpUnit
                endIconMode = TextInputLayout.END_ICON_CUSTOM
                setEndIconOnClickListener {
                    getValidatedNameOrNull()?.let { validatedName ->
                        if (validatedName != resident.name) {
                            renameCallback(resident, validatedName)
                        } else {
                            stopEditing()
                        }
                    }
                }
            }
            binding.textInputEditTextResidentName.apply {
                setText(editingText ?: resident.name)
                isFocusable = true
                isFocusableInTouchMode = true
                isCursorVisible = true
            }
        }

        private fun getValidatedNameOrNull(): String? {
            var errorFlag = false
            val name = binding.textInputEditTextResidentName.text.toString().trim()

            if (name.isEmpty()) {
                binding.textInputLayoutResidentName.error = context.getString(R.string.its_empty)
                UiUtil.removeErrorOnTextChange(binding.textInputEditTextResidentName, false)
                errorFlag = true
            }

            return if (errorFlag)
                null
            else
                name
        }

        fun setError(errorMessage: String) {
            binding.textInputLayoutResidentName.error = errorMessage
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextResidentName, false)
        }

        fun stopEditing() {
            binding.textInputLayoutResidentName.apply {
                error = null
                boxStrokeWidth = 0
                endIconMode = TextInputLayout.END_ICON_NONE
            }
            binding.textInputEditTextResidentName.apply {
                setText(resident.name)
                UiUtil.hideKeyboard(this)
                isFocusable = false
                isFocusableInTouchMode = false
                isCursorVisible = false
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