package ddwucom.mobile.finalreport_01_20210791

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ddwucom.mobile.finalreport_01_20210791.data.MemoDao
import ddwucom.mobile.finalreport_01_20210791.data.MemoDatabase
import ddwucom.mobile.finalreport_01_20210791.data.MemoDto
import ddwucom.mobile.finalreport_01_20210791.databinding.ActivityDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    val detailBinding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    val memoDB: MemoDatabase by lazy {
        MemoDatabase.getDatabase(this)
    }

    val memoDao: MemoDao by lazy {
        memoDB.memoDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(detailBinding.root)

        val addr = intent.getStringExtra("addr")
        val chargeTp = intent.getIntExtra("chargeTp",0)
        val cpStat = intent.getIntExtra("cpStat",0)
        val cpTp = intent.getIntExtra("cpTp",0)
        val csNm = intent.getStringExtra("csNm")

        //충전기 타입 구분
        val chargeTpText = when (chargeTp) {
            1 -> "완속" 2 -> "급속"
            else -> "알 수 없음"
        }
        //충전기 상태 코드 구분
        val cpStatText = when (cpStat) {
            0 -> "상태 확인 불가" 1 -> "충전 가능" 2 -> "충전중"
            3 -> "고장/점검" 4 -> "통신 장애" 9 -> "충전 예약"
            else -> "알 수 없음"
        }
        //충전방식 구분
        val cpTpText = when (cpTp) {
            1 -> "B 타입(5핀)" 2 -> "C 타입(5핀)" 3 -> "BC 타입(5핀)" 4 -> "BC 타입(7핀)"
            5 -> "DC차데모" 6 -> "AC3상" 7 -> "DC콤보" 8 -> "DC차데모+DC콤보"
            9 -> "DC차데모+AC3상" 10 -> "DC차데모+DC콤보+AC3상"
            else -> "알 수 없음"
        }

        detailBinding.infomation.text = "충전소 이름: $csNm\n충전소 주소: $addr\n충전기 타입: $chargeTpText\n충전기 상태: $cpStatText\n충전 방식: $cpTpText"

        detailBinding.btnCancel.setOnClickListener {
            finish()
        }

        detailBinding.btnSave.setOnClickListener {
//            val detail = detailBinding.infomation.text
            val memo = detailBinding.review.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                memoDao.insertMemo(MemoDto(0, csNm!!, addr!!, memo))
            }

            Toast.makeText(this@DetailActivity, "메모가 저장되었습니다.", Toast.LENGTH_SHORT)
                .show()
            finish()
        }

    }
}