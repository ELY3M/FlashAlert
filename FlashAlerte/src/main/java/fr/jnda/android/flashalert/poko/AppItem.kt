package fr.jnda.android.flashalert.poko

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppItem(val name: String,
                   val packageId: String,
                   var selected: Boolean){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}