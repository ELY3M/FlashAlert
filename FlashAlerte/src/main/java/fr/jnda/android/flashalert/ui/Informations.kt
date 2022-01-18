package fr.jnda.android.flashalert.ui

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.PackageInfoCompat
import fr.jnda.android.flashalert.R
import kotlinx.android.synthetic.main.activity_informations.*


class Informations : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informations)
        intent?.extras?.let {
            changeNightStyle(it.getBoolean("nightmode",false))
        }
        var pInfo : PackageInfo? = null
        try {
             pInfo = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            val title =   getString(R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.title = Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                this.title = Html.fromHtml(title)
            }
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp)
        }

        txtVersion.text     = String.format(getString(R.string.info_version),pInfo?.versionName?: "99.99.99")
        txtVersionCode.text = String.format(getString(R.string.info_version_code), pInfo?.let {PackageInfoCompat.getLongVersionCode(pInfo).toString() }?: "00000000")
        txtPermissions.text = String.format(getString(R.string.info_premissions),"http://bit.ly/2E3NkWf")
        txtSources.text     = String.format(getString(R.string.info_sources),"https://gitlab.com/jnda/FlashAlert")
        txtChangeLog.text   = String.format(getString(R.string.info_changelog),"https://gitlab.com/jnda/FlashAlert/blob/master/CHANGELOG")
        txtIssues.text   = String.format(getString(R.string.info_issues),"https://gitlab.com/jnda/FlashAlert/issues")
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
           finish()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun changeNightStyle(bool: Boolean){
        if (bool)
            delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
        else
            delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
    }
}
