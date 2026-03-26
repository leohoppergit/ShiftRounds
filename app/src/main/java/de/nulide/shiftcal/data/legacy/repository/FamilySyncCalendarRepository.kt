package de.nulide.shiftcal.data.legacy.repository

import android.content.Context
import de.nulide.shiftcal.utils.SingletonHolder
import java.io.File

class FamilySyncCalendarRepository(context: Context) : CommonCalendarRepository(context) {
    
    companion object :
        SingletonHolder<FamilySyncCalendarRepository, Context>(::FamilySyncCalendarRepository) {
        const val FAMILY_SYNC_CAL_FILENAME = "family-sync-calendar.json"

        fun getFileGlob(context: Context): File {
            val familyCalFile = File(context.filesDir, FAMILY_SYNC_CAL_FILENAME)
            return familyCalFile
        }

    }

    override fun getFile(): File {
        return getFileGlob(context)
    }
}
