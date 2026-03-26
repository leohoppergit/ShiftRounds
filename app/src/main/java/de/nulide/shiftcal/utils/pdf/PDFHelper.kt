package de.nulide.shiftcal.utils.pdf

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.DeviceRgb
import de.nulide.shiftcal.data.model.Shift
import java.io.File


class PDFHelper {

    companion object {

        const val PDF_FILE = "ShiftRounds.pdf"

        fun color(color: Int): DeviceRgb? {
            if (color != -1) {
                val androidColor = Color(color)
                val rgbColor = DeviceRgb(androidColor.red, androidColor.green, androidColor.blue)
                return rgbColor
            } else {
                return null
            }
        }

        fun color(shift: Shift?): DeviceRgb? {
            if (shift != null) {
                val androidColor = Color(shift.color)
                val rgbColor = DeviceRgb(androidColor.red, androidColor.green, androidColor.blue)
                return rgbColor
            } else {
                return null
            }
        }

        fun sharePDF(context: Context) {
            val pdfFile = File(context.cacheDir, PDF_FILE)
            val uri =
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("application/pdf")
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            context.startActivity(shareIntent)
        }

    }
}
