package fr.jnda.android.flashalert.impl

/**
 * @author x192697
 * @project FlashAlert
 *
 * Création 29/07/18
 *
 *
 */
interface IPermissionCallBack {
    fun onPermissionEvent(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
}