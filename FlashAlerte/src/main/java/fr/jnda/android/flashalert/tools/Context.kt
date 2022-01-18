package fr.jnda.android.flashalert.tools

import android.content.Context
import android.widget.Toast

/**
 * @author x192697
 * @project FlashAlert
 *
 * Création 29/07/18
 *
 *
 */
fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, id, length).show()
}
