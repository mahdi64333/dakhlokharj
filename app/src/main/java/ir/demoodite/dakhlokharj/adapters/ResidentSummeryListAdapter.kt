package ir.demoodite.dakhlokharj.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.ItemSummeryBinding
import ir.demoodite.dakhlokharj.models.database.ResidentSummery
import java.text.DecimalFormat

class ResidentSummeryListAdapter(
    private val decimalFormat: DecimalFormat,
) :
    ListAdapter<ResidentSummery, ResidentSummeryListAdapter.ViewHolder>(diffCallBack) {
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
            val formattedCredit = decimalFormat.format(residentSummery.credit)
            binding.tvCredit.text =
                binding.root.context.getString(R.string.price_template, formattedCredit)
            val formattedDebt = decimalFormat.format(residentSummery.debt)
            binding.tvDebt.text =
                binding.root.context.getString(R.string.price_template, formattedDebt)
            val balance = residentSummery.credit - residentSummery.debt
            val formattedBalance = decimalFormat.format(balance)
            val balanceWithSuffix =
                binding.root.context.getString(R.string.price_template, formattedBalance)
            binding.tvBalance.apply {
                if (balance > 0) {
                    text = "+$balanceWithSuffix"
                    setTextColor(Color.GREEN)
                } else if (balance < 0) {
                    text = "-$balanceWithSuffix"
                    setTextColor(Color.RED)
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