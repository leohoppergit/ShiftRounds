package de.nulide.shiftcal.ui.helper

import android.content.Context
import androidx.core.content.ContextCompat
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.ShiftTime

class SpecialShifts {

    companion object {
        const val NONE_ID = -1
        const val DELETE_ID = -2
        const val CREATE_ID = -3
        const val ARCHIVE_ID = -4

        private val zero = ShiftTime.fromMinutes(0)

        fun getDeleteShift(context: Context): Shift {
            val deleteString = context.getString(R.string.shift_list_delete)
            return Shift(
                DELETE_ID,
                DELETE_ID,
                deleteString,
                deleteString.uppercase().substring(0, 1),
                zero,
                zero,
                0,
                null,
                null,
                null,
                1.0,
                0,
                0,
                -1,
                ContextCompat.getColor(context, R.color.removeColor),
                true,
                false
            )
        }

        fun getCreateNewShift(context: Context): Shift {
            val createNewShiftString = context.getString(R.string.shift_list_create_new_shift)
            return Shift(
                CREATE_ID,
                CREATE_ID,
                createNewShiftString,
                "+",
                zero,
                zero,
                0,
                null,
                null,
                null,
                1.0,
                0,
                0,
                -1,
                ContextCompat.getColor(context, R.color.newColor),
                true,
                false
            )
        }

        fun getCreateNewShiftNumbered(context: Context, pos: Int): Shift {
            val posText = pos.toString()
            return Shift(
                -3,
                -3,
                posText,
                posText,
                zero,
                zero,
                0,
                null,
                null,
                null,
                1.0,
                0,
                0,
                -1,
                ContextCompat.getColor(context, R.color.newColor),
                true,
                false
            )
        }

        fun getArchivedShift(context: Context): Shift {
            val archivedString: String = context.getString(R.string.Archived)
            return Shift(
                ARCHIVE_ID,
                ARCHIVE_ID,
                archivedString,
                "",
                zero,
                zero,
                0,
                null,
                null,
                null,
                1.0,
                0,
                0,
                -1,
                ContextCompat.getColor(context, R.color.onSurfaceWaterMark),
                true,
                false
            )
        }
    }
}
