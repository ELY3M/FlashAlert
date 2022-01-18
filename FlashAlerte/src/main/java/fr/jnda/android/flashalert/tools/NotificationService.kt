package fr.jnda.android.flashalert.tools

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager
import android.util.Log
import androidx.preference.PreferenceManager
import fr.jnda.android.flashalert.FlashAlert
import fr.jnda.android.flashalert.db.AppDatabase
import fr.jnda.android.flashalert.db.AppEntryRepository
import fr.jnda.android.flashalert.impl.CameraImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class NotificationService : NotificationListenerService() {

    private lateinit var mContext: Context
    lateinit var mCameraImpl: CameraImpl


    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        mCameraImpl = CameraImpl.newInstance(mContext)
    }

    private fun StrobeLight() {
        Log.i("flashalert", "Strobelight()")
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val smartHubURL = sharedPreferences.getString("smarthub_url", "http://192.168.1.100/script.php")
        val connection = URL(smartHubURL).openConnection() as HttpURLConnection
        try {
            val data = connection.inputStream.bufferedReader().use { it.readText()
                Log.d("flashalert", "opened url: "+smartHubURL)
            }
        } catch (e: Exception) {
            Log.d("flashalert", "connection crashed: "+e.toString())
        } finally {
            Log.d("flashalert", "disconnected")
            connection.disconnect()
        }

    }



    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pack = sbn.packageName

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val strobeTime = sharedPreferences.getInt("strobe_length", 5000)
        Log.d("flashalert strobe time", "strobeTime: "+strobeTime)
        Log.d("flashalert strobe time toLong", "strobeTime: "+strobeTime.toLong())

        GlobalScope.launch {
            val smsPackage = Telephony.Sms.getDefaultSmsPackage(mContext)
            val callerPackage = mContext.getSystemService(TelecomManager::class.java)?.defaultDialerPackage?:"empty"

            if (pack != smsPackage && pack != callerPackage && DeviceController.continueEvent(mContext)) {

                val selectorDao = AppDatabase.INSTANCE?.appSelector()
                if (selectorDao != null) {
                    val appEntry = AppEntryRepository(selectorDao)
                    val item = appEntry.getItemByPackage(pack)
                    Log.d("flashalert FLASH", "=> $item")
                    if (item != null && item.selected) {
                        if (!FlashAlert.isRunning)
                            FlashAlert.isRunning = mCameraImpl.toggleStroboscope()

                        //StrobeLight()
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                if (FlashAlert.isRunning) {
                                    FlashAlert.isRunning = false
                                    mCameraImpl.stopStroboscope()
                                }
                            }
                        }, strobeTime.toLong()) //was 1500
                        StrobeLight()
                    }
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {

        Log.i("flashalert Msg", "Notification Removed ${sbn.packageName}")
        if (isDefaultDialer(mContext,sbn.packageName)){
            mCameraImpl.stopStroboscope()
        }

    }

    private fun isDefaultDialer(context: Context, packageNameToCheck: String = context.packageName): Boolean {
        val dialingIntent = Intent(Intent.ACTION_DIAL)
        val resolveInfoList = packageManager.queryIntentActivities(dialingIntent, 0)
        if (resolveInfoList.size != 1)
            return false
        return packageNameToCheck == resolveInfoList[0].activityInfo.packageName
    }
}
