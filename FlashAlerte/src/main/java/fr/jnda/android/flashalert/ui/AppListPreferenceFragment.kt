package fr.jnda.android.flashalert.ui

import android.content.Context.TELECOM_SERVICE
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import fr.jnda.android.flashalert.R
import fr.jnda.android.flashalert.db.AppDatabase
import fr.jnda.android.flashalert.db.AppEntryRepository
import fr.jnda.android.flashalert.poko.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class AppListPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    private lateinit var checkBoxPreference: CheckBoxPreference
    private lateinit var appEntry: AppEntryRepository
    private lateinit var appList: MutableList<AppItem>
    private lateinit var screen: PreferenceScreen
    private lateinit var dialogFragment: WaitDialogFragment

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_apps)
        if( activity is AppCompatActivity && activity != null){
            val appCompatActivity: AppCompatActivity = activity as AppCompatActivity
            appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        screen = preferenceScreen
        showDialog()
        reloadAppList()
    }

    private fun showDialog(){
        fragmentManager?.beginTransaction()?.apply {
            val prev = fragmentManager?.findFragmentByTag("dialog")
            if (prev != null) {
                remove(prev)
            }
            addToBackStack(null)
            dialogFragment = WaitDialogFragment()
            dialogFragment.show(this, "dialog")
        }
    }

    fun refreshAppList(){
        screen.removeAll()
        showDialog()
        GlobalScope.launch {
            appEntry.clearAll()
            reloadAppList()
        }
    }

    private fun reloadAppList(){
        //val drawable = ChromeFloatingCirclesDrawable.Builder(context).build()

        val pm = context?.packageManager
        appList = mutableListOf()

        GlobalScope.launch {
            val selectorDao = AppDatabase.INSTANCE?.appSelector()

            if (selectorDao != null) {
                appEntry = AppEntryRepository(selectorDao)
                appList.addAll(appEntry.getAllApps())

                if (appList.size == 0) {
                    pm?.let {
                        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                        lateinit var appItem: AppItem
                        packages.filter { pm.getLaunchIntentForPackage(it.packageName) != null }.forEach {

                            appItem = AppItem(pm.getApplicationLabel(it).toString(), it.packageName, false)
                            val defaultSMS =Telephony.Sms.getDefaultSmsPackage(context)
                            val manger = context?.getSystemService(TELECOM_SERVICE) as TelecomManager?
                            val dialer = manger?.defaultDialerPackage?:packages

                            if (it.packageName != defaultSMS && it.packageName != dialer ) {
                                appList.add(appItem)
                                appEntry.inserAppItem(appItem)
                            }
                        }
                    }
                }

                GlobalScope.launch(Dispatchers.Main) {
                    var addItem = true
                    appList.sortedBy { it.name.toLowerCase(Locale.ENGLISH) }.forEach {
                        checkBoxPreference = CheckBoxPreference(context)
                        checkBoxPreference.apply {
                            title = it.name
                            key = it.packageId
                            isChecked = it.selected
                            icon = try {
                                pm?.getApplicationIcon(it.packageId)
                            } catch( e: PackageManager.NameNotFoundException){
                                appEntry.deleteAppItem(it)
                                addItem = false
                                ContextCompat.getDrawable(context, R.mipmap.ic_default_icon)
                            }
                            onPreferenceClickListener = this@AppListPreferenceFragment
                        }
                        if (addItem)
                            screen.addPreference(checkBoxPreference)
                    }
                    dialogFragment.dismiss()
                }
            }
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        if (preference != null && preference is CheckBoxPreference){
            appEntry.updateItemFromPackage(preference.key,preference.isChecked)
        }
        return true
    }
}