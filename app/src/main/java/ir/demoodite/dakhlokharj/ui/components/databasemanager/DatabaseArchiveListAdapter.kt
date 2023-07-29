package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.text.InputFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.textfield.TextInputLayout
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.ItemArchivedDatabaseBinding
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil
import saman.zamani.persiandate.PersianDate
import java.io.File

class DatabaseArchiveListAdapter(
    var activeArchiveAlias: String,
    activeArchiveFile: File,
    private val shareOnClickListener: (File) -> Unit,
    private val saveOnClickListener: (File) -> Unit,
    private val deleteOnClickListener: (File) -> Unit,
    private val activeArchiveOnClickListener: (File) -> Unit,
    private val newFilenameCallback: (File, newName: String) -> Unit,
) : ListAdapter<File, DatabaseArchiveListAdapter.ViewHolder>(
    diffCallback
) {
    private var activeArchiveFileViewHolderPair = FileViewHolderPair(activeArchiveFile, null)
    private var editingProgressFileViewHolderPair = FileViewHolderPair(null, null)
    private var editingAlias: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemArchivedDatabaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder == activeArchiveFileViewHolderPair.viewHolder) {
            holder.deactivate()
            activeArchiveFileViewHolderPair.viewHolder = null
        }
        if (holder == editingProgressFileViewHolderPair.viewHolder) {
            editingAlias = holder.editingAlias
            holder.stopEditing()
            editingProgressFileViewHolderPair.viewHolder = null
        }

        holder.bind(
            alias = if (getItem(position) === activeArchiveFileViewHolderPair.file) activeArchiveAlias
            else getItem(position).nameWithoutExtension,
            lastModified = getItem(position).lastModified(),
            shareOnClickListener = { shareOnClickListener(getItem(position)) },
            saveOnClickListener = { saveOnClickListener(getItem(position)) },
            deleteOnClickListener = { deleteOnClickListener(getItem(position)) },
            activeArchiveOnClickListener = {
                activeArchiveFileViewHolderPair.viewHolder?.deactivate()
                activeArchiveFileViewHolderPair = FileViewHolderPair(getItem(position), holder)
                activeArchiveOnClickListener(getItem(position))
            },
            renameCallback = { newFilenameCallback(getItem(position), it) },
            onStartEditing = {
                stopEditing()
                editingProgressFileViewHolderPair = FileViewHolderPair(getItem(position), holder)
            },
        )

        if (getItem(position).nameWithoutExtension == activeArchiveFileViewHolderPair.file?.nameWithoutExtension) {
            holder.activate()
            activeArchiveFileViewHolderPair.viewHolder = holder
        }
        if (getItem(position).nameWithoutExtension == editingProgressFileViewHolderPair.file?.nameWithoutExtension) {
            holder.startEditing(editingAlias)
            editingProgressFileViewHolderPair.viewHolder = holder
        }
    }

    private fun stopEditing() {
        editingProgressFileViewHolderPair.viewHolder?.stopEditing()
    }

    class ViewHolder(private val binding: ItemArchivedDatabaseBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        private val context get() = binding.root.context
        private val dpUnit = UiUtil.dpToPixel(context, 1)
        lateinit var alias: String
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
            shareOnClickListener: () -> Unit,
            saveOnClickListener: () -> Unit,
            deleteOnClickListener: () -> Unit,
            activeArchiveOnClickListener: () -> Unit,
            renameCallback: (newName: String) -> Unit,
            onStartEditing: () -> Unit,
        ) {
            this.alias = alias
            this.renameCallback = renameCallback
            binding.apply {
                textInputEditTextArchiveName.setText(alias)
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
                    activeArchiveOnClickListener()
                    activate()
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

        fun activate() {
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

        fun startEditing(editingText: String? = null) {
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
        private val diffCallback = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(
                oldItem: File,
                newItem: File,
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: File,
                newItem: File,
            ): Boolean {
                return oldItem.nameWithoutExtension == newItem.nameWithoutExtension && oldItem.lastModified() == newItem.lastModified()
            }

        }
    }

    data class FileViewHolderPair(
        var file: File?,
        var viewHolder: ViewHolder?,
    )
}