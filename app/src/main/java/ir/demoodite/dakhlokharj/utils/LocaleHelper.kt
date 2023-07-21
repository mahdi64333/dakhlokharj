package ir.demoodite.dakhlokharj.utils

import android.content.res.Configuration
import android.os.Build
import java.util.*


object LocaleHelper {
    fun getCurrentLocale(configuration: Configuration): Locale {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale
        }
        return locale
    }
}