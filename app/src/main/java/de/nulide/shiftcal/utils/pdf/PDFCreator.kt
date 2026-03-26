package de.nulide.shiftcal.utils.pdf

import android.content.Context
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent
import com.itextpdf.layout.Document
import de.nulide.shiftcal.data.repository.SCRepoManager
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
            val wdays = sc.workDays.getWorkDaysOfMonth(
                currentYearMonth.year,
                currentYearMonth.monthValue
            )
            val writer = PdfWriter(File(context.cacheDir, PDFHelper.PDF_FILE))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, PDFFooter(context, document))

            var month = LocalDate.now()
            //Month Year
            if (wdays.isNotEmpty()) {
                month = wdays[0].day
            } else {
                return false
            }
            month = month.withDayOfMonth(1)

            // Title
            document.add(PDFTitle.getTitle(month))


            // Prepare work day data
            val shiftMap = linkedMapOf<Int, Int>()

            for (wday in wdays) {
                shiftMap[wday.shiftId] = 1
            }

            // Shift Calendar Table
            document.add(
                PDFShiftCalendarTable.getShiftCalendarTable(
                    context,
                    sc,
                    month,
                    wdays
                )
            )

            // Shift Info Table Header
            document.add(
                PDFShiftInfoTable.getShiftInfoTable(
                    context,
                    sc,
                    shiftMap.keys.toMutableList()
                )
            )

            document.close()
            return true
        }


    }
}