package de.nulide.shiftcal.net.dto

import org.json.JSONObject

class CalendarUpdate(val spectatorUuid: String, val calendar: String) {

    fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("spectatorUuid", spectatorUuid);
        jsonObject.put("calendar", calendar)
        return jsonObject
    }
}