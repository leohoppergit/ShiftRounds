package de.nulide.shiftcal.ui.settings.list

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import de.nulide.shiftcal.R


class SettingsEntry(
    context: Context,
    @StringRes stringId: Int,
    @DrawableRes iconId: Int,
    var settingsId: Int
) {
    var title: String = context.getString(stringId)
    var icon: Drawable? = ContextCompat.getDrawable(context, iconId)

    companion object {
        const val ID_SHIFTS = 0
        const val ID_SHIFT_BLOCKS = 1
        const val ID_EXPORT = 2
        const val ID_ADVANCED_SETTINGS = 3
        const val ID_FEEDBACK = 7
        const val ID_ABOUT = 8

        fun getSettingsEntries(context: Context): List<SettingsEntry> {
            val entries: MutableList<SettingsEntry> = ArrayList()
            entries.add(
                SettingsEntry(
                    context,
                    R.string.Settings_Shifts,
                    R.drawable.ic_shift,
                    ID_SHIFTS
                )
            )
            entries.add(
                SettingsEntry(
                    context,
                    R.string.Settings_Shift_Blocks,
                    R.drawable.ic_multi_shift_fab,
                    ID_SHIFT_BLOCKS
                )
            )
            entries.add(
                SettingsEntry(
                    context,
                    R.string.Settings_Export_Restore,
                    R.drawable.ic_import_export,
                    ID_EXPORT
                )
            )
            entries.add(
                SettingsEntry(
                    context,
                    R.string.Settings_Advanced_Settings,
                    R.drawable.ic_config,
                    ID_ADVANCED_SETTINGS
                )
            )
            entries.add(
                SettingsEntry(
                    context,
                    R.string.Settings_Feedback,
                    R.drawable.ic_feedback,
                    ID_FEEDBACK
                )
            )
            entries.add(
                SettingsEntry(
                    context,
                    R.string.Settings_About,
                    R.drawable.ic_about,
                    ID_ABOUT
                )
            )

            return entries
        }
    }
}
