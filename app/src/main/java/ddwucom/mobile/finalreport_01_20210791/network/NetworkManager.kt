package ddwucom.mobile.finalreport_01_20210791.network

import android.content.Context
import ddwucom.mobile.finalreport_01_20210791.R
import ddwucom.mobile.finalreport_01_20210791.data.Station
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import kotlin.jvm.Throws

class NetworkManager(val context: Context) {
    private val TAG = "NetworkManager"

    val openApiUrl by lazy {
        context.resources.getString(R.string.data_url)
    }

    @Throws(IOException::class)
    fun downloadXml(loc: String): List<Station>? {
        var stations: List<Station>? = null

        val inputStream = downloadUrl(openApiUrl+loc)

        /*Parser 생성 및 parsing 수행*/
        val parser = ElectricParser()
        stations = parser.parse(inputStream)

        return stations
    }


    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream? {
        val url = URL(urlString)
        return (url.openConnection() as? HttpURLConnection)?.run {
            readTimeout = 5000
            connectTimeout = 5000
            requestMethod = "GET"
            doInput = true

            connect()
            inputStream //최종적으로 이 값 반환
        }
    }

}