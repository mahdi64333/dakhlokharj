package ir.demoodite.dakhlokharj.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.text.SimpleDateFormat
import java.util.*


object LocaleHelper {
    private val jalaliMonthsLength = listOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
    private val gregorianMonthsLength = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private val persianDateFormat = PersianDateFormat("Y/m/d H:i")

    var applicationLanguageCode: String? = null
        set(value) {
            field = value
            val appLocaleList = LocaleListCompat.forLanguageTags(field)
            AppCompatDelegate.setApplicationLocales(appLocaleList)
        }

    val currentLocale
        get() = Locale(applicationLanguageCode ?: "EN")
    private val gregorianDateFormat = SimpleDateFormat("yyyy/MM/dd H:m", Locale("EN"))

    private fun validateJalaliDate(date: String): Boolean {
        val dateParts = date.split("/").map {
            try {
                it.toInt()
            } catch (e: java.lang.Exception) {
                return false
            }
        }
        dateParts.forEach {
            if (it == 0) {
                return false
            }
        }
        if (dateParts[1] > 12) {
            return false
        }
        if (dateParts[1] == 12) {
            return dateParts[2] <= if (PersianDate.isJalaliLeap(dateParts[0])) 30 else 29
        }
        return dateParts[2] <= jalaliMonthsLength[dateParts[1] - 1]
    }

    private fun validateGregorianDate(date: String): Boolean {
        val dateParts = date.split("/").map {
            try {
                it.toInt()
            } catch (e: java.lang.Exception) {
                return false
            }
        }
        dateParts.forEach {
            if (it == 0) {
                return false
            }
        }
        if (dateParts[1] > 12) {
            return false
        }
        if (dateParts[1] == 2) {
            return dateParts[2] <= if (PersianDate.isGrgLeap(dateParts[0])) 29 else 28
        }
        return dateParts[2] <= gregorianMonthsLength[dateParts[1] - 1]
    }

    fun validateLocalizedDate(date: String): Boolean {
        return when (applicationLanguageCode) {
            "EN" -> validateGregorianDate(date)
            "FA" -> validateJalaliDate(date)
            else -> validateGregorianDate(date)
        }
    }

    fun parseLocalizedDate(dateText: String): PersianDate {
        return when (applicationLanguageCode) {
            "EN" -> persianDateFormat.parseGrg(dateText, "yyyy/MM/dd")
            "FA" -> persianDateFormat.parse(dateText, "yyyy/MM/dd")
            else -> persianDateFormat.parseGrg(dateText, "yyyy/MM/dd")
        }
    }

    fun formatLocalizedDate(persianDate: PersianDate): String {
        return when (applicationLanguageCode) {
            "EN" -> gregorianDateFormat.format(persianDate.time)
            "FA" -> persianDateFormat.format(persianDate)
            else -> gregorianDateFormat.format(persianDate.time)
        }
    }
}