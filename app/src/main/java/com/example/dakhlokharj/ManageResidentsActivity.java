package com.example.dakhlokharj;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManageResidentsActivity extends AppCompatActivity implements RenameResidentDialog.RenameResidentDialogListener {
    RecyclerView rvResidents;
    ResidentsAdapter residentsAdapter;
    DatabaseHelper dbHelper;
    ArrayList<Resident> residents;
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    TextView tvNoResidentsFound;
    int resultCode = 0;
    AtomicBoolean undoFlag;
    Snackbar snackbar;
    Resident residentBackup;
    Snackbar.Callback snackbarBaseCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_residents);

        Toolbar toolbar = findViewById(R.id.toolbarManageResidents);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(toolbar.getOverflowIcon()).setTint(getResources().getColor(R.color.white));
        Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(getResources().getColor(R.color.white));


        rvResidents = findViewById(R.id.recyclerViewResidents);
        ImageButton btnAddResident = findViewById(R.id.imageButtonAddResident);
        TextInputLayout tilResidentName = findViewById(R.id.textInputLayoutAddResident);
        TextInputEditText tiEtResidentName = findViewById(R.id.textInputEditTextAddResident);
        tvNoResidentsFound = findViewById(R.id.textViewNoResidentsFound);
        dbHelper = new DatabaseHelper(ManageResidentsActivity.this);

        residents = dbHelper.getAllResidents(false);
        if (residents == null) {
            Toast.makeText(ManageResidentsActivity.this, R.string.an_error_has_occurred, Toast.LENGTH_SHORT).show();
        } else {
            residentsAdapter = new ResidentsAdapter(ManageResidentsActivity.this, residents, getSupportFragmentManager());
            rvResidents.setAdapter(residentsAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(ManageResidentsActivity.this);
            rvResidents.setLayoutManager(layoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(ManageResidentsActivity.this,
                    layoutManager.getOrientation());
            rvResidents.addItemDecoration(dividerItemDecoration);
            if (residents.isEmpty()) {
                tvNoResidentsFound.setVisibility(View.VISIBLE);
            }
        }

        btnAddResident.setOnClickListener(view -> {
            if (Objects.requireNonNull(tiEtResidentName.getText()).toString().trim().isEmpty()) {
                tilResidentName.setError(getString(R.string.please_enter_the_name));
                return;
            }
            String residentName = tiEtResidentName.getText().toString().trim();
            if (resultCode == 0) {
                resultCode = 2;
                setResult(2);
            }
            String errorMsg = dbHelper.addResident(residentName);
            Resident newResident = new Resident(dbHelper.getResidentIdByName(residentName), residentName, true);
            addResidentToAdapter(newResident);
            if (errorMsg == null && residents != null) {
                tiEtResidentName.setText("");
                return;
            }
            if (errorMsg != null && errorMsg.contains("UNIQUE constraint failed")) {
                tilResidentName.setError(getString(R.string.this_name_was_entered));
            } else {
                Toast.makeText(ManageResidentsActivity.this, R.string.an_error_occurred_during_save, Toast.LENGTH_SHORT).show();
            }
        });
        tiEtResidentName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilResidentName.setErrorEnabled(false);
            }
        });


        snackbarBaseCallback = new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                snackBarDismissCallbackMethod();
            }
        };

        itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                undoFlag = new AtomicBoolean(false);
                int position = viewHolder.getAdapterPosition();
                residentBackup = residents.get(position);
                residents.remove(viewHolder.getAdapterPosition());
                residentsAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                for (int i = position; i < residents.size(); i++) {
                    residentsAdapter.notifyItemChanged(i);
                }
                if (residents.size() == 0) {
                    tvNoResidentsFound.setVisibility(View.VISIBLE);
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageResidentsActivity.this);
                alertDialog.setMessage(R.string.are_you_sure_to_delete);
                alertDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    CoordinatorLayout root = findViewById(R.id.coordinatorLayoutManageResidentsActivity);
                    snackbar = Snackbar.make(root, getString(R.string.resident_got_deleted), Snackbar.LENGTH_LONG);
                    BaseTransientBottomBar.Behavior behavior = new BaseTransientBottomBar.Behavior();
                    behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
                    snackbar.setBehavior(behavior);
                    snackbar.setAction(R.string.cancel, view -> {
                        addResidentToAdapter(residentBackup);
                        undoFlag.set(true);
                    });
                    snackbar.addCallback(snackbarBaseCallback);
                    snackbar.show();
                });
                alertDialog.setNegativeButton(R.string.no, (dialogInterface, i) -> addResidentToAdapter(residentBackup));
                alertDialog.setOnCancelListener(dialogInterface -> addResidentToAdapter(residentBackup));
                alertDialog.show();
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvResidents);
    }

    private void addResidentToAdapter(Resident newResident) {
        if (residents.size() == 0) {
            tvNoResidentsFound.setVisibility(View.GONE);
            residents.add(newResident);
            residentsAdapter.notifyItemInserted(0);
        } else if (newResident.getName().compareTo(residents.get(0).getName()) < 0) {
            residents.add(0, newResident);
            residentsAdapter.notifyItemInserted(0);
            for (int i = 1; i < residents.size(); i++) {
                residentsAdapter.notifyItemChanged(i);
            }
        } else if (newResident.getName().compareTo(residents.get(residents.size() - 1).getName()) > 0) {
            residents.add(residents.size(), newResident);
            residentsAdapter.notifyItemInserted(residents.size() - 1);
        } else {
            for (int i = 0; i < residents.size() - 1; i++) {
                if (newResident.getName().compareTo(residents.get(i).getName()) > 0 &&
                        newResident.getName().compareTo(residents.get(i + 1).getName()) < 0) {
                    residents.add(i + 1, newResident);
                    residentsAdapter.notifyItemInserted(i + 1);
                    for (int j = i + 1; j < residents.size(); j++) {
                        residentsAdapter.notifyItemChanged(j);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void applyText(String newName, int id, int position) {
        if (newName.isEmpty()) {
            Toast.makeText(ManageResidentsActivity.this, R.string.no_name_was_given, Toast.LENGTH_SHORT).show();
        } else if (dbHelper.checkIfResidentExistsByName(newName)) {
            Toast.makeText(ManageResidentsActivity.this, getString(R.string.there_is_a_resident_with_this_name), Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateResidentNameById(id, newName);
            Resident changedResident = residents.get(position);
            residents.remove(position);
            residentsAdapter.notifyItemRemoved(position);
            changedResident.setName(newName);
            addResidentToAdapter(changedResident);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onPause() {
        if (snackbar != null && snackbar.isShown() && !undoFlag.get()) {
            snackbar.removeCallback(snackbarBaseCallback);
            snackBarDismissCallbackMethod();
        }
        super.onPause();
    }

    private void snackBarDismissCallbackMethod() {
        if (!undoFlag.get()) {
            resultCode = 3;
            setResult(1);
            dbHelper.deleteResidentById(residentBackup.getId());
        }
    }
}