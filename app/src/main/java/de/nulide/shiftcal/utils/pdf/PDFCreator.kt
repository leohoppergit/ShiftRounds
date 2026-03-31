package de.nulide.shiftcal.utils.pdf

import android.content.Context
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.Document
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
import java.io.File
import java.time.LocalDate
import java.time.YearMonth


class PDFCreator {

    companion object {

        fun convertMonthToPDF(
            context: Context,
            sc: SCRepoManager,
            currentYearMonth: YearMonth
        ): Boolean {
            return convertRangeToPDF(
                context = context,
                sc = sc,
                start = currentYearMonth.atDay(1),
                end = currentYearMonth.atEndOfMonth()
            )
        }

        fun convertRangeToPDF(
            context: Context,
            sc: SCRepoManager,
            start: LocalDate,
            end: LocalDate
        ): Boolean {
            val allWDays = sc.workDays.getAll()
                .filter { !it.day.isBefore(start) && !it.day.isAfter(end) }

            if (allWDays.isEmpty()) {
                return false
            }

            val writer = PdfWriter(File(context.cacheDir, PDFHelper.PDF_FILE))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, PDFFooter(context, document))

            var currentMonth = YearMonth.from(start)
            val endMonth = YearMonth.from(end)
            var firstPage = true

            while (!currentMonth.isAfter(endMonth)) {
                val monthStart = currentMonth.atDay(1)
                val monthEnd = currentMonth.atEndOfMonth()
                val monthWorkDays = allWDays.filter { !it.day.isBefore(monthStart) && !it.day.isAfter(monthEnd) }

                if (monthWorkDays.isNotEmpty()) {
                    if (!firstPage) {
                        document.add(AreaBreak())
                    }
                    firstPage = false

                    document.add(PDFTitle.getTitle(monthStart))

                    val shiftMap = linkedMapOf<Int, Int>()
                    for (wday in monthWorkDays) {
                        shiftMap[wday.shiftId] = 1
                    }

                    document.add(
                        PDFShiftCalendarTable.getShiftCalendarTable(
                            context,
                            sc,
                            monthStart,
                            monthWorkDays
                        )
                    )

                    document.add(
                        PDFShiftInfoTable.getShiftInfoTable(
                            context,
                            sc,
                            shiftMap.keys.toMutableList()
                        )
                    )
                }

                currentMonth = currentMonth.plusMonths(1)
            }

            document.close()
            return !firstPage
        }


    }
}
