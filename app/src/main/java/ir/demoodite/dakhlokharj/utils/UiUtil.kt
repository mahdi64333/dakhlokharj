package ir.demoodite.dakhlokharj.utils

import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout


object UiUtil {

    /**
     * Removes error of TextInputLayout.
     *
     * @param input the TextInputEditText
     */
    fun removeErrorOnType(input: EditText) {
        val layout = input.parent.parent as TextInputLayout
        input.addTextChangedListener {
            layout.error = null
        }
    }
}
