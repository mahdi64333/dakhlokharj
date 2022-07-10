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

    public ResidentsSummeryAdapter(Context context, ArrayList<ResidentSummery> residentSummaries) {
        this.context = context;
        this.residentSummaries = residentSummaries;
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
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(new Locale("fa", "IR"));
        format.applyPattern("#,###");
        holder.tvResidentName.setText(residentSummaries.get(position).getName());
        holder.tvResidentDebt.setText(format.format(residentSummaries.get(position).getDebt()).concat("- تومان"));
        holder.tvResidentCredit.setText(format.format(residentSummaries.get(position).getCredit()).concat("+ تومان"));
        if (residentSummaries.get(position).getBalance() > 0) {
            holder.getTvResidentBalance.setText(format.format(residentSummaries.get(position).getBalance()).concat("+ تومان"));
            holder.getTvResidentBalance.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if (residentSummaries.get(position).getBalance() < 0) {
            holder.getTvResidentBalance.setText(format.format(residentSummaries.get(position).getBalance() * -1).concat("- تومان"));
            holder.getTvResidentBalance.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.getTvResidentBalance.setText(format.format(residentSummaries.get(position).getBalance()).concat(" تومان"));
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
        TextView getTvResidentBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvResidentName = itemView.findViewById(R.id.textViewSummeryResidentName);
            tvResidentDebt = itemView.findViewById(R.id.textViewResidentDebt);
            tvResidentCredit = itemView.findViewById(R.id.textViewResidentCredit);
            getTvResidentBalance = itemView.findViewById(R.id.textViewSummeryBalance);
        }
    }
}
