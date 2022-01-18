package fr.jnda.android.flashalert.tools

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import fr.jnda.android.flashalert.FlashAlert
import kotlinx.coroutines.delay

object DeviceController {

    private const val zenMode = "zen_mode"
    private const val zenModeOff = 0
    private lateinit var compassInfo: CompassInfo
    private const val delayFlash = 5000
    private const val keyLastFlash = "lastFlash"

    @Synchronized
    suspend fun continueEvent(context: Context, test: Boolean = false): Boolean{
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        // Controle si le device a une camera avec flash

        val hasFlash = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (!hasFlash && !test)
            return false

        // Controle si l'application est active ou non
        val isActive = sharedPreferences.getBoolean("isActivate",true)

        if (!isActive)
            return false


        val now = System.currentTimeMillis()

        if ((sharedPreferences.getLong(keyLastFlash,(now- delayFlash) ) + delayFlash) > now){
            return false
        }

        val onScreeOn = sharedPreferences.getBoolean("event_screenon",false)
        val isScreenOn = isScreenOn(context)
        if (onScreeOn && isScreenOn && !FlashAlert.isRunning)
            return false

        val dnd = getDNDMode(context)
        // 0 - If DnD is off.
        // 1 - If DnD is on -RenderScript.Priority Only
        // 2 - If DnD is on - Total Silence
        // 3 - If DnD is on - Alarms Only
        if (dnd > 0 && !sharedPreferences.getBoolean("event_dnd", false) && !FlashAlert.isRunning){
            return false
        }

        val onlyFlat = sharedPreferences.getBoolean("event_onlyflat",false)
        if (onlyFlat && !FlashAlert.isRunning && !deviceIsFlat(context))
            return false

        val perferenceNoise = sharedPreferences.getBoolean("event_noisedetector",true)
        val noiseLimit = sharedPreferences.getInt("event_noiselimit",80)
        if(perferenceNoise && !FlashAlert.isRunning && !noiseIsImportant(noiseLimit)){
            return false
        }

        sharedPreferences.edit {
            putLong(keyLastFlash,System.currentTimeMillis())
        }
        return true
    }

    private fun isScreenOn(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isInteractive
    }

    private fun getDNDMode(context: Context): Int{
        val resolver = context.contentResolver
        return try {
            Settings.Global.getInt(resolver, zenMode)
        } catch (e: Settings.SettingNotFoundException) {
            zenModeOff
        }
    }

    private suspend fun deviceIsFlat(mContext:Context):Boolean{
        val mSensorManager = mContext.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        compassInfo = CompassInfo(object : DeviceControllerCallBack{
            override fun onSensorEvent() {
                mSensorManager.unregisterListener(compassInfo)
            }
        })
        val accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        var state = false
        mSensorManager.registerListener(compassInfo, accelerometer, SensorManager.SENSOR_DELAY_UI)
        delay(500)
        state = compassInfo.flatEnough()

        return state
    }

    private suspend fun noiseIsImportant(limit: Int): Boolean{
        val sound = SoundDetector()
        sound.start()
        val value = sound.getAverageLevel()
        return value > limit
    }

    interface DeviceControllerCallBack {
        fun onSensorEvent()
    }

}