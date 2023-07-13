package ir.demoodite.dakhlokharj.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.ItemPurchaseBinding
import ir.demoodite.dakhlokharj.models.database.DetailedPurchase
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.text.DecimalFormat

class DetailedPurchasesListAdapter(
    private val decimalFormat: DecimalFormat,
    private val onClickListener: (detailedPurchase: DetailedPurchase) -> Unit
) :
    ListAdapter<DetailedPurchase, DetailedPurchasesListAdapter.ViewHolder>(
        diffCallback
    ) {
    private val persianDateFormat: PersianDateFormat by lazy { PersianDateFormat("Y/m/d H:i") }

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
    }

    class ViewHolder(private val binding: ItemPurchaseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            detailedPurchase: DetailedPurchase,
            persianDateFormat: PersianDateFormat,
            decimalFormat: DecimalFormat
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
                newItem: DetailedPurchase
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: DetailedPurchase,
                newItem: DetailedPurchase
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}