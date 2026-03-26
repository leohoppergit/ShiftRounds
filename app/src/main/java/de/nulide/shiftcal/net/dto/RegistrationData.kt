package de.nulide.shiftcal.net.dto

import org.json.JSONObject

class RegistrationData(private val name: String, private val hashedPW: String) {

    fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("name", name);
        jsonObject.put("hashedPW", hashedPW)
        return jsonObject
    }

}