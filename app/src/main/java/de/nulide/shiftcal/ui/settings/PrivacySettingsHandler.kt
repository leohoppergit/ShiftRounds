package de.nulide.shiftcal.ui.settings

import android.content.Context
import android.view.View
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.helper.OnCloseListener

class PrivacySettingsHandler(
    context: Context,
    privacySettingsContent: NestedScrollView,
    loadValues: Boolean
) : View.OnClickListener {

    private val crashlyticsSwitch: MaterialSwitch
    private val saveButton: MaterialButton
    private val declineAllButton: MaterialButton

    private val settings: SettingsRepository

    var onCloseListener: OnCloseListener? = null
        set(value) {
            field = value
        }

    init {
        settings = SettingsRepository.getInstance(context)

        crashlyticsSwitch = privacySettingsContent.findViewById(R.id.crashlyticsSwitch)

        saveButton = privacySettingsContent.findViewById(R.id.saveButton)
        saveButton.setOnClickListener(this)

        declineAllButton = privacySettingsContent.findViewById(R.id.declineAllButton)
        declineAllButton.setOnClickListener(this)

        if (loadValues) {
            updateValues()
        }
    }

    fun updateValues() {
        crashlyticsSwitch.isChecked = settings.getBoolean(Settings.CRASHLYTICS)
    }

    override fun onClick(v: View?) {
        when (v) {
            saveButton -> {
                settings.set(Settings.CRASHLYTICS, crashlyticsSwitch.isChecked)
            }

            declineAllButton -> {
                settings.set(Settings.CRASHLYTICS, false)
            }
        }
        updateValues()
        settings.set(Settings.INTRO_PRIVACY, true)
        onCloseListener?.onClose()
    }

}