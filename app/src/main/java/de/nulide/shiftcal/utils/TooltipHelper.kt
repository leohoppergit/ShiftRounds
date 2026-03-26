package de.nulide.shiftcal.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.OnBalloonDismissListener
import de.nulide.shiftcal.R

class TooltipHelper {
    companion object {

        val END = ArrowOrientation.END
        val START = ArrowOrientation.START
        val TOP = ArrowOrientation.TOP
        val BOTTOM = ArrowOrientation.BOTTOM

        fun createDefaultTooltip(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            orientation: ArrowOrientation,
            msg: String
        ): Balloon {
            val balloonBuilder =
                createDefaultBalloonBuilder(context, lifecycleOwner, orientation, msg)
            return balloonBuilder.build()
        }

        fun createDefaultTooltipWithListener(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            orientation: ArrowOrientation,
            msg: String,
            listener: OnBalloonDismissListener
        ): Balloon {
            val balloonBuilder =
                createDefaultBalloonBuilder(context, lifecycleOwner, orientation, msg)
            balloonBuilder.setOnBalloonDismissListener(listener)
            return balloonBuilder.build()
        }

        fun createDefaultTooltipVariant(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            arrowOrientation: ArrowOrientation,
            msg: String
        ): Balloon {
            val balloonBuilder =
                createDefaultBalloonBuilder(context, lifecycleOwner, arrowOrientation, msg)
            balloonBuilder.setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            return balloonBuilder.build()
        }

        fun createTooltipWithListener(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            arrowOrientation: ArrowOrientation,
            msg: String,
            listener: OnBalloonDismissListener
        ): Balloon {
            val balloonBuilder =
                createDefaultIntroBalloonBuilder(context, lifecycleOwner, arrowOrientation, msg)
            balloonBuilder.setOnBalloonDismissListener(listener)
            balloonBuilder.setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            return balloonBuilder.build()
        }

        private fun createDefaultBalloonBuilder(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            orientation: ArrowOrientation,
            msg: String
        ): Balloon.Builder {
            val backgroundColor =
                ColorHelper.getColorAttr(context, androidx.appcompat.R.attr.colorAccent)

            val balloonBuilder = Balloon.Builder(context)
                .setHeight(BalloonSizeSpec.WRAP)
                .setWidth(BalloonSizeSpec.WRAP)
                .setText(msg)
                .setTextSize(17f)
                .setTextLineSpacing(1f)
                .setWidthRatio(0.6f)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
                .setArrowPosition(0.5f)
                .setArrowOrientation(orientation)
                .setMargin(8)
                .setPadding(8)
                .setDismissWhenClicked(true)
                .setFocusable(false)
                .setCornerRadius(4f)
                .setBackgroundColor(backgroundColor)
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setAutoDismissDuration(2500L)
                .setLifecycleOwner(lifecycleOwner)
            if (ColorHelper.isTooBright(backgroundColor)) {
                balloonBuilder.setTextColor(context.getColor(R.color.textColorBlack))
            } else {
                balloonBuilder.setTextColor(context.getColor(R.color.textColorWhite))
            }

            return balloonBuilder
        }

        fun createSmallTooltip(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            orientation: ArrowOrientation,
            msg: String
        ): Balloon {
            var builder = createDefaultBalloonBuilder(context, lifecycleOwner, orientation, msg)
            builder = builder.setMargin(0)
                .setPadding(4)
                .setWidthRatio(0.4f)
            return builder.build()
        }

        private fun createDefaultIntroBalloonBuilder(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            orientation: ArrowOrientation,
            msg: String
        ): Balloon.Builder {
            val backgroundColor =
                ColorHelper.getColorAttr(context, androidx.appcompat.R.attr.colorAccent)
            val view = LayoutInflater.from(context).inflate(R.layout.layout_tooltip_intro, null)
            val textView = view.findViewById<TextView>(R.id.tooltip_text)
            textView.text = msg
            if (ColorHelper.isTooBright(backgroundColor)) {
                textView.setTextColor(context.getColor(R.color.textColorBlack))
            } else {
                textView.setTextColor(context.getColor(R.color.textColorWhite))
            }
            val balloonBuilder = Balloon.Builder(context)
                .setHeight(BalloonSizeSpec.WRAP)
                .setWidth(BalloonSizeSpec.WRAP)
                .setLayout(view)
                .setWidthRatio(0.9f)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
                .setArrowPosition(0.5f)
                .setArrowOrientation(orientation)
                .setMargin(8)
                .setPadding(8)
                .setDismissWhenClicked(true)
                .setFocusable(false)
                .setCornerRadius(4f)
                .setBackgroundColor(backgroundColor)
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setLifecycleOwner(lifecycleOwner)

            return balloonBuilder
        }


    }
}