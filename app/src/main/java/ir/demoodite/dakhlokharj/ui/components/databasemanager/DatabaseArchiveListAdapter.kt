package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.text.InputFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.ItemArchivedDatabaseBinding
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil
import saman.zamani.persiandate.PersianDate
import java.io.File

class DatabaseArchiveListAdapter(
    var activeArchiveAlias: String,
    private val shareOnClickListener: (DatabaseArchive) -> Unit,
    private val saveOnClickListener: (DatabaseArchive) -> Unit,
    private val deleteOnClickListener: (DatabaseArchive) -> Unit,
    private val activeArchiveOnClickListener: (DatabaseArchive) -> Unit,
    private val newFilenameCallback: (DatabaseArchive, newName: String) -> Unit,
) : ListAdapter<DatabaseArchiveListAdapter.DatabaseArchive, DatabaseArchiveListAdapter.ViewHolder>(
    diffCallback
) {
    private var activeArchiveViewHolder: ViewHolder? = null
    private var editingArchivePosition = RecyclerView.NO_POSITION
    private var editingViewHolder: ViewHolder? = null
    private var editingAlias: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemArchivedDatabaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        if (holder == activeArchiveViewHolder) {
            activeArchiveAlias = holder.editingAlias
            holder.deactivate()
            activeArchiveViewHolder = null
        }

        if (holder == editingViewHolder) {
            editingAlias = holder.editingAlias
            holder.stopEditing()
            editingViewHolder = null
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            alias = getItem(position).alias,
            lastModified = getItem(position).file.lastModified(),
            isActive = getItem(position).alias == activeArchiveAlias,
            isEditing = holder.adapterPosition == editingArchivePosition,
            editingText = editingAlias,
            shareOnClickListener = { shareOnClickListener(getItem(position)) },
            saveOnClickListener = { saveOnClickListener(getItem(position)) },
            deleteOnClickListener = { deleteOnClickListener(getItem(position)) },
            activateArchiveOnClickListener = {
                stopEditing()
                activeArchiveViewHolder?.deactivate()
                activeArchiveOnClickListener(getItem(position))
            },
            renameCallback = {
                newFilenameCallback(getItem(position), it)
                editingAlias = null
            },
            onStartEditing = {
                stopEditing()
                editingAlias = null
                editingArchivePosition = holder.adapterPosition
                editingViewHolder = holder
            },
        )

        if (getItem(position).alias == activeArchiveAlias) {
            activeArchiveViewHolder = holder
        }
        if (holder.adapterPosition == editingArchivePosition) {
            editingViewHolder = holder
        }
    }

    fun stopEditing() {
        editingViewHolder?.stopEditing()
        editingAlias = null
        editingViewHolder = null
    }

    data class DatabaseArchive(
        var alias: String,
        var file: File,
    ) {
        override fun equals(other: Any?) =
            (other is DatabaseArchive)
                    && this.alias == other.alias
                    && this.file.absolutePath == other.file.absolutePath
                    && this.file.lastModified() == other.file.lastModified()

        override fun hashCode(): Int {
            var result = alias.hashCode()
            result = 31 * result + file.hashCode()
            return result
        }
    }

    class ViewHolder(private val binding: ItemArchivedDatabaseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context get() = binding.root.context
        private val dpUnit = UiUtil.dpToPixel(context, 1)
        var alias: String = ""
            set(value) {
                field = value
                binding.textInputEditTextArchiveName.setText(alias)
            }
        lateinit var renameCallback: (newName: String) -> Unit
        val editingAlias get() = binding.textInputEditTextArchiveName.text.toString().trim()

        init {
            binding.textInputEditTextArchiveName.filters = arrayOf(
                InputFilter.LengthFilter(24), FilenameInputFilter()
            )
        }

        fun bind(
            alias: String,
            lastModified: Long,
            isActive: Boolean,
            isEditing: Boolean,
            editingText: String?,
            shareOnClickListener: () -> Unit,
            saveOnClickListener: () -> Unit,
            deleteOnClickListener: () -> Unit,
            activateArchiveOnClickListener: () -> Unit,
            renameCallback: (newName: String) -> Unit,
            onStartEditing: () -> Unit,
        ) {
            this.alias = alias
            this.renameCallback = renameCallback
            if (isActive) {
                activate()
            } else {
                deactivate()
            }
            if (isEditing) {
                startEditing(editingText)
            } else {
                stopEditing()
            }
            binding.apply {
                tvLastModified.text = context.getString(
                    R.string.last_modified_template, LocaleHelper.formatLocalizedDate(
                        PersianDate(lastModified)
                    )
                )
                btnShareArchive.setOnClickListener {
                    shareOnClickListener()
                }
                btnSaveArchive.setOnClickListener {
                    saveOnClickListener()
                }
                btnDeleteArchive.setOnClickListener {
                    deleteOnClickListener()
                }
                btnActiveArchive.setOnClickListener {
                    activate()
                    activateArchiveOnClickListener()
                }
                textInputEditTextArchiveName.setOnLongClickListener {
                    onStartEditing()
                    startEditing()
                    it.requestFocus()
                    UiUtil.showKeyboard(it)
                    (it as EditText).setSelection(it.length())
                    true
                }
                textInputEditTextArchiveName.error = null
            }
        }

        private fun validateAndGetFilename(): String? {
            var errorFlag = false

            val filename = binding.textInputEditTextArchiveName.text.toString().trim()

            if (filename.isEmpty()) {
                binding.textInputLayoutArchiveName.error = context.getString(R.string.its_empty)
                UiUtil.removeErrorOnTextChange(binding.textInputEditTextArchiveName, false)
                errorFlag = true
            }

            return if (errorFlag) null else filename
        }

        private fun activate() {
            binding.btnActiveArchive.isEnabled = false
            val colorGreen = ContextCompat.getColor(context, R.color.green_add)
            binding.btnActiveArchive.drawable.setTint(colorGreen)
        }

        fun deactivate() {
            binding.btnActiveArchive.isEnabled = true
            val colorSemitransparentGray =
                ContextCompat.getColor(context, R.color.semitransparent_gray)
            binding.btnActiveArchive.drawable.setTint(colorSemitransparentGray)
        }

        private fun startEditing(editingText: String? = null) {
            binding.textInputLayoutArchiveName.apply {
                boxStrokeWidth = dpUnit
                endIconMode = TextInputLayout.END_ICON_CUSTOM
                setEndIconOnClickListener {
                    validateAndGetFilename()?.let {
                        stopEditing()
                        renameCallback(it)
                    }
                }
            }
            binding.textInputEditTextArchiveName.apply {
                setText(editingText ?: alias)
                isFocusable = true
                isFocusableInTouchMode = true
                isCursorVisible = true
            }
        }

        fun stopEditing() {
            binding.textInputLayoutArchiveName.apply {
                boxStrokeWidth = 0
                endIconMode = TextInputLayout.END_ICON_NONE
            }
            binding.textInputEditTextArchiveName.apply {
                setText(alias)
                UiUtil.hideKeyboard(this)
                isFocusable = false
                isFocusableInTouchMode = false
                isCursorVisible = false
            }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DatabaseArchive>() {
            override fun areItemsTheSame(
                oldItem: DatabaseArchive,
                newItem: DatabaseArchive,
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: DatabaseArchive,
                newItem: DatabaseArchive,
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}