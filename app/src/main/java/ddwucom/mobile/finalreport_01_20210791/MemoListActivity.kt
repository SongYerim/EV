package ddwucom.mobile.finalreport_01_20210791

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ddwucom.mobile.finalreport_01_20210791.data.MemoDao
import ddwucom.mobile.finalreport_01_20210791.data.MemoDatabase
import ddwucom.mobile.finalreport_01_20210791.databinding.ActivityMemoListBinding
import ddwucom.mobile.finalreport_01_20210791.ui.MemoAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoListActivity : AppCompatActivity(){
    private val TAG = "MemoListActivity"
    val memoListBinding by lazy {
        ActivityMemoListBinding.inflate(layoutInflater)
    }
    val memoDB : MemoDatabase by lazy {
        MemoDatabase.getDatabase(this)
    }

    val memoDao : MemoDao by lazy {
        memoDB.memoDao()
    }

    val adapter : MemoAdapter by lazy {
        MemoAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(memoListBinding.root)
        Log.d(TAG,"memolist에 들어옴")
        memoListBinding.rvMemo.adapter = adapter
        memoListBinding.rvMemo.layoutManager = LinearLayoutManager(this)
        Log.d(TAG,"adpater설정 완료")

        memoListBinding.btnRtMap.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        adapter.setOnItemClickListener(object: MemoAdapter.OnMemoItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent (this@MemoListActivity, MemoActivity::class.java )
                intent.putExtra("memoDto", adapter.memoList?.get(position))
                startActivity(intent)
            }
        })

        showAllMemo()
    }

    fun showAllMemo() {
        CoroutineScope(Dispatchers.Main).launch {
            memoDao.getAllMemos().collect { memos ->
                adapter.memoList = memos
                adapter.notifyDataSetChanged()
            }
        }
    }
}