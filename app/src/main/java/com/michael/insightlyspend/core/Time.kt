package com.michael.insightlyspend.core

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object Time {
    private fun calendar(): Calendar = Calendar.getInstance()

    fun nowMillis(): Long = System.currentTimeMillis()

    fun startOfNextMonthMillis(epochMillis: Long = nowMillis()): Long {
        val c = calendar()
        c.timeInMillis = epochMillis
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.add(Calendar.MONTH, 1)
        stripTime(c)
        return c.timeInMillis
    }

    fun startOfMonthMillis(epochMillis: Long = nowMillis()): Long {
        val c = calendar()
        c.timeInMillis = epochMillis
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun endOfMonthMillis(epochMillis: Long = nowMillis()): Long {
        val c = calendar()
        c.timeInMillis = epochMillis
        val maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        c.set(Calendar.DAY_OF_MONTH, maxDay)
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    fun startOfDayMillis(epochMillis: Long): Long {
        val c = calendar()
        c.timeInMillis = epochMillis
        stripTime(c)
        return c.timeInMillis
    }

    fun endOfDayMillis(epochMillis: Long): Long {
        val c = calendar()
        c.timeInMillis = epochMillis
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    fun monthKey(epochMillis: Long = nowMillis()): String {
        val c = calendar()
        c.timeInMillis = epochMillis
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", y, m)
    }

    fun lastNCalendarDaysStartsIncludingToday(n: Int): List<Long> {
        val zone = TimeZone.getDefault()
        val todayStart = Calendar.getInstance(zone, Locale.getDefault())
        todayStart.timeInMillis = nowMillis()
        stripTime(todayStart)
        return (n - 1 downTo 0).map { daysBack ->
            val copy = todayStart.clone() as Calendar
            copy.add(Calendar.DAY_OF_YEAR, -daysBack)
            copy.timeInMillis
        }
    }

    fun previousMonthRange(epochMillis: Long = nowMillis()): Pair<Long, Long> {
        val c = calendar()
        c.timeInMillis = epochMillis
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.add(Calendar.MONTH, -1)
        stripTime(c)
        val start = c.timeInMillis
        val endC = c.clone() as Calendar
        endC.set(Calendar.DAY_OF_MONTH, endC.getActualMaximum(Calendar.DAY_OF_MONTH))
        endC.set(Calendar.HOUR_OF_DAY, 23)
        endC.set(Calendar.MINUTE, 59)
        endC.set(Calendar.SECOND, 59)
        endC.set(Calendar.MILLISECOND, 999)
        val end = endC.timeInMillis
        return start to end
    }

    fun daysInMonth(epochMillis: Long = nowMillis()): Int {
        val c = calendar()
        c.timeInMillis = epochMillis
        return c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun dayOfMonth(epochMillis: Long = nowMillis()): Int {
        val c = calendar()
        c.timeInMillis = epochMillis
        return c.get(Calendar.DAY_OF_MONTH)
    }

    private fun stripTime(c: Calendar) {
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
    }
}
