package fr.jnda.android.flashalert.tools

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.jnda.android.flashalert.R


/**
 * @author x192697
 * @project FlashAlert
 *
 * Création 29/07/18
 *
 *
 */
class PermissionHelper(private val context: Activity){

    private val enabledNotification = "enabled_notification_listeners"

    fun cameraHasPermission() : Boolean = ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun smsHasPermission() : Boolean = (ContextCompat.checkSelfPermission(context,Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context,Manifest.permission.RECEIVE_MMS) == PackageManager.PERMISSION_GRANTED)

    fun callHasPermission() : Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED )

        } else
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    fun contactsHasPermission() : Boolean = ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    fun soundHasPermission() : Boolean = ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun askPermission(vararg permission: String) {
        ActivityCompat.requestPermissions(context, permission, 123)
    }

    fun canReadNotifications(): Boolean  {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, enabledNotification)
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun displayInfo(){
        AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.alert_title))
            setMessage(context.getString(R.string.alert_message))
            setPositiveButton(context.getString(R.string.alert_positivebtn)) { _, _ ->  askPermission(Manifest.permission.CAMERA) }
        }.create().show()
    }

    fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(R.string.notification_listener_permission)
        alertDialogBuilder.setMessage(R.string.notification_listener_permission_explanation)
        alertDialogBuilder.setPositiveButton(android.R.string.yes
        ) { _, _ -> context.startActivityForResult(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS),6719) }
        alertDialogBuilder.setNegativeButton(android.R.string.no
        ) { dialog, _ -> dialog.dismiss()  }
        return alertDialogBuilder.create()
    }
}