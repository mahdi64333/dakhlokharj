package ir.demoodite.dakhlokharj.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.textfield.TextInputLayout


object UiUtil {
    private const val DIALOG_BUTTONS_MARGIN = 22

    /**
     * Removes error of TextInputLayout.
     *
     * @param input the TextInputEditText
     * @param preserveErrorTextSpace preserve error label space after removing the error label
     */
    fun removeErrorOnType(input: EditText, preserveErrorTextSpace: Boolean = true) {
        val layout = input.parent.parent as TextInputLayout
        input.addTextChangedListener {
            layout.error = null
            layout.isErrorEnabled = preserveErrorTextSpace
        }
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun isNightModeOn(resources: Resources): Boolean {
        val nightModeFlags = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    fun setSweetAlertDialogNightMode(resources: Resources) {
        SweetAlertDialog.DARK_STYLE = isNightModeOn(resources)
    }

    fun fixSweetAlertDialogButtons(button: Button) {
        val margin = dpToPixel(button.context, DIALOG_BUTTONS_MARGIN)
        button.setPadding(0)
        button.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(margin, 0, margin, 0)
        }
    }

    private fun dpToPixel(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
