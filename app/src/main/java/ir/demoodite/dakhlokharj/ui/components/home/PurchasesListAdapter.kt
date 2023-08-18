package ir.demoodite.dakhlokharj.ui.components.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.databinding.ItemPurchaseBinding
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import saman.zamani.persiandate.PersianDate
import java.text.DecimalFormat
import java.text.NumberFormat

class PurchasesListAdapter(
    private val onItemClickListener: (detailedPurchase: DetailedPurchase) -> Unit,
) :
    ListAdapter<DetailedPurchase, PurchasesListAdapter.ViewHolder>(
        diffCallback
    ) {
    var onLongClickListener: ((detailedPurchase: DetailedPurchase) -> Unit)? = null
    private val decimalFormat =
        NumberFormat.getInstance(LocaleHelper.currentLocale) as DecimalFormat

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPurchaseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), decimalFormat)
        holder.itemView.setOnClickListener {
            onItemClickListener(getItem(position))
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
            decimalFormat: DecimalFormat,
        ) {
            binding.tvPurchaseName.text = detailedPurchase.purchaseProduct
            binding.tvPurchaseBuyer.text = detailedPurchase.buyerName
            val purchaseDate = PersianDate(detailedPurchase.purchaseTime)
            binding.tvPurchaseDatetime.text = LocaleHelper.formatLocalizedDate(purchaseDate)
            binding.tvPurchasePrice.text = binding.root.context.getString(
                R.string.price_template,
                LocaleHelper.localizePrice(decimalFormat, detailedPurchase.purchasePrice)
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