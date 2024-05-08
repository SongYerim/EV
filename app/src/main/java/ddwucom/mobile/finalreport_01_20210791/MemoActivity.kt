package ddwucom.mobile.finalreport_01_20210791

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ddwucom.mobile.finalreport_01_20210791.data.MemoDao
import ddwucom.mobile.finalreport_01_20210791.data.MemoDatabase
import ddwucom.mobile.finalreport_01_20210791.data.MemoDto
import ddwucom.mobile.finalreport_01_20210791.databinding.ActivityMemoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoActivity : AppCompatActivity(){
    val memoBinding by lazy {
        ActivityMemoBinding.inflate(layoutInflater)
    }
    val memoDB: MemoDatabase by lazy {
        MemoDatabase.getDatabase(this)
    }

    val memoDao: MemoDao by lazy {
        memoDB.memoDao()
    }

    lateinit var memoDto: MemoDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(memoBinding.root)

//        val memo = intent.getSerializableExtra("memoDto")
        val memo = intent.getSerializableExtra("memoDto") as MemoDto

        // memoDto의 csNm과 addr 값을 가져와서 info에 표시합니다.
        memoBinding.info.text = "충전소 이름: ${memo.csNm}\n충전소 주소: ${memo.addr}\n\n나의 메모: ${memo.memo}"

        val id = memo.id
        memoBinding.btnRemove.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("메모 삭제")
            builder.setMessage("메모를 삭제하시겠습니까?")
            builder.setPositiveButton("삭제") {_,_ ->
                CoroutineScope(Dispatchers.IO).launch {
                    memoDao.deleteMemo(id)
                }
                Toast.makeText(this, "메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            builder.setNegativeButton("취소") { _,_ ->
                //아무런 동작 필요 없음
            }
            val dialog = builder.create()
            dialog.show()
        }
        memoBinding.btnClose.setOnClickListener {
            finish()
        }
        memoBinding.btnShare.setOnClickListener {
            // MemoActivity의 내용 문자열로 만듦
            val memoText = "충전소 이름: ${memo.csNm}\n충전소 주소: ${memo.addr}\n\n나의 메모: ${memo.memo}"

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, memoText)

            val chooserTitle = "친구에게 공유하기"
            startActivity(Intent.createChooser(intent, chooserTitle))
        }


    }


}