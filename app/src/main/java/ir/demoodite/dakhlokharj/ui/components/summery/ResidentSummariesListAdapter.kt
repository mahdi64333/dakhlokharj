package ir.demoodite.dakhlokharj.ui.components.summery

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.ResidentSummery
import ir.demoodite.dakhlokharj.databinding.ItemSummeryBinding
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import java.text.DecimalFormat
import java.text.NumberFormat

class ResidentSummariesListAdapter(
) :
    ListAdapter<ResidentSummery, ResidentSummariesListAdapter.ViewHolder>(diffCallBack) {
    private val decimalFormat =
        NumberFormat.getInstance(LocaleHelper.currentLocale) as DecimalFormat

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            ItemSummeryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), decimalFormat)
    }

    class ViewHolder(private val binding: ItemSummeryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(residentSummery: ResidentSummery, decimalFormat: DecimalFormat) {
            binding.tvResidentName.text = residentSummery.residentName
            val formattedCredit = LocaleHelper.localizePrice(decimalFormat, residentSummery.credit)
            binding.tvCredit.text =
                binding.root.context.getString(R.string.price_template, formattedCredit)
            val formattedDebt = LocaleHelper.localizePrice(decimalFormat, residentSummery.debt)
            binding.tvDebt.text =
                binding.root.context.getString(R.string.price_template, formattedDebt)
            val balance = residentSummery.credit - residentSummery.debt
            val formattedBalance = LocaleHelper.localizePrice(decimalFormat, balance)
            val balanceWithSuffix =
                binding.root.context.getString(R.string.price_template, formattedBalance)
            binding.tvBalance.apply {
                if (balance > 0) {
                    text = "+$balanceWithSuffix"
                    setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            android.R.color.holo_green_dark,
                            binding.root.context.theme
                        )
                    )
                } else if (balance < 0) {
                    text = balanceWithSuffix
                    setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            android.R.color.holo_red_dark,
                            binding.root.context.theme
                        )
                    )
                } else {
                    text = balanceWithSuffix
                }
            }
        }
    }

    companion object {
        private val diffCallBack = object : DiffUtil.ItemCallback<ResidentSummery>() {
            override fun areItemsTheSame(
                oldItem: ResidentSummery,
                newItem: ResidentSummery,
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: ResidentSummery,
                newItem: ResidentSummery,
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}