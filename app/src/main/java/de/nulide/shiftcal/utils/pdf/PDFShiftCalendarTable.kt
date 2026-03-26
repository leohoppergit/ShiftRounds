package de.nulide.shiftcal.utils.pdf

import android.content.Context
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.IBlockElement
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.BorderRadius
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.kizitonwose.calendar.core.daysOfWeek
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.WorkDay
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.pdf.PDFHelper.Companion.color
import java.time.DayOfWeek
import java.time.LocalDate

class PDFShiftCalendarTable {

    companion object {

        fun getShiftCalendarTable(
            context: Context,
            sc: SCRepoManager,
            month: LocalDate,
            wdays: List<WorkDay>
        ): Table {
            val settings = SettingsRepository.getInstance(context)

            val daysTable = Table(7)
            daysTable.setBorderRadius(BorderRadius(32f))
            daysTable.setMargin(16f)
            daysTable.setHorizontalAlignment(HorizontalAlignment.CENTER)

            val firstDayOfWeekIndex = settings.getInt(Settings.START_OF_WEEK)
            val maxDaysOfMonth = month.month.maxLength()
            var startDayOfMonth = month.dayOfWeek.value - 1
            if (startDayOfMonth == -1) {
                startDayOfMonth = 6
            }

            val daysBeforeStart = (startDayOfMonth - firstDayOfWeekIndex + 7) % 7

            val daysOfWeek = daysOfWeek(DayOfWeek.of(firstDayOfWeekIndex + 1))

            // Add WeekDays
            for (dayOfWeek in daysOfWeek) {
                val cell = cell(dayOfWeek.name.substring(0, 3), bold = true, borderless = true)
                daysTable.addCell(cell)
            }

            //Add Spacer
            repeat(daysBeforeStart) {
                daysTable.addCell(cell("", borderless = true))
            }


            // Add Days
            for (day in 1..maxDaysOfMonth) {
                val text = day.toString()
                var shift1: Shift? = null
                var shift2: Shift? = null
                for (wday in wdays) {
                    if (wday.day.dayOfMonth == day) {
                        if (shift1 == null) {
                            shift1 = sc.shifts.get(wday.shiftId)
                        } else {
                            shift2 = sc.shifts.get(wday.shiftId)
                        }
                    }
                }
                val cell: Cell = cell(text, shift1, shift2)

                daysTable.addCell(cell)
            }


            daysTable.setNextRenderer(RoundedBorderTableRenderer(daysTable, 8.0))
            return daysTable
        }

        private fun cell(
            text: String,
            shift1: Shift? = null,
            shift2: Shift? = null,
            bold: Boolean = false,
            borderless: Boolean = false
        ): Cell {
            // Cell
            val res = Cell()

            if (!bold) {
                val size = 50f
                res.setMinHeight(size)
                res.setMinWidth(size)
            }

            res.add(titleParagraph(text, bold, shift1?.color, shift2?.color))

            val shiftDayTable = Table(2)
            shiftDayTable.setHorizontalAlignment(HorizontalAlignment.CENTER)
            shiftDayTable.setWidth(UnitValue.createPercentValue(100f))
            for (shift in listOf(shift1, shift2)) {
                if (shift != null) {
                    val shiftParagraph = Paragraph(shift.shortName)
                    shiftParagraph.setFontSize(10f)
                    shiftParagraph.setTextAlignment(TextAlignment.CENTER)
                    shiftParagraph.setPadding(2f)
                    if (!ColorHelper.isTooBright(shift.color)) {
                        shiftParagraph.setFontColor(ColorConstants.WHITE)
                    } else {
                        shiftParagraph.setFontColor(ColorConstants.BLACK)
                    }
                    val cell = emptyCell(shiftParagraph)
                    shiftDayTable.addCell(cell)
                }
            }

            res.add(shiftDayTable)

            val radius = 8f
            res.setBorder(null)

            res.setNextRenderer(
                ShiftCellRenderer(
                    res,
                    radius.toDouble(),
                    color(shift1),
                    color(shift2)
                )
            )

            if (borderless) {
                res.setNextRenderer(null)
            }

            val result = Cell()
            result.setBorder(null)
            result.add(res)
            result.setPadding(2f)

            return result
        }

        private fun emptyCell(element: IBlockElement): Cell {
            val cell = Cell()
            cell.setBorder(null)
            cell.add(element)
            return cell
        }

        private fun titleParagraph(
            text: String,
            bold: Boolean,
            color1: Int?,
            color2: Int?
        ): IBlockElement {
            var c1 = -1
            var c2 = -1
            if (color1 != null) {
                c1 = color1
            }
            if (color2 != null) {
                c2 = color2
            }
            if (c2 == -1 || text.length == 1) {
                val title = titlePart(text, bold, c1)
                title.setTextAlignment(TextAlignment.CENTER)
                return title
            } else {
                val titleTable = Table(2)
                titleTable.setHorizontalAlignment(HorizontalAlignment.CENTER)
                val part1 = emptyCell(titlePart(text.substring(0, 1), bold, c1))
                val part2 = emptyCell(titlePart(text.substring(1, 2), bold, c2))
                titleTable.addCell(part1)
                titleTable.addCell(part2)
                return titleTable
            }
        }

        private fun titlePart(text: String, bold: Boolean, color: Int = -1): Paragraph {
            val titlePart = Paragraph(text)
            titlePart.setFontSize(16f)
            if (bold) {
                titlePart.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            }
            var tooBright = true
            if (color != -1) {
                tooBright = ColorHelper.isTooBright(color)
            }
            if (tooBright) {
                titlePart.setFontColor(ColorConstants.BLACK)
            } else {
                titlePart.setFontColor(ColorConstants.WHITE)
            }
            return titlePart
        }
    }
}