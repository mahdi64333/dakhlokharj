package ir.demoodite.dakhlokharj.ui.showcase

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Holds record of showcases show history.
 *
 * @param context Needed to create the [SharedPreferences] object to store data.
 */
class ShowcaseStatus(context: Context) {
    /**
     * [SharedPreferences] object of showcase records.
     */
    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    /**
     * Show history of a screen's showcase.
     *
     * @param screen The [Screen] to query for it's history.
     * @return True if showcase of the [screen] has been shown and false otherwise.
     */
    fun isShowcaseShown(screen: Screen): Boolean {
        return sharedPreferences.getBoolean(screen.name, false)
    }

    /**
     * Record end of a screen's showcase to query it later.
     *
     * @param screen The target [Screen].
     */
    fun recordShowcaseEnd(screen: Screen) {
        sharedPreferences.edit {
            putBoolean(screen.name, true)
        }
    }

    /**
     * Screens that have a showcase associated whit them.
     */
    enum class Screen {
        HOME,
        RESIDENTS,
    }

    companion object {
        private const val PREFERENCE_NAME = "showcase_preference"
    }
}