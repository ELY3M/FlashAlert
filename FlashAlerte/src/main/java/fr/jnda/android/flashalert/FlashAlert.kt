package fr.jnda.android.flashalert

import android.app.Application
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import fr.jnda.android.flashalert.db.AppDatabase


/**
 * @author x192697
 * @project FlashAlert
 *
 * Création 30/07/18
 *
 *
 */
class FlashAlert: Application() {
    companion object {
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        val dayAndNight = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("settings_dayAndNight",false)

        if (dayAndNight){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        AppDatabase.getAppDataBase(this)
    }
}