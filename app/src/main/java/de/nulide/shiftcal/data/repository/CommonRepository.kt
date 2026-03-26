package de.nulide.shiftcal.data.repository

import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.factory.TimeFactory
import java.time.LocalDate
import java.time.YearMonth

open class CommonRepository(val db: ShiftSwiftDB, val rm: SCRepoManager) {
    val calId: Int
        get() {
            return rm.curCalId
        }

    fun postDataChange() {
        rm.postDataChange()
    }

    fun getStartEndMonth(year: Int, month: Int): Pair<LocalDate, LocalDate> {
        val start = TimeFactory.convYearMonthToLocalDate(YearMonth.of(year, month))
        val end = start.plusMonths(1).minusDays(1)
        return Pair(start, end)
    }

}