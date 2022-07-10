package com.example.dakhlokharj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SelectedConsumerAdapter extends RecyclerView.Adapter<SelectedConsumerAdapter.ViewHolder> {
    Context context;
    ArrayList<String> consumers;
    private OnItemClickListener listener;

    public SelectedConsumerAdapter(Context context, ArrayList<String> consumers) {
        this.context = context;
        this.consumers = consumers;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.selected_consumer_card, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvName.setText(consumers.get(position));
    }

    @Override
    public int getItemCount() {
        return consumers.size();
    }

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CardView cardView;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            tvName = itemView.findViewById(R.id.textViewSelectedConsumerName);
            cardView = itemView.findViewById(R.id.cardViewSelectedConsumer);
            cardView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClicked(position);
                    }
                }
            });
        }
    }
}
