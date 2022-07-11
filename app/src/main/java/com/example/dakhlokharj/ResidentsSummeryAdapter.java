package com.example.dakhlokharj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ResidentsSummeryAdapter extends RecyclerView.Adapter<ResidentsSummeryAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<ResidentSummery> residentSummaries;
    DecimalFormat format;

    public ResidentsSummeryAdapter(Context context, ArrayList<ResidentSummery> residentSummaries) {
        this.context = context;
        this.residentSummaries = residentSummaries;
        format = (DecimalFormat) NumberFormat.getInstance(new Locale(context.getString(R.string.language)));
        format.applyPattern("#,###");
    }

    @NonNull
    @Override
    public ResidentsSummeryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.residents_summery_recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResidentsSummeryAdapter.ViewHolder holder, int position) {
        holder.tvResidentName.setText(residentSummaries.get(position).getName());
        holder.tvResidentDebt.setText(createSignedCostString(residentSummaries.get(position).getDebt()));
        holder.tvResidentCredit.setText(createSignedCostString(residentSummaries.get(position).getCredit()));
        holder.tvResidentBalance.setText(createSignedCostString(residentSummaries.get(position).getBalance()));
        if (residentSummaries.get(position).getBalance() > 0) {
            holder.tvResidentBalance.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if (residentSummaries.get(position).getBalance() < 0) {
            holder.tvResidentBalance.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return residentSummaries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvResidentName;
        TextView tvResidentDebt;
        TextView tvResidentCredit;
        TextView tvResidentBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvResidentName = itemView.findViewById(R.id.textViewSummeryResidentName);
            tvResidentDebt = itemView.findViewById(R.id.textViewResidentDebt);
            tvResidentCredit = itemView.findViewById(R.id.textViewResidentCredit);
            tvResidentBalance = itemView.findViewById(R.id.textViewSummeryBalance);
        }
    }

    private String createSignedCostString(int cost) {
        String sign;
        if (cost > 0) {
            sign = "+";
        } else if (cost < 0) {
            sign = "-";
        } else {
            sign = "";
        }
        if (context.getString(R.string.currency).equals("fa")) {
            return format.format(cost) + context.getString(R.string.currency) + sign;
        } else {
            return sign + context.getString(R.string.currency) + format.format(cost);
        }
    }
}
