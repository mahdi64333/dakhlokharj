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
    /**
     * Adapter position of the resident item that's being edited.
     */
    private var editingPosition = RecyclerView.NO_POSITION

    /**
     * [EditText] input text of the resident that's being edited. It can be used after
     * the [ViewHolder] gets recycled and comes back.
     */
    private var editingName: String? = null

    /**
     * [TextInputLayout] error text of the resident that's being edited. It can be used to after
     * the [ViewHolder] gets recycled and comes back.
     */
    private var editingError: String? = null

    /**
     * The [ViewHolder] of the resident Item that's being edited.
     */
    private var editingViewHolder: ViewHolder? = null

    /**
     * Gets called when activation state of a resident item changes.
     */
    lateinit var onActivationChangedListener: (resident: Resident, isActive: Boolean) -> Unit

    /**
     * Gets called after entering a new name for a resident item.
     */
    lateinit var onNameChangedListener: (resident: Resident, newName: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(binding = ItemResidentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), renameCallback = { resident, newName ->
            if (newName != null) {
                onNameChangedListener(resident, newName)
            }
        }, onActivationChanged = { resident, isActive ->
            onActivationChangedListener(resident, isActive)
        })
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        // Saving editing ViewHolder state and changing it to a ordinary ViewHolder
        if (holder == editingViewHolder) {
            // Saving the input value of the ViewHolder
            editingName = holder.editingName

            holder.stopEditing()
            editingViewHolder = null
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(resident = getItem(position),
            isEditing = editingPosition == holder.adapterPosition,
            onStartEditing = {
                // Changing the editing ViewHolder to this ViewHolder
                stopEditing()
                editingPosition = holder.adapterPosition
                editingViewHolder = holder
            })

        // Setting the editing ViewHolder to this ViewHolder if the adapter position matches
        if (holder.adapterPosition == editingPosition) {
            editingViewHolder = holder
            editingName?.let {
                holder.editingName = it
            }
            editingError?.let {
                editingViewHolder?.setError(it)
            }
        }
    }

    /**
     * Whether ListAdapter is in editing state or not.
     *
     * @return True if one of residents are being edited.
     */
    fun isEditing(): Boolean {
        return editingPosition != RecyclerView.NO_POSITION
    }

    /**
     * Stops editing of ListAdapter.
     */
    fun stopEditing() {
        editingViewHolder?.stopEditing()
        editingPosition = RecyclerView.NO_POSITION
        editingName = null
        editingError = null
        editingViewHolder = null
    }

    /**
     * Sets the error string of the editing resident item.
     *
     * @param errorMessage The message of error which will be put inside the [TextInputLayout].
     */
    fun setError(errorMessage: String) {
        editingError = errorMessage
        editingViewHolder?.setError(errorMessage)
    }

    class ViewHolder(
        private val binding: ItemResidentBinding,
        private val renameCallback: (Resident, newName: String?) -> Unit,
        private val onActivationChanged: (resident: Resident, active: Boolean) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var resident: Resident
        private val context get() = binding.root.context
        private val dpUnit = UiUtil.dpToPixel(context, 1)
        var editingName
            get() = binding.textInputEditTextResidentName.text.toString().trim()
            set(value) {
                binding.textInputEditTextResidentName.setText(value)
            }

        fun bind(
            resident: Resident,
            isEditing: Boolean,
            onStartEditing: () -> Unit,
        ) {
            this.resident = resident
            binding.apply {
                textInputEditTextResidentName.setText(resident.name)
                textInputEditTextResidentName.setOnLongClickListener {
                    startEditing(onStartEditing)
                    it.requestFocus()
                    UiUtil.showKeyboard(it)
                    (it as EditText).setSelection(it.length())
                    true
                }
                checkBoxActive.isChecked = resident.active
                checkBoxActive.setOnCheckedChangeListener { _, isChecked ->
                    onActivationChanged(resident, isChecked)
                }
                if (isEditing) {
                    startEditing(onStartEditing)
                }
            }
        }

        private fun startEditing(onStartEditing: () -> Unit) {
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

            return if (errorFlag) null
            else name
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