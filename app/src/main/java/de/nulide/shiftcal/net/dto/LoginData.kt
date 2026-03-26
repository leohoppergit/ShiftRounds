package de.nulide.shiftcal.net.dto

import org.json.JSONObject

class LoginData(private val uuid: String, private val hashedPW: String) {

    fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("uuid", uuid);
        jsonObject.put("hashedPW", hashedPW)
        return jsonObject
    }

}