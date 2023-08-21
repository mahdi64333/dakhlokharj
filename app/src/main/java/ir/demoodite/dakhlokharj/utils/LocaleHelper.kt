package ir.demoodite.dakhlokharj.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToLong


object LocaleHelper {
    // Calendars months length
    private val jalaliMonthsLength = listOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
    private val gregorianMonthsLength = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    // Date formatters for different calendars
    private val persianDateFormat = PersianDateFormat("Y/m/d H:i")
    private val gregorianDateFormat = SimpleDateFormat("yyyy/MM/dd H:m", Locale("EN"))

    /**
     * The language of application. Changing this value will result in a change
     * for application's language.
     */
    var applicationLanguageCode: String? = null
        set(value) {
            field = value
            val appLocaleList = LocaleListCompat.forLanguageTags(field)
            AppCompatDelegate.setApplicationLocales(appLocaleList)
        }

    /**
     * Current application locale based on the application language.
     */
    val currentLocale
        get() = Locale(applicationLanguageCode ?: "EN")

    /**
     * Determines whether a given string is a valid jalali date or not.
     *
     * @param date The date in "YYYY/MM/DD" format
     * @return True if the string was a valid jalali date and false otherwise
     */
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

    /**
     * Determines whether a given string is a valid gregorian date or not.
     *
     * @param date The date in "YYYY/MM/DD" format
     * @return True if the string was a valid gregorian date and false otherwise
     */
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

    /**
     * Determines whether a given string is a valid date or not
     * based on application's locale.
     *
     * @param date The date in "YYYY/MM/DD" format
     * @return True if the string was a valid date and false otherwise
     */
    fun validateLocalizedDate(date: String): Boolean {
        return when (applicationLanguageCode) {
            "EN" -> validateGregorianDate(date)
            "FA" -> validateJalaliDate(date)
            else -> validateGregorianDate(date)
        }
    }

    /**
     * Parses a given string to a [PersianDate] object
     * based on application's locale.
     *
     * @param date The date in "YYYY/MM/DD" format
     * @return [PersianDate] object of parsed date
     */
    fun parseLocalizedDate(date: String): PersianDate {
        return when (applicationLanguageCode) {
            "EN" -> persianDateFormat.parseGrg(date, "yyyy/MM/dd")
            "FA" -> persianDateFormat.parse(date, "yyyy/MM/dd")
            else -> persianDateFormat.parseGrg(date, "yyyy/MM/dd")
        }
    }

    /**
     * Formats a date string. The date is based on application's language.
     *
     * @param timeStamp Unix timestamp
     * @return Date string in "YYYY/MM/DD" format
     */
    fun formatLocalizedDate(timeStamp: Long): String {
        return when (applicationLanguageCode) {
            "EN" -> gregorianDateFormat.format(timeStamp)
            "FA" -> persianDateFormat.format(PersianDate(timeStamp))
            else -> gregorianDateFormat.format(timeStamp)
        }
    }

    /**
     * Creates a localized price string based on application's language.
     */
    fun localizePrice(decimalFormat: DecimalFormat, price: Double): String {
        return if (applicationLanguageCode == "FA")
            decimalFormat.format(price.roundToLong())
        else
            decimalFormat.format(price)
    }
}