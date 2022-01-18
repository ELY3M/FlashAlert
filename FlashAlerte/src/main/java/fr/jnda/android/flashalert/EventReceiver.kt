package fr.jnda.android.flashalert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.PhoneLookup
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import android.util.Log
import androidx.preference.PreferenceManager
import fr.jnda.android.flashalert.impl.CameraImpl
import fr.jnda.android.flashalert.tools.DeviceController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class EventReceiver : BroadcastReceiver() {

    lateinit var mCameraImpl: CameraImpl
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mContext: Context
    private val actionMMSReceived = "android.provider.Telephony.WAP_PUSH_RECEIVED"
    private val mmsDataType = "application/vnd.wap.mms-message"

    override fun onReceive(context: Context, intent: Intent) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        mCameraImpl = CameraImpl.newInstance(context)
        mContext = context

        GlobalScope.launch {
            Log.d(TAG, "onReceive: action ${intent.action}")
            if (DeviceController.continueEvent(mContext) || intent.isWhiteListed()) {
                when (intent.action) {
                    "android.provider.Telephony.SMS_RECEIVED",
                    "android.provider.Telephony.WAP_PUSH_RECEIVED" -> {
                        if (sharedPreferences.getBoolean("event_sms", false)) {
                            doStuffSMS(intent)
                        }
                    }
                    "android.intent.action.PHONE_STATE" -> {
                        if (sharedPreferences.getBoolean("event_call", false)) {
                            doStuffCall(intent)
                        }
                    }
                }
            }
        }
    }

    private fun Intent.isWhiteListed(): Boolean {
        return if(this.action == "android.intent.action.PHONE_STATE" ){
            return this.hasExtra("state") &&
                    (this.getStringExtra("state") == "OFFHOOK" || this.getStringExtra("state") =="IDLE")
        }
        else false

    }

    private fun getMMSPhoneNumber(intent: Intent?): String?{
        val action = intent?.action
        val type = intent?.type

        if(action.equals(actionMMSReceived) && type.equals(mmsDataType)) {

            val bundle: Bundle? = intent?.extras
            if (bundle != null) {
                val buffer = bundle.getByteArray("data")
                if (buffer != null) {
                    var incomingNumber = String(buffer)
                    var indx = incomingNumber.indexOf("/TYPE")
                    if (indx > 0 && (indx - 15) > 0) {
                        val newIndx = indx - 15
                        incomingNumber = incomingNumber.substring(newIndx, indx)
                        indx = incomingNumber.indexOf("+")
                        if (indx > 0) {
                            incomingNumber = incomingNumber.substring(indx)
                            return incomingNumber
                        }
                    }
                }
            }
        }
        return null
    }

    private fun doStuffSMS(intent: Intent){

        val msgs = getMessage(intent)
        val number = if (msgs == null){
            getMMSPhoneNumber(intent)
        } else {
            msgs[0].displayOriginatingAddress
        }
        if (contactAllowed(number)) {
            if (!FlashAlert.isRunning)
                FlashAlert.isRunning = mCameraImpl.toggleStroboscope()

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    if (FlashAlert.isRunning) {
                        FlashAlert.isRunning = false
                        mCameraImpl.stopStroboscope()
                    }
                }
            }, 1500)
        }
    }

    private val TAG = "EventReceiver"
    private fun doStuffCall(intent: Intent){
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        state?.apply {
            when (this) {
                "RINGING" -> {
                    if (contactAllowed(number)) {
                        if (!FlashAlert.isRunning)
                            FlashAlert.isRunning = mCameraImpl.toggleStroboscope()
                    }
                }
                "IDLE","OFFHOOK" -> {
                    if (FlashAlert.isRunning) {
                        FlashAlert.isRunning = false
                        mCameraImpl.stopStroboscope()
                    }

                }
            }

        }
    }

    private fun contactAllowed(number: String?) : Boolean{

        when {
            sharedPreferences.getBoolean("settings_all",false) -> return true
            sharedPreferences.getBoolean("settings_contacts",false) -> return try {
                if (number == null)
                    return false
                val resolver = mContext.contentResolver
                val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
                val c = resolver.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)

                if (c != null && c.count > 0) {
                    c.close()
                    true
                } else {
                    false
                }
            } catch (ex: Exception) {
                false
            }
            else -> return false
        }
    }

    /**
     * Methode de vérification des messages suite à un bug constaté sur android M
     * NPE getMessagesFromIntent ligne 1088
     */
    private fun getMessage(intent: Intent):Array<SmsMessage>? {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N){
            val messages: Array<Any>?
            try {
                @Suppress("UNCHECKED_CAST")
                messages = intent.getSerializableExtra("pdus") as? Array<Any>?
            } catch (e: ClassCastException) {
                return null
            }
            return if (messages.isNullOrEmpty()) {
                null
            } else
                Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } else {
            return Telephony.Sms.Intents.getMessagesFromIntent(intent)
        }
    }
}
