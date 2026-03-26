package de.nulide.shiftcal.utils.pdf

import android.content.Context
import androidx.core.content.ContextCompat
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.BorderRadius
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.pdf.PDFHelper.Companion.color

class PDFShiftInfoTable {

    companion object {

        fun getShiftInfoTable(
            context: Context,
            sc: SCRepoManager,
            shifts: MutableList<Int>
        ): Table {

            val shiftInfoTable = Table(4)
            shiftInfoTable.setWidth(UnitValue.createPercentValue(100f))
            shiftInfoTable.setHorizontalAlignment(HorizontalAlignment.CENTER)
            shiftInfoTable.setMargin(16f)
            val shiftTitle = ContextCompat.getString(context, R.string.shift_creator_shift_title)
            val startsTitle = ContextCompat.getString(context, R.string.calendar_start_time_label)
                .replace(":", "")
            val endsTitle =
                ContextCompat.getString(context, R.string.calendar_end_time_label).replace(":", "")

            shiftInfoTable.addCell(cell(shiftTitle, title = true))
            shiftInfoTable.addCell(cell(" ", title = true))
            shiftInfoTable.addCell(cell(startsTitle, title = true))
            shiftInfoTable.addCell(cell(endsTitle, title = true))

            // Shift Info Entries

            for ((index, shiftId) in shifts.withIndex()) {
                val shift = sc.shifts.get(shiftId)
                val borderless = index == shifts.size - 1
                shiftInfoTable.addCell(
                    cell(
                        shift.shortName,
                        color = shift.color,
                        fixedSize = true,
                        borderless = borderless
                    )
                )
                shiftInfoTable.addCell(cell(shift.name, leftAlign = true, borderless = borderless))
                shiftInfoTable.addCell(cell(shift.startTime.toString(), borderless = borderless))
                val endText = buildString {
                    append(shift.endTime.toString())
                    if (shift.endDayOffset > 0) {
                        append(" (+").append(shift.endDayOffset).append(")")
                    }
                }
                shiftInfoTable.addCell(cell(endText, borderless = borderless))
            }
            return shiftInfoTable
        }


        fun cell(
            text: String,
            rowSpan: Boolean = false,
            color: Int = -1,
            leftAlign: Boolean = false,
            title: Boolean = false,
            fixedSize: Boolean = false,
            borderless: Boolean = false
        ): Cell {
            var whiteTextColor = false
            if (color != -1) {
                whiteTextColor = !ColorHelper.isTooBright(color)
            }

            var res = Cell()
            if (rowSpan) {
                res = Cell(1, 2)
            }

            val paragraph = Paragraph(text)
            paragraph.setFontSize(16f)

            if (leftAlign) {
                paragraph.setTextAlignment(TextAlignment.LEFT)
            } else {
                paragraph.setTextAlignment(TextAlignment.CENTER)
            }

            paragraph.setPadding(2f)

            if (title) {
                paragraph.setMargin(2f)
                paragraph.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                res.setBorder(SolidBorder(1.5f))
            }

            if (whiteTextColor) {
                paragraph.setFontColor(ColorConstants.WHITE)
            }

            res.add(paragraph)

            if (color != -1) {
                paragraph.setBackgroundColor(color(color))
                paragraph.setBorderRadius(BorderRadius(4f))
            }

            res.setBorderLeft(null)
            res.setBorderRight(null)
            res.setBorderTop(null)

            if (fixedSize) {
                val size = 25f
                res.setMinHeight(size)
                res.setMinWidth(size)
            }

            if (borderless) {
                res.setBorder(null)
                res.setNextRenderer(null)
            }

            return res
        }
    }
}
