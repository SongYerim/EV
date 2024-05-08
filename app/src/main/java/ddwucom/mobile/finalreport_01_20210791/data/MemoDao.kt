package ddwucom.mobile.finalreport_01_20210791.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo_table")
    fun getAllMemos() : Flow<List<MemoDto>>

    @Query("SELECT * FROM memo_table WHERE id = :id")
    suspend fun getMemoById(id: Long) : List<MemoDto>

    @Insert
    suspend fun insertMemo(memo: MemoDto)

    @Update
    suspend fun updateMemo(memo: MemoDto)

    @Query("DELETE FROM memo_table WHERE id = :id")
    suspend fun deleteMemo(id: Long)

}