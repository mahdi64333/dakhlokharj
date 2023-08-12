package ir.demoodite.dakhlokharj.ui.components.residents

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.databinding.ItemResidentBinding
import ir.demoodite.dakhlokharj.utils.UiUtil

class ResidentsListAdapter : ListAdapter<Resident, ResidentsListAdapter.ViewHolder>(diffCallback) {
    private var editingPosition = RecyclerView.NO_POSITION
    private var editingName: String? = null
    private var editingViewHolder: ViewHolder? = null
    lateinit var onActivationChangedListener: (resident: Resident, active: Boolean) -> Unit
    lateinit var onNameChangedListener: (resident: Resident, newName: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        return ViewHolder(
            binding = ItemResidentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), getValidatedNameOrNull = { nameTextInputEditText, nameTextInputLayout ->
                if (nameTextInputEditText.text.toString().isEmpty()) {
                    nameTextInputLayout.error = context.getString(R.string.its_empty)
                    UiUtil.removeErrorOnTextChange(nameTextInputEditText, false)
                    null
                } else if (currentList.find {
                        it.name == nameTextInputEditText.text.toString().trim()
                    } != null) {
                    nameTextInputLayout.error =
                        context.getString(R.string.there_is_a_resident_with_this_name)
                    UiUtil.removeErrorOnTextChange(nameTextInputEditText, false)
                    null
                } else {
                    nameTextInputEditText.text.toString().trim()
                }
            },
            renameCallback = { resident, newName ->
                editingPosition = RecyclerView.NO_POSITION
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
        }
    }

    fun stopEditing() {
        editingPosition = RecyclerView.NO_POSITION
        editingViewHolder?.stopEditing()
    }

    class ViewHolder(
        private val binding: ItemResidentBinding,
        private val getValidatedNameOrNull: (TextInputEditText, TextInputLayout) -> String?,
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
                    val validatedName = if (editingName == resident.name) {
                        null
                    } else {
                        getValidatedNameOrNull(
                            binding.textInputEditTextResidentName,
                            binding.textInputLayoutResidentName
                        )
                    }
                    UiUtil.hideKeyboard(binding.textInputEditTextResidentName)
                    renameCallback(resident, validatedName)
                    stopEditing()
                }
            }
            binding.textInputEditTextResidentName.apply {
                setText(editingText ?: resident.name)
                isFocusable = true
                isFocusableInTouchMode = true
                isCursorVisible = true
            }
        }

        fun stopEditing() {
            binding.textInputLayoutResidentName.apply {
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