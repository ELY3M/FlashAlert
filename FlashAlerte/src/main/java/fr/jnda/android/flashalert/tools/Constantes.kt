package fr.jnda.android.flashalert.tools

import android.os.Build

/**
 * @author x192697
 * @project FlashAlert
 *
 * Création 29/07/18
 *
 *
 */


fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
