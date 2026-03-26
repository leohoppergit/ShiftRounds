package de.nulide.shiftcal.utils.pdf

import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.renderer.DrawContext
import com.itextpdf.layout.renderer.TableRenderer

class RoundedBorderTableRenderer(table: Table, val radius: Double) : TableRenderer(table) {

    override fun draw(drawContext: DrawContext?) {
        super.draw(drawContext)
        val rect: Rectangle = occupiedAreaBBox

        val x = rect.x
        val y = rect.y
        val width = rect.width
        val height = rect.height

        drawContext?.canvas?.roundRectangle(
            x.toDouble(),
            y.toDouble(),
            width.toDouble(),
            height.toDouble(),
            radius
        )
        drawContext?.canvas?.stroke()

    }

}
