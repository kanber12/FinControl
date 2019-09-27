package by.kanber.fincontrol.util

import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.*

class DateUtil {
    companion object {
        private val formatter = SimpleDateFormat("dd MM yyyy HH mm", Locale.US)
        private val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        private val formatTime = SimpleDateFormat("HH:mm", Locale.US)

        fun getViewableTime(longTime: Long): String {
            var result = ""
            val timeArray = timeToString(longTime).split(" ")
            val time = DateTime(Date(longTime))
            val currTime = DateTime()

            if (isToday(currTime, time)) {
                result += "today"
            } else {
                if (isYesterday(currTime, time)) {
                    result += "yesterday"
                } else {
                    result += "${timeArray[0]}.${timeArray[1]}"

                    if (currTime.year > time.year) {
                        result += ".${timeArray[2]}"
                    }
                }
            }

            return result + " at ${timeArray[3]}:${timeArray[4]}"
        }

        fun getDate(date: Long): String = formatDate.format(Date(date))

        fun getTime(time: Long): String = formatTime.format(Date(time))

        fun getLongDate(date: Long) = formatDate.parse(getDate(date)).time

        fun getLongTime(date: Long) =  formatTime.parse(getTime(date)).time

        fun isTransactionExpired(currDate: Long, date: Long) = Days.daysBetween(DateTime(date), DateTime(currDate)).days >= 10

        private fun timeToString(time: Long): String = formatter.format(Date(time))

        private fun isToday(currTime: DateTime, time: DateTime): Boolean =
            currTime.dayOfMonth == time.dayOfMonth && currTime.monthOfYear == time.monthOfYear && currTime.year == time.year

        private fun isYesterday(currTime: DateTime, time: DateTime): Boolean =
            currTime.year - time.year == 1 && currTime.dayOfMonth == 1 && time.dayOfMonth == 31 && currTime.monthOfYear == 1 && time.monthOfYear == 12 ||
                    currTime.year == time.year && currTime.monthOfYear - time.monthOfYear == 1 && currTime.dayOfMonth == 1 && time.dayOfMonth == getDaysInMonth(
                time.monthOfYear,
                time.year
            ) ||
                    currTime.year == time.year && currTime.monthOfYear == time.monthOfYear && currTime.dayOfMonth - time.dayOfMonth == 1

        private fun getDaysInMonth(month: Int, year: Int): Int =
            (28 + ((month + Math.floor(month / 8.0)) % 2) + 2 % month +
                    Math.floor((1 + (1 - (year % 4 + 2) % (year % 4 + 1)) * ((year % 100 + 2) % (year % 100 + 1.0)) + (1 - (year % 400 + 2) % (year % 400 + 1))) / month) +
                    Math.floor(1.0 / month) - Math.floor(((1 - (year % 4 + 2) % (year % 4 + 1)) * ((year % 100 + 2) % (year % 100 + 1)) + (1 - (year % 400 + 2) % (year % 400 + 1.0))) / month)).toInt()
    }
}