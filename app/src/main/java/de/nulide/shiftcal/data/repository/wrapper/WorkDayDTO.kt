package de.nulide.shiftcal.data.repository.wrapper

import de.nulide.shiftcal.data.factory.TimeFactory
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.WorkDay
import java.time.Duration
import java.time.LocalDateTime

data class WorkDayDTO(
    val wday: WorkDay,
    val shift: Shift
) {

    val isRunning: Boolean
        get() {
            val now = LocalDateTime.now()
            val start = TimeFactory.convLocalDateAndMinutesToLocalDateTime(
                wday.day,
                shift.startTime.timeInMinutes
            )
            var end = TimeFactory.convLocalDateAndMinutesToLocalDateTime(
                wday.day,
                shift.endTime.timeInMinutes
            )
            end = end.plusDays(shift.endDayOffset.toLong())
            return start <= now && now <= end
        }

    val amountFinished: Double
        get() {
            val now = LocalDateTime.now()
            val start = TimeFactory.convLocalDateAndMinutesToLocalDateTime(
                wday.day,
                shift.startTime.timeInMinutes
            )
            var end = TimeFactory.convLocalDateAndMinutesToLocalDateTime(
                wday.day,
                shift.endTime.timeInMinutes
            )
            end = end.plusDays(shift.endDayOffset.toLong())
            val total = Duration.between(start, end).toMillis().toDouble()
            val finished = Duration.between(start, now).toMillis().toDouble()
            return finished / total
        }

}
