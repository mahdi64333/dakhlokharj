package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.text.InputFilter
import android.text.Spanned

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