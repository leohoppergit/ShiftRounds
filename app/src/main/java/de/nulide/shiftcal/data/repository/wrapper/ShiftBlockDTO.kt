package de.nulide.shiftcal.data.repository.wrapper

import de.nulide.shiftcal.data.model.ShiftBlock
import de.nulide.shiftcal.data.model.ShiftBlockEntry

data class ShiftBlockDTO(
    val block: ShiftBlock,
    val entries: MutableList<ShiftBlockEntry>
) {
    override fun toString(): String {
        val s = StringBuilder()
        s.append(block.id).append("-").append(block.calendarId).append("\n").append(block.name)
            .append("\n")
        for (entry in entries) {
            s.append(entry.id).append("-").append(entry.shiftId).append(entry.pos)
        }
        return s.toString()
    }

    override fun equals(other: Any?): Boolean {
        return other.toString() == this.toString()
    }

    fun add(pos: Int, shiftId: Int) {
        entries.add(ShiftBlockEntry(pos, shiftId))
    }

    fun getAtPos(pos: Int): List<ShiftBlockEntry> {
        return entries.filter { entry -> entry.pos == pos }
    }

    fun removeAt(pos: Int) {
        entries.removeAll { entry -> entry.pos == pos }
    }

    fun getMaxDays(): Int {
        return if (entries.isNotEmpty()) {
            entries.maxOf { it.pos } + 1
        } else {
            0
        }
    }

    constructor() : this(ShiftBlock(""), mutableListOf())
}