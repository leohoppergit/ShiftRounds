package de.nulide.shiftcal.utils.permission

interface PermissionGrantedListener {

    fun onPermissionGranted(perm: String)

    fun onPermissionNotGranted(perm: String)

}