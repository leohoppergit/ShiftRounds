package de.nulide.shiftcal.net

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method
import com.android.volley.RequestQueue
import com.android.volley.Response.ErrorListener
import de.nulide.shiftcal.BuildConfig
import de.nulide.shiftcal.crypt.AES
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.net.dto.CalendarUpdate
import de.nulide.shiftcal.net.dto.LoginData
import de.nulide.shiftcal.net.dto.RegistrationData
import de.nulide.shiftcal.net.listener.AccountDeleteListener
import de.nulide.shiftcal.net.listener.ActiveSpectatorListListener
import de.nulide.shiftcal.net.listener.CalendarListener
import de.nulide.shiftcal.net.listener.OTTAcquiredListener
import de.nulide.shiftcal.net.listener.RegistrationListener
import de.nulide.shiftcal.net.listener.RemoveSpectatorSuccessListener
import de.nulide.shiftcal.net.listener.SetNameSuccessListener
import de.nulide.shiftcal.net.listener.SpectatorAcquiredListener
import de.nulide.shiftcal.net.listener.SpectatorListener
import de.nulide.shiftcal.net.listener.VersionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


class ShiftSwiftServerRestApi(val context: Context) {

    companion object {
        private const val URL_CALENDAR = "/calendar"
        private const val URL_USER = "/user"

        private const val URL_NAME = "/name"

        private const val URL_OWNER = "/owner"
        private const val URL_LOGIN = "$URL_USER/login"
        private const val URL_SPECTATOR = "/spectator"
        private const val URL_ALL = "/all"
        private const val URL_WAIT = "/wait"
        private const val URL_VERSION = "/version"
        private const val BASE_URL = BuildConfig.SERVER_IP

        const val CURRENT_SUPPORTED_API_VERSION = "3"
    }

    private val queue: RequestQueue = PatchedVolley.newRequestQueue(context)

    private val sc = SCRepoManager.getInstance(context)
    private var authToken = ""

