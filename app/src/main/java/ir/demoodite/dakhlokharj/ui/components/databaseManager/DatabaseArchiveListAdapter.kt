package ir.demoodite.dakhlokharj.ui.components.databaseManager

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
import java.io.File

/**
 * A [ListAdapter] for database archives.
 *
 * @param activeArchiveAlias Alias of the active archive
 * @param shareOnClickListener A callback that gets called when pressing share button of an archive
 * @param saveOnClickListener A callback that gets called when pressing save button of an archive
 * @param deleteOnClickListener A callback that gets called when pressing delete button of an archive
 * @param activeArchiveOnClickListener A callback that gets called when a new archive gets activated
 * @param newFilenameCallback A callback that gets called alias of a archive changes
 */
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
    /**
     * Adapter position of the archive item that's being edited.
     */
    private var editingArchivePosition = RecyclerView.NO_POSITION

    /**
     * [EditText] input text of the archive item that's being edited. It can be used after
     * the [ViewHolder] gets recycled and comes back.
     */
    private var editingAlias: String? = null

    /**
     * The [ViewHolder] of the archive Item that's being edited.
     */
    private var editingViewHolder: ViewHolder? = null

    /**
     * The [ViewHolder] of the active archive item.
     */
    private var activeArchiveViewHolder: ViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemArchivedDatabaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        // Changing the active ViewHolder to a ordinary one
        if (holder == activeArchiveViewHolder) {
            holder.deactivate()
            activeArchiveViewHolder = null
        }

        // Saving editing ViewHolder state and changing it to a ordinary ViewHolder
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
            shareOnClickListener = {
                stopEditing()
                shareOnClickListener(getItem(position))
            },
            saveOnClickListener = {
                stopEditing()
                saveOnClickListener(getItem(position))
            },
            deleteOnClickListener = {
                stopEditing()
                deleteOnClickListener(getItem(position))
            },
            activateArchiveOnClickListener = {
                stopEditing()
                activeArchiveViewHolder?.deactivate()
                activeArchiveOnClickListener(getItem(position))
            },
            renameCallback = {
                stopEditing()
                newFilenameCallback(getItem(position), it)
            },
            onStartEditing = {
                stopEditing()
                editingArchivePosition = holder.adapterPosition
                editingViewHolder = holder
            },
        )

        if (getItem(position).alias == activeArchiveAlias) {
            activeArchiveViewHolder = holder
        }
        if (holder.adapterPosition == editingArchivePosition) {
            editingViewHolder = holder
            editingAlias?.let { holder.setEditTextAlias(it) }
        }
    }

    /**
     * Whether ListAdapter is in editing state or not.
     *
     * @return True if one of residents are being edited.
     */
    fun isEditing(): Boolean {
        return editingArchivePosition != RecyclerView.NO_POSITION
    }

    /**
     * Stops editing of ListAdapter.
     */
    fun stopEditing() {
        editingViewHolder?.stopEditing()
        editingAlias = null
        editingViewHolder = null
        editingArchivePosition = RecyclerView.NO_POSITION
    }

    data class DatabaseArchive(
        var alias: String,
        var file: File,
    ) {
        override fun equals(other: Any?) =
            (other is DatabaseArchive) && this.alias == other.alias && this.file.absolutePath == other.file.absolutePath && this.file.lastModified() == other.file.lastModified()

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
        private var alias: String = ""
            set(value) {
                // Setting the ViewHolder text and using application name if the alias is empty
                field = value.ifEmpty { context.getString(R.string.app_name) }
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
                startEditing()
            } else {
                stopEditing()
            }
            binding.apply {
                tvLastModified.text = context.getString(
                    R.string.last_modified_template, LocaleHelper.formatLocalizedDate(lastModified)
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

        private fun startEditing() {
            binding.textInputLayoutArchiveName.apply {
                boxStrokeWidth = dpUnit
                endIconMode = TextInputLayout.END_ICON_CUSTOM
                setEndIconOnClickListener {
                    validateAndGetFilenameOrNull()?.let {
                        stopEditing()
                        if (it != alias) {
                            renameCallback(it)
                        }
                    }
                }
            }
            binding.textInputEditTextArchiveName.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                isCursorVisible = true
            }
        }

        private fun validateAndGetFilenameOrNull(): String? {
            var errorFlag = false

            val filename = binding.textInputEditTextArchiveName.text.toString().trim()

            if (filename.isEmpty()) {
                binding.textInputLayoutArchiveName.error = context.getString(R.string.its_empty)
                UiUtil.removeErrorOnTextChange(binding.textInputEditTextArchiveName, false)
                errorFlag = true
            }

            return if (errorFlag) null else filename
        }

        fun setEditTextAlias(alias: String) {
            binding.textInputEditTextArchiveName.setText(alias)
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