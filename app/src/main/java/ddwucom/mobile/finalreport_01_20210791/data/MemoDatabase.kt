package ddwucom.mobile.finalreport_01_20210791.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MemoDto::class], version=1)
abstract class MemoDatabase : RoomDatabase(){
    abstract fun memoDao() : MemoDao

    companion object{
        @Volatile       // Main memory 에 저장한 값 사용
        private var INSTANCE : MemoDatabase? = null

        // INSTANCE 가 null 이 아니면 반환, null 이면 생성하여 반환
        fun getDatabase(context: Context) : MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                    MemoDatabase::class.java, "memo_db").build()
                INSTANCE = instance
                instance
            }
        }
    }
}