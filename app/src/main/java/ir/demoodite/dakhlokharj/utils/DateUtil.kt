package ir.demoodite.dakhlokharj.utils

import saman.zamani.persiandate.PersianDate

object DateUtil {
    private val jalaliMonthsLength = listOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
    private val gregorianMonthsLength = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    fun validateJalaliDate(date: String): Boolean {
        val dateParts = date.split("/").map {
            it.toInt()
        }
        dateParts.forEach {
            if (it == 0) {
                return false
            }
        }
        if (dateParts[1] == 12) {
            return dateParts[2] <= if (PersianDate.isJalaliLeap(dateParts[0])) 30 else 29
        }
        return dateParts[2] <= jalaliMonthsLength[dateParts[1] - 1]
    }

    fun validateGregorianDate(date: String): Boolean {
        val dateParts = date.split("/").map {
            it.toInt()
        }
        dateParts.forEach {
            if (it == 0) {
                return false
            }
        }
        if (dateParts[1] == 2) {
            return dateParts[2] <= if (PersianDate.isGrgLeap(dateParts[0])) 29 else 28
        }
        return dateParts[2] <= gregorianMonthsLength[dateParts[1] - 1]
    }
}