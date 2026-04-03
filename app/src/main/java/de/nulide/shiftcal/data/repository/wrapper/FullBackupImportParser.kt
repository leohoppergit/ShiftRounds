package de.nulide.shiftcal.data.repository.wrapper

import com.fasterxml.jackson.databind.JsonNode
import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.data.model.MonthNote
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.ShiftBlock
import de.nulide.shiftcal.data.model.ShiftBlockEntry
import de.nulide.shiftcal.data.model.ShiftTime
import de.nulide.shiftcal.data.model.WorkDay
import java.time.LocalDate

object FullBackupImportParser {

    fun parse(json: String): FullBackupDTO? {
        val root = JIO.getObjectMapper().readTree(json) ?: return null
        if (!root.isObject) return null

        val backupVersion = root.path("backupVersion").asInt(0)
        val appName = root.path("appName").asText("")
        if (backupVersion <= 0 || appName.isBlank()) return null

        return FullBackupDTO(
            backupVersion = backupVersion,
            exportedAt = root.path("exportedAt").asText(""),
            appName = appName,
            userName = root.path("userName").asText(""),
            settings = parseSettings(root.path("settings")),
            shifts = parseShifts(root.path("shifts")),
            monthNotes = parseMonthNotes(root.path("monthNotes")),
            workDays = parseWorkDays(root.path("workDays")),
            shiftBlocks = parseShiftBlocks(root.path("shiftBlocks"))
        )
    }

    private fun parseSettings(node: JsonNode): Map<String, String> {
        if (!node.isObject) return emptyMap()
        val result = linkedMapOf<String, String>()
        val fieldNames = node.fieldNames()
        while (fieldNames.hasNext()) {
            val key = fieldNames.next()
            val value = node.path(key)
            result[key] = if (value.isTextual) value.asText() else value.toString()
        }
        return result
    }

    private fun parseShifts(node: JsonNode): List<Shift> {
        if (!node.isArray) return emptyList()
        return node.mapNotNull { shiftNode ->
            val name = shiftNode.path("name").asText("")
            if (name.isBlank()) return@mapNotNull null

            Shift(
                id = shiftNode.path("id").asInt(0),
                calendarId = shiftNode.path("calendarId").asInt(0),
                name = name,
                shortName = shiftNode.path("shortName").asText(""),
                startTime = parseShiftTime(shiftNode.path("startTime")),
                endTime = parseShiftTime(shiftNode.path("endTime")),
                endDayOffset = shiftNode.path("endDayOffset").asInt(0),
                customBalanceMinutes = shiftNode.path("customBalanceMinutes").takeIf { !it.isNull }?.asInt(),
                specialAccountId = shiftNode.path("specialAccountId").takeIf { !it.isNull }?.asText(),
                specialAccountMinutes = shiftNode.path("specialAccountMinutes").takeIf { !it.isNull }?.asInt(),
                overtimeMultiplier = shiftNode.path("overtimeMultiplier").asDouble(1.0),
                sortOrder = shiftNode.path("sortOrder").asInt(0),
                breakMinutes = shiftNode.path("breakMinutes").asInt(0),
                alarmLeadMinutes = shiftNode.path("alarmLeadMinutes").takeIf { !it.isNull }?.asInt(),
                color = shiftNode.path("color").asInt(0),
                toAlarm = shiftNode.path("toAlarm").asBoolean(false),
                archived = shiftNode.path("archived").asBoolean(false)
            )
        }
    }

    private fun parseMonthNotes(node: JsonNode): List<MonthNote> {
        if (!node.isArray) return emptyList()
        return node.mapNotNull { noteNode ->
            if (!noteNode.has("year") || !noteNode.has("month")) return@mapNotNull null
            MonthNote(
                calendarId = noteNode.path("calendarId").asInt(0),
                year = noteNode.path("year").asInt(0),
                month = noteNode.path("month").asInt(0),
                msg = noteNode.path("msg").asText("")
            )
        }
    }

    private fun parseWorkDays(node: JsonNode): List<WorkDay> {
        if (!node.isArray) return emptyList()
        return node.mapNotNull { workDayNode ->
            val day = parseLocalDate(workDayNode.path("day")) ?: return@mapNotNull null
            WorkDay(
                id = workDayNode.path("id").asInt(0),
                calendarId = workDayNode.path("calendarId").asInt(0),
                day = day,
                shiftId = workDayNode.path("shiftId").asInt(0),
                alarmDismissed = workDayNode.path("alarmDismissed").asBoolean(false),
                icons = parseIcons(workDayNode.path("icons")),
                note = workDayNode.path("note").asText(""),
                overtimeMinutes = workDayNode.path("overtimeMinutes").asInt(0)
            )
        }
    }

    private fun parseShiftBlocks(node: JsonNode): List<ShiftBlockDTO> {
        if (!node.isArray) return emptyList()
        return node.mapNotNull { blockNode ->
            val blockJson = blockNode.path("block")
            val blockName = blockJson.path("name").asText("")
            if (blockName.isBlank()) return@mapNotNull null

            val block = ShiftBlock(
                id = blockJson.path("id").asInt(0),
                calendarId = blockJson.path("calendarId").asInt(0),
                name = blockName
            )
            val entries = blockNode.path("entries").takeIf { it.isArray }?.map { entryNode ->
                ShiftBlockEntry(
                    shiftBlockId = entryNode.path("shiftBlockId").asInt(block.id),
                    id = entryNode.path("id").asInt(0),
                    calendarId = entryNode.path("calendarId").asInt(0),
                    pos = entryNode.path("pos").asInt(0),
                    shiftId = entryNode.path("shiftId").asInt(0)
                )
            }?.toMutableList() ?: mutableListOf()

            ShiftBlockDTO(block, entries)
        }
    }

    private fun parseShiftTime(node: JsonNode): ShiftTime {
        return ShiftTime(
            hour = node.path("hour").asInt(0),
            minute = node.path("minute").asInt(0)
        )
    }

    private fun parseLocalDate(node: JsonNode): LocalDate? {
        return when {
            node.isArray && node.size() >= 3 -> {
                runCatching {
                    LocalDate.of(node[0].asInt(), node[1].asInt(), node[2].asInt())
                }.getOrNull()
            }

            node.isTextual -> runCatching { LocalDate.parse(node.asText()) }.getOrNull()
            else -> null
        }
    }

    private fun parseIcons(node: JsonNode): MutableList<Int> {
        if (!node.isArray) return mutableListOf()
        return node.mapNotNull { if (it.isInt) it.asInt() else null }.toMutableList()
    }
}