    private val defaultRetry = DefaultRetryPolicy(500, 3, 1.5f)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.MINUTES)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    fun getVersion(listener: VersionListener, errorListener: ErrorListener?) {
        val url = BASE_URL + URL_VERSION
        val responseHandler = ResponseHandler(listener, errorListener)
        val versionRequest = DataRequest(Method.GET, url, responseHandler, responseHandler)
        queue.add(versionRequest)
    }

    fun register(
        name: String,
        hashedPW: String,
        listener: RegistrationListener,
        errorListener: ErrorListener
    ) {
        if (hashedPW.isNotEmpty()) {
            val url = BASE_URL + URL_USER
            val responseHandler = ResponseHandler(listener, errorListener)
            val registrationRequest =
                DataRequest(Method.POST, url, responseHandler, responseHandler)
            registrationRequest.addData(hashedPW)
            val registrationData = RegistrationData(name, hashedPW)
            registrationRequest.addData(registrationData.toJsonObject().toString())
            registrationRequest.setContentTypeJSON()
            queue.add(registrationRequest)
        }
    }

    fun getAuthAndAdd(request: DataRequest) {
        if (authToken.isEmpty()) {
            val user = sc.users.getLocal()
            if (user.netUuid != null) {
                val loginData = LoginData(user.netUuid!!, user.password)
                val authHelper = AuthHandler(this, context, request)
                val url = BASE_URL + URL_LOGIN
                val loginRequest = DataRequest(Method.POST, url, authHelper, authHelper)
                loginRequest.addData(loginData.toJsonObject().toString())
                loginRequest.setContentTypeJSON()
                queue.add(loginRequest)
            }
        } else {
            request.addAuth(authToken)
            queue.add(request)
        }
    }

    fun getNewSpectatorOTT(listener: OTTAcquiredListener, errorListener: ErrorListener) {
        val url = BASE_URL + URL_SPECTATOR
        val responseHandler = ResponseHandler(listener, errorListener)
        val newSpectatorRequest = DataRequest(Method.POST, url, responseHandler, responseHandler)
        getAuthAndAdd(newSpectatorRequest)
    }

    fun getSpectator(ott: String, listener: SpectatorListener, errorListener: ErrorListener) {
        val url = BASE_URL + URL_SPECTATOR
        val responseHandler = ResponseHandler(listener, errorListener)
        val newSpectatorRequest = DataRequest(Method.GET, url, responseHandler, responseHandler)
        newSpectatorRequest.addAuth(ott)
        queue.add(newSpectatorRequest)
    }

    fun getCalendar(
        uuid: String,
        externalCalendarListener: CalendarListener?,
        errorListener: ErrorListener?
    ) {
        val url = BASE_URL + URL_CALENDAR
        val spectatorId = uuid
        val calendarListener = CalendarListenerHandler(context, uuid, externalCalendarListener)
        val responseHandler = ResponseHandler(calendarListener, errorListener)
        val getCalendarRequest = DataRequest(Method.GET, url, responseHandler, responseHandler)
        getCalendarRequest.retryPolicy = defaultRetry
        getCalendarRequest.addAuth(spectatorId)
        queue.add(getCalendarRequest)
    }

    fun removeSpectator(
        spectatorUUID: String,
        removeSpectatorSuccessListener: RemoveSpectatorSuccessListener?,
        errorListener: ErrorListener?
    ) {
        val url = "$BASE_URL$URL_SPECTATOR/$spectatorUUID"
        val responseHandler = ResponseHandler(removeSpectatorSuccessListener, errorListener)
        val deleteSpectatorRequest =
            DataRequest(Method.DELETE, url, responseHandler, responseHandler)
        getAuthAndAdd(deleteSpectatorRequest)
    }

    fun setSpectatorName(
        uuid: String,
        name: String,
        setNameSuccessListener: SetNameSuccessListener?,
        errorListener: ErrorListener?
    ) {
        val url = BASE_URL + URL_SPECTATOR + URL_NAME
        val spectatorId = uuid
        val responseHandler = ResponseHandler(setNameSuccessListener, errorListener)
        val setSpectatorNameRequest = DataRequest(Method.PUT, url, responseHandler, responseHandler)
        setSpectatorNameRequest.addAuth(spectatorId)
        setSpectatorNameRequest.addData(name)
        queue.add(setSpectatorNameRequest)
    }

    fun getOwnerName(
        uuid: String,
        errorListener: ErrorListener?
    ) {
        val url = BASE_URL + URL_SPECTATOR + URL_OWNER
        val spectatorId = uuid
        val ownerNameListenerHandler = OwnerNameListenerHandler(context)
        val responseHandler = ResponseHandler(ownerNameListenerHandler, errorListener)
        val setSpectatorNameRequest = DataRequest(Method.GET, url, responseHandler, responseHandler)
        setSpectatorNameRequest.addAuth(spectatorId)
        queue.add(setSpectatorNameRequest)
    }

    fun getSpectators(listener: ActiveSpectatorListListener, errorListener: ErrorListener) {
        val url = BASE_URL + URL_SPECTATOR + URL_ALL
        val responseHandler = ResponseHandler(listener, errorListener)
        val newSpectatorRequest = DataRequest(Method.GET, url, responseHandler, responseHandler)
        getAuthAndAdd(newSpectatorRequest)
    }

    fun updateCalendar() {
        if (sc.users.getLocal().netUuid != null) {
            val sc = SCRepoManager.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                val data = sc.fromLocal { sc.asJSON() }
                for (user in sc.users.getShared()) {
                    val pw = user.password
                    val encData = AES.encryptWithAes(data, pw)
                    val calendarUpdateHandler = CalendarUpdateHandler(context)
                    if (encData != null) {
                        val url = BASE_URL + URL_CALENDAR
                        if (user.netUuid != null) {
                            val calendarUpdate = CalendarUpdate(user.netUuid!!, encData)
                            val updateCalendarRequest =
                                DataRequest(
                                    Method.PUT,
                                    url,
                                    calendarUpdateHandler,
                                    calendarUpdateHandler
                                )
                            updateCalendarRequest.addData(calendarUpdate.toJsonObject().toString())
                            updateCalendarRequest.setContentTypeJSON()
                            getAuthAndAdd(updateCalendarRequest)
                        }
                    }
                }
            }
        }
    }

    fun deleteData(accountDeleteListener: AccountDeleteListener, errorListener: ErrorListener) {
        val url = BASE_URL + URL_USER
        val responseHandler = ResponseHandler(accountDeleteListener, errorListener)
        val deleteSpectatorRequest =
            DataRequest(Method.DELETE, url, responseHandler, responseHandler)
        getAuthAndAdd(deleteSpectatorRequest)
    }

    fun waitForAcquisition(
        spectatorAcquiredListener: SpectatorAcquiredListener,
        errorListener: ErrorListener
    ) {
        val url = BASE_URL + URL_SPECTATOR + URL_WAIT
        val request = Request.Builder()
            .get()
            .url(url)
            .header("Authorization", "Bearer $authToken")
            .build()
        client.newCall(request).enqueue(SpectatorWaiter(spectatorAcquiredListener, errorListener))
    }

    fun setAuthToken(response: String) {
        authToken = response
    }

    fun setSharingName(
        uuid: String, name: String,
        setNameSuccessListener: SetNameSuccessListener?,
        errorListener: ErrorListener?
    ) {
        val url = BASE_URL + URL_USER + URL_NAME
        val responseHandler = ResponseHandler(setNameSuccessListener, errorListener)
        val setSharingNameRequest = DataRequest(Method.PUT, url, responseHandler, responseHandler)
        setSharingNameRequest.addData(name)
        getAuthAndAdd(setSharingNameRequest)
    }

}