package ddwucom.mobile.finalreport_01_20210791

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ddwucom.mobile.finalreport_01_20210791.data.Station
import ddwucom.mobile.finalreport_01_20210791.databinding.ActivityListBinding
import ddwucom.mobile.finalreport_01_20210791.network.NetworkManager
import ddwucom.mobile.finalreport_01_20210791.ui.ElectricAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

//검색한 위치 목록 보여주기(이름과 주소명과 충전기 타입)
class ListActivity : AppCompatActivity() {
    private val TAG = "ListActivity"

    val listBinding by lazy {
        ActivityListBinding.inflate(layoutInflater)
    }

    lateinit var adapter : ElectricAdapter
    lateinit var networkDao : NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(listBinding.root)
        networkDao = NetworkManager(this)

        //목록으로 불러오기
        adapter = ElectricAdapter()
        listBinding.list.adapter = adapter
        listBinding.list.layoutManager = LinearLayoutManager(this)

        //main에서 넘어오는 키워드 값으로 목록 불러오는 결과 나타내기
        val loc = intent.getStringExtra("location")
        Log.d(TAG, "${loc} 위치 들어옴")
        CoroutineScope(Dispatchers.Main).launch{
            val def = async(Dispatchers.IO) {
                var stations : List<Station>? = null
                try {
                    stations = networkDao.downloadXml(loc.toString())

                } catch (e: IOException) {
                    Log.d(TAG, e.message?: "null")
                    null
                } catch (e: XmlPullParserException) {
                    Log.d(TAG, e.message?: "null")
                    null
                }
                stations
            }

            adapter.stations= def.await()
            adapter.notifyDataSetChanged()
        }
        //목록에서 한개 클릭시 상세정보 보여주게 넘기기
        adapter.setOnItemClickListener(object: ElectricAdapter.OnItemClickListener{
            override fun onItemClick(view: View, position: Int){
                val addr = adapter.stations?.get(position)?.addr
                val chargeTp = adapter.stations?.get(position)?.chargeTp
                val cpStat = adapter.stations?.get(position)?.cpStat
                val cpTp = adapter.stations?.get(position)?.cpTp
                val csNm = adapter.stations?.get(position)?.csNm

                // DetailActivity 를 호출하며 stations값들을 intent 로 전달
                val intent = Intent(applicationContext, DetailActivity::class.java)
                intent.putExtra("addr", addr)
                intent.putExtra("chargeTp", chargeTp)
                intent.putExtra("cpStat", cpStat)
                intent.putExtra("cpTp", cpTp)
                intent.putExtra("csNm", csNm)
                startActivity(intent)
            }
        })

        listBinding.btnMap.setOnClickListener {
            //main으로 가기
            finish()
        }
        listBinding.btnList.setOnClickListener {
            val loc = listBinding.etLocation.text.toString()
            CoroutineScope(Dispatchers.Main).launch{
                val def = async(Dispatchers.IO) {
                    var stations : List<Station>? = null
                    try {
                        stations = networkDao.downloadXml(loc)
                    } catch (e: IOException) {
                        Log.d(TAG, e.message?: "null")
                        null
                    } catch (e: XmlPullParserException) {
                        Log.d(TAG, e.message?: "null")
                        null
                    }
                    stations
                }

                adapter.stations= def.await()
                adapter.notifyDataSetChanged()
            }
        }

        listBinding.btnMyReview2.setOnClickListener {
            val intent = Intent(this@ListActivity, MemoListActivity::class.java)
            startActivity(intent)
        }
    }



}