package de.nulide.shiftcal.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBackground
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import de.nulide.shiftcal.R

class QRCodeHelper {

    companion object {

        fun generateQRCode(context: Context, dataString: String): Drawable {
            val data = QrData.Text(dataString)
            val shapes = QrVectorShapes(
                darkPixel = QrVectorPixelShape.RoundCorners(.25f),
                ball = QrVectorBallShape.RoundCorners(.25f),
                frame = QrVectorFrameShape.RoundCorners(.25f)
            )
            val options = QrVectorOptions.Builder()
                .setPadding(.15f)
                .setBackground(
                    QrVectorBackground(
                        drawable = ContextCompat.getDrawable(context, R.drawable.qr_code_bg)
                    )
                )
                .setShapes(shapes).build()
            val drawable = QrCodeDrawable(data, options)
            return drawable
        }

    }

}