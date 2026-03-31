package de.nulide.shiftcal.utils.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.DeviceRgb
import de.nulide.shiftcal.data.model.Shift
import java.io.File


class PDFHelper {

    companion object {

        const val PDF_FILE = "ShiftRounds.pdf"

        fun color(color: Int): DeviceRgb? {
            if (color != -1) {
                val rgbColor = DeviceRgb(
                    android.graphics.Color.red(color),
                    android.graphics.Color.green(color),
                    android.graphics.Color.blue(color)
                )
                return rgbColor
            } else {
                return null
            }
        }

        fun color(shift: Shift?): DeviceRgb? {
            if (shift != null) {
                val rgbColor = DeviceRgb(
                    android.graphics.Color.red(shift.color),
                    android.graphics.Color.green(shift.color),
                    android.graphics.Color.blue(shift.color)
                )
                return rgbColor
            } else {
                return null
            }
        }

        fun sharePDF(context: Context) {
            val pdfFile = File(context.cacheDir, PDF_FILE)
            shareFile(context, pdfFile, "application/pdf")
        }

        fun shareFile(context: Context, file: File, mimeType: String) {
            val uri: Uri =
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, null))
        }

    }
}
