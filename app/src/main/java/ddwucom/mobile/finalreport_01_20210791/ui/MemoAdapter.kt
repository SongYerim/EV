package ddwucom.mobile.finalreport_01_20210791.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ddwucom.mobile.finalreport_01_20210791.data.MemoDto
import ddwucom.mobile.finalreport_01_20210791.databinding.MemoItemBinding

class MemoAdapter : RecyclerView.Adapter<MemoAdapter.MemoHolder>(){
    var memoList: List<MemoDto>? = null
    var itemClickListener: OnMemoItemClickListener? = null

    override fun getItemCount(): Int {
        return memoList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoHolder {
        val itemBinding = MemoItemBinding.inflate( LayoutInflater.from(parent.context), parent, false)
        return MemoHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MemoHolder, position: Int) {
        val dto = memoList?.get(position)
        val id = dto?.id
        val csNm = dto?.csNm
//        holder.itemBinding.tvData.text = "${id}.${csNm}"
        holder.itemBinding.tvData.text = csNm
        holder.itemBinding.clItemMemo.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
    }

    class MemoHolder(val itemBinding: MemoItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface OnMemoItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnMemoItemClickListener) {
        itemClickListener = listener
    }
}