package de.nulide.shiftcal.ui.helper

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.skydoves.balloon.OnBalloonClickListener
import de.nulide.shiftcal.R
import de.nulide.shiftcal.ui.settings.AdvancedSettingsActivity
import de.nulide.shiftcal.utils.TooltipHelper


class InfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr),
    View.OnClickListener, OnBalloonClickListener {

    private var balloonText = ""
    private var openActivity = -1
    private var scrollToView = -1

    init {
        setImageResource(R.drawable.ic_info)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.InfoView, 0, 0)
            val newBalloonText = typedArray.getString(R.styleable.InfoView_balloonText)
            if (newBalloonText != null) {
                setOnClickListener(this)
                balloonText = newBalloonText
            }
            openActivity = typedArray.getResourceId(R.styleable.InfoView_openActivity, -1)
            scrollToView = typedArray.getResourceId(R.styleable.InfoView_scrollTo, -1)
        }
    }

    override fun onClick(v: View?) {
        val activity = context as? AppCompatActivity
        if (activity != null) {
            val balloon = TooltipHelper.createDefaultTooltipVariant(
                context,
                activity,
                TooltipHelper.TOP,
                balloonText
            )
            if (openActivity != -1) {
                balloon.setOnBalloonClickListener(this)
            }
            balloon.showAlignTop(this)
        }
    }

    override fun onBalloonClick(view: View) {
        if (openActivity == R.string.Settings_Advanced_Settings) {
            val intent = Intent(context, AdvancedSettingsActivity::class.java)
            intent.putExtra(AdvancedSettingsActivity.EXTRA_SCROLL_TO, scrollToView)
            context.startActivity(intent)
        }
    }


}