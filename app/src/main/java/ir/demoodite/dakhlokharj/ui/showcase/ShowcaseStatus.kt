package ir.demoodite.dakhlokharj.ui.showcase

import android.content.Context
import androidx.core.content.edit

class ShowcaseStatus(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun isShowcaseShown(screen: Screen): Boolean {
        return sharedPreferences.getBoolean(screen.name, false)
    }

    fun recordShowcaseEnd(screen: Screen) {
        sharedPreferences.edit {
            putBoolean(screen.name, true)
        }
    }

    enum class Screen {
        HOME,
    }

    companion object {
        private const val PREFERENCE_NAME = "showcase_preference"
    }
}