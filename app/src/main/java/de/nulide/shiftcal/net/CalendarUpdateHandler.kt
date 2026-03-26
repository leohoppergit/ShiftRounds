package de.nulide.shiftcal.net

import android.content.Context
import com.android.volley.Response
import com.android.volley.VolleyError
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository

class CalendarUpdateHandler(context: Context) : Response.Listener<String>, Response.ErrorListener {

    val settings = SettingsRepository.getInstance(context)

    override fun onResponse(response: String?) {
        settings.set(Settings.FAMILY_SYNC_LAST_UPLOAD_FAILED, false)
    }

    override fun onErrorResponse(error: VolleyError?) {
        settings.set(Settings.FAMILY_SYNC_LAST_UPLOAD_FAILED, true)
    }
}