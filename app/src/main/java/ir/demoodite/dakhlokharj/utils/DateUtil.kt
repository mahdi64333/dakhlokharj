package ir.demoodite.dakhlokharj.utils

import saman.zamani.persiandate.PersianDate

object DateUtil {
    fun validateDate(date: String): Boolean {
        val dateParts = date.split("/").map {
            it.toInt()
        }
        dateParts.forEach {
            if (it == 0) {
                return false
            }
        }
        if (dateParts[1] > 12) {
            return false
        }
        if (dateParts[1] <= 6) {
            return dateParts[2] <= 31
        }
        if (dateParts[1] <= 11) {
            return dateParts[2] <= 30
        }
        if (dateParts[1] <= 12) {
            return dateParts[2] <= if (PersianDate.isJalaliLeap(dateParts[0])) 30 else 29
        }
        return true
    }
}