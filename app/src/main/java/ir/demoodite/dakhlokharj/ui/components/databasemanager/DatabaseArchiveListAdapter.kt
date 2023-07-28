package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.text.InputFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.ItemArchivedDatabaseBinding
import ir.demoodite.dakhlokharj.utils.UiUtil
import java.io.File

class DatabaseArchiveListAdapter(
    activeArchive: File,
    private val shareOnClickListener: (File) -> Unit,
    private val saveOnClickListener: (File) -> Unit,
    private val deleteOnClickListener: (File) -> Unit,
    private val activeArchiveOnClickListener: (File) -> Unit,
    private val newFilenameCallback: (File, newName: String) -> Unit,
) : ListAdapter<File, DatabaseArchiveListAdapter.ViewHolder>(
    diffCallback
) {
    private var activeArchiveFileViewHolderPair = FileViewHolderPair(activeArchive, null)
    private var editingProgressFileViewHolderPair = FileViewHolderPair(null, null)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemArchivedDatabaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItem(position).nameWithoutExtension) {
            activeArchiveFileViewHolderPair.file?.nameWithoutExtension ->
                activeArchiveFileViewHolderPair.viewHolder = holder
            editingProgressFileViewHolderPair.file?.nameWithoutExtension ->
                editingProgressFileViewHolderPair.viewHolder = holder
        }
        when (holder) {
            activeArchiveFileViewHolderPair.viewHolder ->
                activeArchiveFileViewHolderPair.viewHolder = null
            editingProgressFileViewHolderPair.viewHolder ->
                editingProgressFileViewHolderPair.viewHolder = null
        }

        holder.bind(
            file = getItem(position),
            shareOnClickListener = { shareOnClickListener(getItem(position)) },
            saveOnClickListener = { saveOnClickListener(getItem(position)) },
            deleteOnClickListener = { deleteOnClickListener(getItem(position)) },
            activeArchiveOnClickListener = {
                activeArchiveFileViewHolderPair.viewHolder?.deactivate()
                holder.activate()
                activeArchiveFileViewHolderPair = FileViewHolderPair(getItem(position), holder)
                activeArchiveOnClickListener(getItem(position))
            },
            renameCallback = { newFilenameCallback(getItem(position), it) },
            onStartEditing = {
                stopEditing()
                holder.startEditing()
                editingProgressFileViewHolderPair = FileViewHolderPair(getItem(position), holder)
            },
        )
    }

    fun stopEditing() {
        editingProgressFileViewHolderPair.viewHolder?.stopEditing()
    }

    class ViewHolder(private val binding: ItemArchivedDatabaseBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        private val context get() = binding.root.context

        init {
            binding.textInputEditTextArchiveName.filters = arrayOf(
                InputFilter.LengthFilter(24),
                FilenameInputFilter()
            )
        }

        fun bind(
            file: File,
            shareOnClickListener: () -> Unit,
            saveOnClickListener: () -> Unit,
            deleteOnClickListener: () -> Unit,
            activeArchiveOnClickListener: () -> Unit,
            renameCallback: (newName: String) -> Unit,
            onStartEditing: () -> Unit,
        ) {
            binding.apply {
                tvArchiveName.text = file.nameWithoutExtension
                textInputEditTextArchiveName.setText(file.nameWithoutExtension)
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
                }
                textInputLayoutArchiveName.setEndIconOnClickListener {
                    validateAndGetFilename()?.let {
                        renameCallback(it)
                    }
                }
                layoutArchiveName.setOnLongClickListener {
                    onStartEditing()
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

        fun startEditing() {
            binding.textInputLayoutArchiveName.isInvisible = false
        }

        fun stopEditing() {
            binding.textInputLayoutArchiveName.isInvisible = true
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
                return oldItem.nameWithoutExtension == newItem.nameWithoutExtension
                        && oldItem.lastModified() == newItem.lastModified()
            }

        }
    }

    data class FileViewHolderPair(
        var file: File?,
        var viewHolder: ViewHolder?,
    )
}