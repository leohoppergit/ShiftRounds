package de.nulide.shiftcal.net

import com.android.volley.Response.ErrorListener
import de.nulide.shiftcal.net.listener.SpectatorAcquiredListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class SpectatorWaiter(
    private val spectatorAcquiredListener: SpectatorAcquiredListener,
    private val errorLister: ErrorListener
) : Callback {

    override fun onFailure(call: Call, e: IOException) {
        errorLister.onErrorResponse(null)
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
            val body = response.body

            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            var line = ""
            while (reader.readLine().also { line = it } != null) {
                if (line.isNotEmpty()) {
                    if (line.startsWith("data:")) {
                        spectatorAcquiredListener.onSpectatorAcquired(line.substringAfter("data:"))
                        return
                    }
                }
            }

        }
    }
}