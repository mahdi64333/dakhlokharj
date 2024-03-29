package ir.demoodite.dakhlokharj.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.textfield.TextInputLayout
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.Resident


object UiUtil {
    private const val DIALOG_BUTTONS_MARGIN = 22

    /**
     * Removes error of [TextInputLayout] after any change to the [EditText] content.
     *
     * @param input the TextInputEditText
     * @param preserveErrorTextSpace preserve error label space after removing the error label
     */
    fun removeErrorOnTextChange(input: EditText, preserveErrorTextSpace: Boolean = true) {
        val layout = input.parent.parent as TextInputLayout
        input.addTextChangedListener {
            layout.error = null
            layout.isErrorEnabled = preserveErrorTextSpace
        }
    }

    /**
     * Hides the soft keyboard focused on the view.
     *
     * @param view The view that is in focus
     */
    fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Shows the soft keyboard for the given view.
     *
     * @param view The target of showing soft keyboard
     */
    fun showKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun isNightModeOn(resources: Resources): Boolean {
        val nightModeFlags = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Sets the [SweetAlertDialog] class dark or light style based on whether phone is on
     * night mode or not.
     */
    fun setSweetAlertDialogNightMode(resources: Resources) {
        SweetAlertDialog.DARK_STYLE = isNightModeOn(resources)
    }

    /**
     * Fixes [SweetAlertDialog]'s button paddings and horizontal margins.
     */
    fun fixSweetAlertDialogButton(button: Button) {
        val margin = dpToPixel(button.context, DIALOG_BUTTONS_MARGIN)
        button.setPadding(0)
        button.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(margin, 0, margin, 0)
        }
    }

    /**
     * Converts dp unit to pixel
     */
    fun dpToPixel(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    /**
     * Shows a [SweetAlertDialog] with a of all resident names as consumers of a purchase.
     */
    fun createAndShowConsumersSweetDialog(
        context: Context,
        consumers: List<Resident>,
    ): SweetAlertDialog {
        return SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE).apply {
            val consumerNames = consumers.map {
                if (it.deleted) {
                    "${it.name} (${context.getString(R.string.deleted)})"
                } else {
                    it.name
                }
            }
            titleText = context.getString(R.string.consumers)
            val listView = ListView(context)
            val arrayAdapter = ArrayAdapter(
                context, android.R.layout.simple_list_item_1, consumerNames
            )
            listView.adapter = arrayAdapter
            listView.selector = ColorDrawable(Color.TRANSPARENT)
            setCustomView(listView)
            confirmText = context.getString(R.string.confirm)
            show()
            getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0)
        }
    }
}
