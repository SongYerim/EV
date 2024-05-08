package ddwucom.mobile.finalreport_01_20210791

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ddwucom.mobile.finalreport_01_20210791.data.Station
import ddwucom.mobile.finalreport_01_20210791.databinding.ActivityMainBinding
import ddwucom.mobile.finalreport_01_20210791.network.NetworkManager
import ddwucom.mobile.finalreport_01_20210791.ui.ElectricAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    val mainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    lateinit var adapter : ElectricAdapter
    lateinit var networkDao : NetworkManager
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var geocoder : Geocoder
    private lateinit var currentLoc : Location
    private lateinit var googleMap : GoogleMap
    var centerMarker : Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)
        networkDao = NetworkManager(this)

        //목록으로 불러오기
        adapter = ElectricAdapter()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())
        checkPermissions()// 권한 획득
        startLocUpdates()// 위치 확인 시작

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(mapReadyCallback)

        val location = mainBinding.etLocation.text.toString()
        CoroutineScope(Dispatchers.Main).launch{
            val def = async(Dispatchers.IO) {
                var stations : List<Station>? = null
                try {
                    stations = networkDao.downloadXml(location)
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

            val station = adapter.stations
            if (station != null) {
                for(stat in station){
                    val lat = stat.lat
                    val longi = stat.longi
                    val csNm = stat.csNm
                    val cpStat = stat.cpStat
                    val addr = stat.addr
                    val chargeTp = stat.chargeTp
                    val cpTp = stat.cpTp
//                    //충전기 상태 코드 구분
                    val cpStatText = when (cpStat) {
                        0 -> "상태 확인 불가" 1 -> "충전 가능" 2 -> "충전중"
                        3 -> "고장/점검" 4 -> "통신 장애" 9 -> "충전 예약"
                        else -> "알 수 없음"
                    }
                    addMarker(LatLng(lat!!,longi!!), csNm!!, cpStatText, cpStat!!, addr!!,chargeTp!!,cpTp!!) // 마커 추가
                }
            }
        }

        mainBinding.btnList.setOnClickListener{
            //listactivity로 넘어가기
            val location = mainBinding.etLocation.text.toString()
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra("location", location)
            startActivity(intent)
        }

        mainBinding.btnMap.setOnClickListener {
            fusedLocationClient.removeLocationUpdates(locCallback) //위치확인중지
            //geocoding
            val location = mainBinding.etLocation.text.toString()
            // 지오코딩을 통해 주소를 위도와 경도로 변환
            geocoder.getFromLocationName(location,5) { addresses->
                CoroutineScope(Dispatchers.Main).launch {
                    val lat = addresses.get(0).latitude
                    val longi = addresses.get(0).longitude
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, longi), 15f)) // 해당 위치로 지도 이동 및 줌
                }
            }
        }

//        mainBinding.btnLocStop.setOnClickListener {
//            fusedLocationClient.removeLocationUpdates(locCallback) //위치확인중지
//        }

        mainBinding.btnMyReview.setOnClickListener {
            val intent = Intent(this@MainActivity, MemoListActivity::class.java)
            startActivity(intent)
        }

        mainBinding.btnCurrent.setOnClickListener {
            startLocUpdates()// 위치 확인 시작
        }

    }

    fun checkPermissions () {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    /*registerForActivityResult 는 startActivityForResult() 대체*/
    val locationPermissionRequest
            = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions() ) {
            permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
            }
            else -> {
            }
        }
    }


    /*위치 정보 수신 시작 */
    @SuppressLint("MissingPermission")
    private fun startLocUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locRequest,     // LocationRequest 객체
            locCallback,    // LocationCallback 객체
            Looper.getMainLooper()  // System 메시지 수신 Looper
        )
    }

    /*위치 정보 수신 설정*/
    val locRequest = LocationRequest.Builder(5000)
        .setMinUpdateIntervalMillis(3000)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .build()
    /*GoogleMap 로딩이 완료될 경우 실행하는 Callback*/
    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG,"GoogleMap is ready")
            //로딩 끝나면 수행
            googleMap.setOnMarkerClickListener {
                Toast.makeText(this@MainActivity, it.tag.toString().split("#")[1], Toast.LENGTH_LONG).show()
//                Toast.makeText(applicationContext, it.tag.toString(), Toast.LENGTH_SHORT).show()
                false
            }
            // 마커의 info window 클릭 이벤트 처리
            googleMap.setOnInfoWindowClickListener { marker ->
                // DetailActivity로 넘어가는 코드를 작성
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra("csNm", marker.title)
                intent.putExtra("cpStat", marker.tag.toString().split("#")[0].toInt())
                intent.putExtra("addr", marker.tag.toString().split("#")[1])
                intent.putExtra("chargeTp", marker.tag.toString().split("#")[2].toInt())
                intent.putExtra("cpTp", marker.tag.toString().split("#")[3].toInt())
                startActivity(intent)
            }
        }
    }
    /*위치 정보 수신 시 수행할 동작을 정의하는 Callback*/ //마지막 매개변수는 비동기방식
    val locCallback : LocationCallback = object : LocationCallback() {
        @SuppressLint("NewApi")
        override fun onLocationResult(locResult: LocationResult) {
            currentLoc = locResult.locations.get(0) //수신한 위치 중 첫번째 위치를 기록
            geocoder.getFromLocation(currentLoc.latitude, currentLoc.longitude, 5) { addresses ->
                CoroutineScope(Dispatchers.Main).launch {
                }
            }
            val targetLoc = LatLng(currentLoc.latitude, currentLoc.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLoc,17F))
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc,17F))
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locCallback)
    }
    /*LBSTest 관련*/
    //    최종위치 확인
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
//                showData(location.toString())
                currentLoc = location
            } else {
                currentLoc = Location("기본 위치")      // Last Location 이 null 경우 기본으로 설정
                currentLoc.latitude = 37.606816
                currentLoc.longitude = 127.042383
            }
        }
        fusedLocationClient.lastLocation.addOnFailureListener { e: Exception ->
            Log.d(TAG, e.toString())
        }
    }
    /*마커 추가*/
    fun addMarker(targetLoc:LatLng,name:String,state:String,cpStat:Int,addr:String,chargeTp:Int,cpTp:Int) {  // LatLng(37.606320, 127.041808)
        val markerOptions: MarkerOptions = MarkerOptions()  //마커를 표현하는 Option 생성
        markerOptions.position(targetLoc)   //필수
            .title("${name}")       //이름 보여주기
            .snippet("상태:${state}")   //상태 표기
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//            .icon(BitmapDescriptorFactory.fromResource(R.id.android))

        centerMarker = googleMap.addMarker(markerOptions)   //지도에 마커 추가, 추가마커 반환
        centerMarker?.showInfoWindow()  //마커 터치 Infowindow 표시
        centerMarker?.tag="${cpStat}#${addr}#${chargeTp}#${cpTp}" //마커에 관련 정보(Object) 저장
    }

}