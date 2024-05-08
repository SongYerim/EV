package ddwucom.mobile.finalreport_01_20210791.network

import android.util.Xml
import ddwucom.mobile.finalreport_01_20210791.data.Station
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class ElectricParser {
    private val ns: String? = null

    companion object {
        val FAULT_RESULT = "faultResult"    // OpenAPI 결과에 오류가 있을 때에 생성하는 정보를 위해 지정
        val ITEM_TAG = "item"
        val ADDR_TAG = "addr"
        val CHARGETP_TAG = "chargeTp"
        val CPSTAT_TAG = "cpStat"
        val CPTP_TAG = "cpTp"
        val CSNM_TAG = "csNm"
        val LAT_TAG = "lat"
        val LONGI_TAG = "longi"
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream?) : List<Station> {

        inputStream.use { inputStream ->
            val parser : XmlPullParser = Xml.newPullParser()
            /*Parser 의 동작 정의, next() 호출 전 반드시 호출 필요*/
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            /* Paring 대상이 되는 inputStream 설정 */
            parser.setInput(inputStream, null)
            /*Parsing 대상 태그의 상위 태그까지 이동*/
            while (parser.name != "items") {
                parser.next()
            }
            return readBoxOffice(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBoxOffice(parser: XmlPullParser) : List<Station> { //대상 항목 탐색
        val stations = mutableListOf<Station>() //DTO를 저장할 List
        parser.require(XmlPullParser.START_TAG, ns, "items") //현재 TAG를 확인 아닐 경우 예외 발생
        while(parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == ITEM_TAG) {
                stations.add( readDailyBoxOffice(parser) )
            } else {
                skip(parser)
            }
        }
        return stations
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDailyBoxOffice(parser: XmlPullParser) : Station {
        parser.require(XmlPullParser.START_TAG, ns, ITEM_TAG)
        var addr: String? = null
        var chargeTp: Int? = null
        var cpStat: Int? = null
        var cpTp: Int? = null
        var csNm: String? = null
        var lat: Double? = null
        var longi: Double? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                ADDR_TAG -> addr = readTextInTag(parser, ADDR_TAG)
                CHARGETP_TAG -> chargeTp = readTextInTag(parser, CHARGETP_TAG).toInt()
                CPSTAT_TAG -> cpStat = readTextInTag(parser, CPSTAT_TAG).toInt()
                CPTP_TAG -> cpTp = readTextInTag(parser, CPTP_TAG).toInt()
                CSNM_TAG -> csNm = readTextInTag(parser, CSNM_TAG)
                LAT_TAG -> lat = readTextInTag(parser, LAT_TAG).toDouble()
                LONGI_TAG -> longi = readTextInTag(parser, LONGI_TAG).toDouble()
                else -> skip(parser)
            }
        }
        return Station (addr, chargeTp, cpStat, cpTp, csNm, lat, longi)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTextInTag (parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        var text = ""
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return text
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}