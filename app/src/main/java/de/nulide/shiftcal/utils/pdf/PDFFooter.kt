package de.nulide.shiftcal.utils.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent
import com.itextpdf.layout.Document
import de.nulide.shiftcal.R

class PDFFooter(val context: Context, val doc: Document) : AbstractPdfDocumentEventHandler() {


    override fun onAcceptedEvent(event: AbstractPdfDocumentEvent?) {
        val docEvent = event as PdfDocumentEvent;
        val canvas = PdfCanvas(docEvent.page);
        val pageSize = docEvent.page.pageSize;

        val cordX = ((pageSize.left + doc.leftMargin)
                + (pageSize.right - doc.rightMargin)) / 2
        val footerY = doc.bottomMargin

        canvas.beginText();
        canvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA), 11f);
        canvas.moveText(cordX.toDouble(), footerY.toDouble())
        val appName = ContextCompat.getString(context, R.string.app_name)
        canvas.showText(appName)

        // Prepare Image
        val iconDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        val size = 255
        val bitmap = createBitmap(size, size)
        val bmCanvas = Canvas(bitmap)
        iconDrawable?.setBounds(0, 0, size, size)
        iconDrawable?.draw(bmCanvas)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = ImageDataFactory.create(stream.toByteArray())

        canvas.addImageFittedIntoRectangle(
            image,
            Rectangle(cordX - 30, footerY - (25 / 3), 25f, 25f),
            true
        )


        canvas.endText()
        canvas.release()

    }

}