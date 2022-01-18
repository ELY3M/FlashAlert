package fr.jnda.android.flashalert.db

import fr.jnda.android.flashalert.poko.AppItem
import kotlinx.coroutines.*

class AppEntryRepository(private val mDao: AppSelectorDao) {


    suspend fun getAllApps(): List<AppItem>{
        return withContext(Dispatchers.Default) {
            mDao.getAppItem()
        }
    }

    suspend fun getItemByPackage(packageId: String): AppItem? {
        return withContext(Dispatchers.Default){
            mDao.getAppItemByPackageId(packageId)
        }
    }
    fun inserAppItem(appItem: AppItem): Job{
        return GlobalScope.launch {
            mDao.insertApp(appItem)
        }
    }

    fun deleteAppItem(appItem: AppItem): Job{
        return GlobalScope.launch {
            mDao.deleteApp(appItem)
        }
    }

    fun updateItemFromPackage(packageId: String,selected: Boolean): Job{
        return GlobalScope.launch {
            mDao.updateFromPackage(packageId, selected)
        }
    }

    suspend fun clearAll() {
        withContext(Dispatchers.Default) {
            AppDatabase.INSTANCE?.clearAllTables()
            true
        }

    }
}



