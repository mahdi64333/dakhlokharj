package com.example.dakhlokharj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
    Context context;
    ArrayList<Order> orders;
    DatabaseHelper dbHelper;

    public OrdersAdapter(Context context, ArrayList<Order> orders) {
        this.context = context;
        this.orders = orders;
        dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.orders_recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvOrderName.setText(orders.get(position).getOrderName());
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(new Locale(context.getString(R.string.language)));
        format.applyPattern("#,###");
        holder.tvOrderPrice.setText(format.format(orders.get(position).getPrice()).concat(context.getString(R.string.currency)));
        holder.tvOrderBuyer.setText(orders.get(position).getBuyer());
        String dateTime = "";
        dateTime += orders.get(position).getHour() + ":";
        dateTime += orders.get(position).getMinute() + ":";
        dateTime += orders.get(position).getSecond() + "   ";
        dateTime += orders.get(position).getYear() + "/";
        dateTime += orders.get(position).getMonth() + "/";
        dateTime += orders.get(position).getDay();
        holder.tvOrderDateTime.setText(dateTime);
        holder.itemView.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            String consumersString = dbHelper.getAllConsumersName(orders.get(position).getId());
            alertDialog.setMessage(context.getResources().getString(R.string.consumers) + consumersString);
            alertDialog.setPositiveButton(context.getResources().getString(R.string.ok), null);
            alertDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderName;
        TextView tvOrderPrice;
        TextView tvOrderBuyer;
        TextView tvOrderDateTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOrderName = itemView.findViewById(R.id.textViewOrderName);
            tvOrderPrice = itemView.findViewById(R.id.textViewOrderPrice);
            tvOrderBuyer = itemView.findViewById(R.id.textViewOrderBuyer);
            tvOrderDateTime = itemView.findViewById(R.id.textViewOrderDateTime);
        }
    }
}
