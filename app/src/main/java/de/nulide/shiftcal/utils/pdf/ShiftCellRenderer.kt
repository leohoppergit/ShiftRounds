package de.nulide.shiftcal.utils.pdf

import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.renderer.CellRenderer
import com.itextpdf.layout.renderer.DrawContext

class ShiftCellRenderer(
    modelElement: Cell?,
    val radius: Double,
    val color1: Color? = null,
    val color2: Color? = null
) : CellRenderer(modelElement) {

    @Override
    override fun draw(drawContext: DrawContext?) {
        val x = occupiedAreaBBox.x.toDouble()
        val y = occupiedAreaBBox.y.toDouble()
        val height = occupiedAreaBBox.height.toDouble()
        val width = occupiedAreaBBox.width.toDouble()
        drawContext?.canvas?.setColor(ColorConstants.BLACK, true)
        drawContext?.canvas?.roundRectangle(x, y, width, height, radius)
        drawContext?.canvas?.stroke()
        if (color1 != null) {
            drawContext?.canvas?.setColor(color1, true)
            drawContext?.canvas?.roundRectangle(x, y, width, height, radius)
            drawContext?.canvas?.fill()
            if (color2 != null) {
                drawContext?.canvas?.setColor(color2, true)
                drawContext?.canvas?.roundRectangle(x + width / 2, y, width / 2, height, radius)
                drawContext?.canvas?.roundRectangle(x + width / 2, y, width / 4, height, 0.0)
                drawContext?.canvas?.fill()
            }
        }
        drawContext?.canvas?.setColor(ColorConstants.BLACK, true)
        super.draw(drawContext)
    }
}