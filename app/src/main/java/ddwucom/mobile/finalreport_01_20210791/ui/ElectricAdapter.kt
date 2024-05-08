package ddwucom.mobile.finalreport_01_20210791.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ddwucom.mobile.finalreport_01_20210791.data.Station
import ddwucom.mobile.finalreport_01_20210791.databinding.ListItemBinding

class ElectricAdapter : RecyclerView.Adapter<ElectricAdapter.ElectricHolder>() {
    var stations: List<Station>? = null

    override fun getItemCount(): Int {
        return stations?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectricHolder {
        val itemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ElectricHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ElectricHolder, position: Int) {
        val addr = stations?.get(position)?.addr
        val chargeTp = stations?.get(position)?.chargeTp
        val cpStat = stations?.get(position)?.cpStat
        val csNm = stations?.get(position)?.csNm
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
//        holder.itemBinding.tvItem.text = stations?.get(position).toString() //전체 목록 보여줌
        holder.itemBinding.tvItem.text =
            "이름: ${csNm}\n주소: $addr\n충전기 상태: $cpStatText"

        holder.itemBinding.clItem.setOnClickListener{
            clickListener?.onItemClick(it, position)
        }
    }

    class ElectricHolder(val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    var clickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }
}