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
    private val shareOnClickListener: (File, String) -> Unit,
    private val saveOnClickListener: (File, String) -> Unit,
    private val deleteOnClickListener: (File) -> Unit,
    private val activeArchiveOnClickListener: (File) -> Unit,
    private val newFilenameCallback: (File, newName: String) -> Unit,
) : ListAdapter<Pair<File, String>, DatabaseArchiveListAdapter.ViewHolder>(
    diffCallback
) {
    private var activeArchiveViewHolder: ViewHolder? = null
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
            alias = getItem(position).second,
            lastModified = getItem(position).first.lastModified(),
            shareOnClickListener = {
                shareOnClickListener(
                    getItem(position).first,
                    getItem(position).second
                )
            },
            saveOnClickListener = {
                saveOnClickListener(
                    getItem(position).first,
                    getItem(position).second
                )
            },
            deleteOnClickListener = { deleteOnClickListener(getItem(position).first) },
            activeArchiveOnClickListener = {
                activeArchiveViewHolder?.deactivate()
                activeArchiveOnClickListener(getItem(position).first)
                activeArchiveViewHolder = holder
            },
            renameCallback = { newFilenameCallback(getItem(position).first, it) },
            onStartEditing = {
                stopEditing()
                editingViewHolder = holder
                editingAlias = getItem(position).second
            },
        )

        if (getItem(position).second == activeArchiveAlias) {
            holder.activate()
            activeArchiveViewHolder = holder
        }
        if (getItem(position).second == editingAlias) {
            holder.startEditing(editingAlias)
            editingViewHolder = holder
        }
    }

    fun stopEditing() {
        editingViewHolder?.stopEditing()
        editingAlias = null
        editingViewHolder = null
    }

    class ViewHolder(private val binding: ItemArchivedDatabaseBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        private val context get() = binding.root.context
        private val dpUnit = UiUtil.dpToPixel(context, 1)
        private var alias: String = ""
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
        private val diffCallback = object : DiffUtil.ItemCallback<Pair<File, String>>() {
            override fun areItemsTheSame(
                oldItem: Pair<File, String>,
                newItem: Pair<File, String>,
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: Pair<File, String>,
                newItem: Pair<File, String>,
            ): Boolean {
                return oldItem.second == newItem.second && oldItem.first.lastModified() == newItem.first.lastModified()
            }

        }
    }
}