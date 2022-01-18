package fr.jnda.android.flashalert.db

import androidx.room.*
import fr.jnda.android.flashalert.poko.AppItem

@Dao
interface AppSelectorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApp(item: AppItem)

//    @Update
//    fun updateApp(item: AppItem)

//    @Query("UPDATE appitem SET selected= :selected WHERE name= :name")
//    fun updateFromName(name: String, selected: Boolean)

    @Query("UPDATE appitem SET selected= :selected WHERE packageId= :packageId")
    fun updateFromPackage(packageId: String, selected: Boolean)

    @Query("SELECT * FROM AppItem WHERE packageId = :packageId")
    fun getAppItemByPackageId(packageId: String) : AppItem?

    @Delete
    fun deleteApp(item: AppItem)

    @Query("SELECT * FROM AppItem")
    fun getAppItem(): List<AppItem>

}