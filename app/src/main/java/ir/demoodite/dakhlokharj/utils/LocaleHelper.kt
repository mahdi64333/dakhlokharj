package ir.demoodite.dakhlokharj.utils

import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.*


object LocaleHelper {
    fun setLocaleByLanguage(lang: String) {
        val appLocaleList = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(appLocaleList)
    }

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