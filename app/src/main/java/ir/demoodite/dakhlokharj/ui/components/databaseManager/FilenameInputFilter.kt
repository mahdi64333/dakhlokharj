package ir.demoodite.dakhlokharj.ui.components.databaseManager

import android.text.InputFilter
import android.text.Spanned

/**
 * An [InputFilter] implementation for filename input.
 */
class FilenameInputFilter : InputFilter {
    private val reservedChars = "|\\?*<\":>+[]/'."

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int,
    ): CharSequence {
        return source?.filter { !reservedChars.contains(it) } ?: ""
    }
}