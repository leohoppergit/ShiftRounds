package de.nulide.shiftcal.data.repository

import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.model.ShiftBlock
import de.nulide.shiftcal.data.model.ShiftBlockEntry
import de.nulide.shiftcal.data.repository.wrapper.ShiftBlockDTO

class ShiftBlockRepository(db: ShiftSwiftDB, rm: SCRepoManager) : CommonRepository(db, rm) {

    private val shiftBlockDao = db.shiftBlockDao()
    private val shiftBlockEntryDao = db.shiftBlockEntryDao()

    fun get(id: Int): ShiftBlockDTO {
        val block = shiftBlockDao.get(calId, id)
        val entries = shiftBlockEntryDao.getAllOf(calId, id)
        return ShiftBlockDTO(block, entries)
    }

    fun add(shiftBlock: ShiftBlock, entries: MutableList<ShiftBlockEntry>) {
        val id = shiftBlockDao.getHighestID(calId) + 1
        shiftBlock.id = id
        shiftBlock.calendarId = calId
        shiftBlockDao.add(shiftBlock)
        set(ShiftBlockDTO(shiftBlock, entries))
    }

    fun add(shiftBlock: ShiftBlockDTO) {
        add(shiftBlock.block, shiftBlock.entries)
    }

    fun delete(shiftBlock: ShiftBlock) {
        shiftBlockDao.delete(shiftBlock)
    }

    fun getAll(): MutableList<ShiftBlockDTO> {
        val blocks = shiftBlockDao.getAll(calId)
        val list = mutableListOf<ShiftBlockDTO>()
        for (block in blocks) {
            val entries = shiftBlockEntryDao.getAllOf(calId, block.id)
            if (entries.isNotEmpty()) {
                list.add(ShiftBlockDTO(block, entries))
            } else {
                shiftBlockDao.delete(block)
            }
        }
        return list
    }

    fun hasAny(): Boolean {
        return shiftBlockDao.hasAny(calId)
    }

    fun set(toEditShiftBlock: ShiftBlockDTO) {
        val block = toEditShiftBlock.block
        shiftBlockDao.update(block)
        shiftBlockEntryDao.deleteAllOf(calId, block.id)
        for (entry in toEditShiftBlock.entries) {
            entry.shiftBlockId = block.id
            entry.calendarId = calId
            shiftBlockEntryDao.add(entry)
        }
    }

}