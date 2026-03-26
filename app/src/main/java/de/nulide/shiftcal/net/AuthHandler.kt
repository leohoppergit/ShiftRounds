package de.nulide.shiftcal.net

import android.content.Context
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.helper.WarningDialog

class AuthHandler(
    val serverApi: ShiftSwiftServerRestApi,
    val context: Context,
    val request: DataRequest
) : Response.Listener<String>, Response.ErrorListener {

    private val queue: RequestQueue = PatchedVolley.newRequestQueue(context)

    override fun onResponse(response: String) {
        if (response != ServerCode.NO_ACCESS) {
            serverApi.setAuthToken(response)
            request.addAuth(response)
            queue.add(request)
        } else {
            val settings = SettingsRepository.getInstance(context)
            val sc = SCRepoManager.getInstance(context)
            settings.resetSharingAccount()
            sc.users.removeShared()
            if (request.method != Request.Method.DELETE) {
                WarningDialog(
                    context,
                    WarningDialog.ICON_INFO,
                    ContextCompat.getString(context, R.string.info_dialog_family_sync_logout)
                ).show()
                onErrorResponse(AuthFailureError())
            }
        }
    }

    override fun onErrorResponse(error: VolleyError?) {
        request.errorListener?.onErrorResponse(error)
    }
}