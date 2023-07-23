package ir.demoodite.dakhlokharj.ui.components.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.databinding.ItemPurchaseBinding
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.text.DecimalFormat

class PurchasesListAdapter(
    private val decimalFormat: DecimalFormat,
    private val onClickListener: (detailedPurchase: DetailedPurchase) -> Unit,
) :
    ListAdapter<DetailedPurchase, PurchasesListAdapter.ViewHolder>(
        diffCallback
    ) {
    private val persianDateFormat: PersianDateFormat by lazy { PersianDateFormat("Y/m/d H:i") }
    var onLongClickListener: ((detailedPurchase: DetailedPurchase) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPurchaseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), persianDateFormat, decimalFormat)
        holder.itemView.setOnClickListener {
            onClickListener(getItem(position))
        }
        holder.itemView.setOnLongClickListener {
            onLongClickListener?.invoke(getItem(position))
            true
        }
    }

    class ViewHolder(private val binding: ItemPurchaseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            detailedPurchase: DetailedPurchase,
            persianDateFormat: PersianDateFormat,
            decimalFormat: DecimalFormat,
        ) {
            binding.tvPurchaseName.text = detailedPurchase.purchaseProduct
            binding.tvPurchaseBuyer.text = detailedPurchase.buyerName
            val detailedPurchaseDate = PersianDate(detailedPurchase.purchaseTime)
            binding.tvPurchaseDatetime.text = persianDateFormat.format(detailedPurchaseDate)
            binding.tvPurchasePrice.text = binding.root.context.getString(
                R.string.price_template,
                decimalFormat.format(detailedPurchase.purchasePrice)
            )

        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DetailedPurchase>() {
            override fun areItemsTheSame(
                oldItem: DetailedPurchase,
                newItem: DetailedPurchase,
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: DetailedPurchase,
                newItem: DetailedPurchase,
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}