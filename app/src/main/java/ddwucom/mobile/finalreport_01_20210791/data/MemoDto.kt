package ddwucom.mobile.finalreport_01_20210791.data

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName="memo_table")
data class MemoDto (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var csNm: String,
    var addr: String,
    var memo:String) : Serializable {
    override fun toString():String{
        return "${id} (${addr} ${csNm} : $memo)"
    }
}