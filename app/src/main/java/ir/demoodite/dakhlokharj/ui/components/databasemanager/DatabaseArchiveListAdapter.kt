package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
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
        when (holder) {
            activeArchiveFileViewHolderPair.viewHolder -> {
                Log.i(javaClass.name, "Active viewHolder went out")
                holder.deactivate()
                activeArchiveFileViewHolderPair.viewHolder = null
            }
            editingProgressFileViewHolderPair.viewHolder -> {
                Log.i(javaClass.name, "Editing viewHolder went out")
                editingAlias = holder.editingAlias
                holder.stopEditing()
                editingProgressFileViewHolderPair.viewHolder = null
            }
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

        when (getItem(position).nameWithoutExtension) {
            activeArchiveFileViewHolderPair.file?.nameWithoutExtension -> {
                Log.i(javaClass.name, "Active viewHolder came in")
                holder.activate()
                activeArchiveFileViewHolderPair.viewHolder = holder
            }
            editingProgressFileViewHolderPair.file?.nameWithoutExtension -> {
                Log.i(javaClass.name, "Editing viewHolder came in")
                holder.startEditing(editingAlias)
                editingProgressFileViewHolderPair.viewHolder = holder
            }
        }
    }

    fun stopEditing() {
        editingProgressFileViewHolderPair.viewHolder?.stopEditing()
    }

    class ViewHolder(private val binding: ItemArchivedDatabaseBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        private val context get() = binding.root.context
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
            binding.apply {
                tvArchiveName.text = alias
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
                textInputLayoutArchiveName.setEndIconOnClickListener {
                    validateAndGetFilename()?.let {
                        renameCallback(it)
                    }
                }
                layoutArchiveNameLabel.setOnLongClickListener {
                    onStartEditing()
                    startEditing()
                    true
                }
            }
        }

        private fun validateAndGetFilename(): String? {
            var errorFlag = false

            val filename = binding.textInputEditTextArchiveName.text.toString().trim()

            if (filename.isEmpty()) {
                binding.textInputLayoutArchiveName.error = context.getString(R.string.its_empty)
                UiUtil.removeErrorOnTextChange(binding.textInputEditTextArchiveName)
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
            binding.textInputLayoutArchiveName.isInvisible = false
            binding.layoutArchiveNameLabel.isInvisible = true
            binding.textInputEditTextArchiveName.setText(editingText ?: binding.tvArchiveName.text)
        }

        fun stopEditing() {
            binding.textInputLayoutArchiveName.isInvisible = true
            binding.layoutArchiveNameLabel.isInvisible = false
            binding.textInputEditTextArchiveName.setText(binding.tvArchiveName.text)
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