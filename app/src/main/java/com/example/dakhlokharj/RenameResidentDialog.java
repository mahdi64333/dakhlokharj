package com.example.dakhlokharj;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class RenameResidentDialog extends AppCompatDialogFragment {
    private final int id, position;
    private final String oldName;
    private TextInputEditText tiEtNewName;
    private RenameResidentDialogListener listener;

    public RenameResidentDialog(int id, int position, String oldName) {
        super();
        this.id = id;
        this.position = position;
        this.oldName = oldName;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_rename_resident, null);
        tiEtNewName = view.findViewById(R.id.textInputEditTextRenameResident);
        tiEtNewName.setText(oldName);
        builder.setView(view).
                setTitle(R.string.rename_resident).
                setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss()).
                setPositiveButton(getString(R.string.confirm), (dialogInterface, i) -> {
                    String newName = Objects.requireNonNull(tiEtNewName.getText()).toString().trim();
                    listener.applyText(newName, id, position);
                });
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (RenameResidentDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement RenameResidentDialogListener");
        }
    }

    public interface RenameResidentDialogListener {
        void applyText(String newName, int id, int position);
    }
}
