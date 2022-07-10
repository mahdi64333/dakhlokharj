package com.example.dakhlokharj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ResidentsAdapter extends RecyclerView.Adapter<ResidentsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Resident> residents;
    FragmentManager fragmentManager;

    public ResidentsAdapter(Context context, ArrayList<Resident> residents, FragmentManager fragmentManager) {
        this.context = context;
        this.residents = residents;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.residents_recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvResidentName.setText(residents.get(position).getName());
        holder.cbResidentActive.setChecked(residents.get(position).getActive());
        holder.cbResidentActive.setOnClickListener(view -> {
            boolean b = holder.cbResidentActive.isChecked();
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            databaseHelper.updateResidentActiveById(residents.get(position).getId(), b);
            residents.get(position).setActive(b);
        });
        holder.itemView.setOnLongClickListener(view -> {
            RenameResidentDialog renameResidentDialog = new RenameResidentDialog(residents.get(position).getId(),
                    position,
                    residents.get(position).getName());
            renameResidentDialog.show(fragmentManager, null);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return residents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvResidentName;
        CheckBox cbResidentActive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvResidentName = itemView.findViewById(R.id.textViewResidentRowName);
            cbResidentActive = itemView.findViewById(R.id.checkBoxResidentRowActive);
        }
    }
}
