package de.nulide.shiftcal.data.legacy.model

class ShiftBlock {
    var id: Int
    var name: String
    var shiftBlockEntries: MutableList<ShiftBlockEntry>

    constructor() {
        this.id = -6
        this.name = "Error"
        this.shiftBlockEntries = mutableListOf()
    }
    
}