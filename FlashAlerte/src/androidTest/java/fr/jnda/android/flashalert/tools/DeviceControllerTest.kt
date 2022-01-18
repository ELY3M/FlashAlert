package fr.jnda.android.flashalert.tools

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import fr.jnda.android.flashalert.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeviceControllerTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup(){

    }

    @Test
    fun continueEvent() = runBlocking {
        val mContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        sharedPreferences.edit(commit = true) {
            putBoolean("event_noisedetector",false)
            putBoolean("event_call",false)
            putBoolean("settings_all" ,false)
            putBoolean("isActivate",false)
            putBoolean("settings_contacts",false)
            putBoolean("event_screenon",false)
            putBoolean("event_sms",false)
        }
        Assert.assertEquals(false ,DeviceController.continueEvent(mContext, true))

        sharedPreferences.edit(commit = true) {
            putBoolean("isActivate",true)
        }
        Assert.assertEquals(true ,DeviceController.continueEvent(mContext, true))

        sharedPreferences.edit(commit = true) {
            putBoolean("event_screenon",true)
        }
        Assert.assertEquals(false ,DeviceController.continueEvent(mContext, true))

        sharedPreferences.edit(commit = true) {
            putLong("keyLastFlash",System.currentTimeMillis())
        }
        Assert.assertEquals(false ,DeviceController.continueEvent(mContext, true))

        sharedPreferences.edit(commit = true) {
            putLong("keyLastFlash",System.currentTimeMillis() - 5200)
        }
        Assert.assertEquals(true ,DeviceController.continueEvent(mContext, true))

    }

    @Test
    fun useAppContext() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("fr.jnda.android.flashalert", appContext.packageName)
    }
}