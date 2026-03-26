package de.nulide.shiftcal.utils.pdf

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import java.text.DateFormatSymbols
import java.time.LocalDate

class PDFTitle {
    companion object {
        fun getTitle(month: LocalDate): Paragraph? {
            val months = DateFormatSymbols().months.toList()
            val year = month.year.toString()
            val monthValue = month.monthValue - 1
            val month = months[monthValue]
            val dateParagraph = Paragraph("$year $month")
                .setFontSize(30f)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8f)
            dateParagraph.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            return dateParagraph
        }
    }
}