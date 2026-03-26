package de.nulide.shiftcal.net

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest

class DataRequest(
    method: Int,
    url: String?,
    listener: Response.Listener<String>?,
    errorListener: Response.ErrorListener?
) : StringRequest(method, url, listener, errorListener) {

    private var contentType = "text/plain"
    private var data = ""
    private var auth = ""

    fun setContentTypeJSON() {
        contentType = CONTENT_TYPE_JSON
    }

    fun addAuth(bearer: String) {
        auth = bearer
    }

    fun addData(data: String) {
        this.data = data
    }

    override fun getHeaders(): MutableMap<String, String> {
        if (auth.isNotEmpty()) {
            val headerMap = super.getHeaders().toMutableMap()
            headerMap["Authorization"] = "Bearer $auth"
            return headerMap
        }
        return super.getHeaders()
    }

    override fun getBody(): ByteArray {
        return data.toByteArray()
    }

    override fun getBodyContentType(): String {
        return contentType
    }

    companion object {
        const val CONTENT_TYPE_JSON = "application/json"
    }
}