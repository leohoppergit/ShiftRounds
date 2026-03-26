package de.nulide.shiftcal.ui.helper

interface OnItemClickedListener : (Int) -> Unit {
    override fun invoke(position: Int)
}