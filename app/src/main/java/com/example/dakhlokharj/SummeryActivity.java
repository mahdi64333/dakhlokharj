package com.example.dakhlokharj;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ghasemkiani.util.PersianCalendarHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

public class SummeryActivity extends AppCompatActivity {
    Toolbar toolbar;
    ConstraintLayout summeryFilterControlLayout;
    RecyclerView rvResidentsSummery;
    ArrayList<ResidentSummery> residentsSummaries, filteredResidentsSummaries;
    ResidentsSummeryAdapter residentsSummeryAdapter, filteredResidentsSummeryAdapter;
    DatabaseHelper dbHelper;
    TextView tvNoSummery;
    ImageButton ibFilter;
    TextInputEditText tiEtFromDate, tiEtToDate;
    TextInputLayout tilFromDate, tilToDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summery);

        toolbar = findViewById(R.id.toolbarSummeryActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(toolbar.getOverflowIcon()).setTint(getResources().getColor(R.color.white));
        Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(getResources().getColor(R.color.white));

        summeryFilterControlLayout = findViewById(R.id.summeryFilterControlLayout);
        rvResidentsSummery = findViewById(R.id.recyclerViewResidentsSummery);
        tvNoSummery = findViewById(R.id.textViewNoSummery);
        ibFilter = findViewById(R.id.imageButtonSummeryFilterByDate);
        tiEtFromDate = findViewById(R.id.textInputEditTextFromDateSummeryFilter);
        tilFromDate = findViewById(R.id.textInputLayoutFromDateSummeryFilter);
        tiEtToDate = findViewById(R.id.textInputEditTextToDateSummeryFilter);
        tilToDate = findViewById(R.id.textInputLayoutToDateSummeryFilter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SummeryActivity.this);
        rvResidentsSummery.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(SummeryActivity.this,
                layoutManager.getOrientation());
        rvResidentsSummery.addItemDecoration(dividerItemDecoration);
        dbHelper = new DatabaseHelper(SummeryActivity.this);
        residentsSummaries = dbHelper.getAllResidentsSummery();

        if (residentsSummaries == null) {
            Toast.makeText(SummeryActivity.this, R.string.an_error_has_occurred, Toast.LENGTH_SHORT).show();
        } else {
            residentsSummeryAdapter = new ResidentsSummeryAdapter(SummeryActivity.this, residentsSummaries);
            rvResidentsSummery.setAdapter(residentsSummeryAdapter);
            if (residentsSummaries.size() == 0) {
                tvNoSummery.setVisibility(View.VISIBLE);
            }
        }

        rvResidentsSummery.setAdapter(residentsSummeryAdapter);

        tilFromDate.setErrorIconDrawable(null);
        tilToDate.setErrorIconDrawable(null);

        ibFilter.setOnClickListener(view -> {
            boolean errorFlag = false;
            String fromDateString = Objects.requireNonNull(tiEtFromDate.getText()).toString().trim();
            String toDateString = Objects.requireNonNull(tiEtToDate.getText()).toString().trim();
            if (fromDateString.isEmpty()) {
                tilFromDate.setError(getString(R.string.its_empty));
                errorFlag = true;
            }
            if (toDateString.isEmpty()) {
                tilToDate.setError(getString(R.string.its_empty));
                errorFlag = true;
            }
            if (errorFlag) {
                return;
            }

            int[] fromDate = parseDate(fromDateString);
            int[] toDate = parseDate(toDateString);
            if (fromDate == null) {
                tilFromDate.setError(getString(R.string.incorrect_date_format));
                errorFlag = true;
            }
            if (toDate == null) {
                tilToDate.setError(getString(R.string.incorrect_date_format));
                errorFlag = true;
            }
            if (errorFlag) {
                return;
            }

            if (isDateNotValid(fromDate)) {
                tilFromDate.setError(getString(R.string.invalid_value));
                errorFlag = true;
            }
            if (isDateNotValid(toDate)) {
                tilToDate.setError(getString(R.string.invalid_value));
                errorFlag = true;
            }
            if (errorFlag) {
                return;
            }

            if (fromDate[0] > toDate[0]) {
                tilFromDate.setError(getString(R.string.has_to_be_smaller));
                tilToDate.setError(getString(R.string.has_to_be_bigger));
                errorFlag = true;
            } else if (fromDate[0] == toDate[0] && fromDate[1] > toDate[1]) {
                tilFromDate.setError(getString(R.string.has_to_be_smaller));
                tilToDate.setError(getString(R.string.has_to_be_bigger));
                errorFlag = true;
            } else if (fromDate[0] == toDate[0] && fromDate[1] == toDate[1] && fromDate[2] > toDate[2]) {
                tilFromDate.setError(getString(R.string.has_to_be_smaller));
                tilToDate.setError(getString(R.string.has_to_be_bigger));
                errorFlag = true;
            }
            if (errorFlag) {
                return;
            }

            tilToDate.setErrorEnabled(false);
            tilFromDate.setErrorEnabled(false);
            filteredResidentsSummaries = dbHelper.getAllResidentsSummeryWithDateBetween(fromDate[0], toDate[0],
                    fromDate[1], toDate[1],
                    fromDate[2], toDate[2]);
            filteredResidentsSummeryAdapter = new ResidentsSummeryAdapter(SummeryActivity.this, filteredResidentsSummaries);
            rvResidentsSummery.setAdapter(filteredResidentsSummeryAdapter);
            if (filteredResidentsSummaries.size() == 0) {
                tvNoSummery.setVisibility(View.VISIBLE);
            } else {
                tvNoSummery.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_summery_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuItemSummeryFilter) {
            if (summeryFilterControlLayout.getVisibility() == View.VISIBLE) {
                summeryFilterControlLayout.setVisibility(View.GONE);
                rvResidentsSummery.setAdapter(residentsSummeryAdapter);
                if (residentsSummaries.size() == 0) {
                    tvNoSummery.setVisibility(View.VISIBLE);
                } else {
                    tvNoSummery.setVisibility(View.GONE);
                }
            } else {
                summeryFilterControlLayout.setVisibility(View.VISIBLE);
                if (filteredResidentsSummaries != null) {
                    rvResidentsSummery.setAdapter(filteredResidentsSummeryAdapter);
                    if (filteredResidentsSummaries.size() == 0) {
                        tvNoSummery.setVisibility(View.VISIBLE);
                    } else {
                        tvNoSummery.setVisibility(View.GONE);
                    }
                } else {
                    tvNoSummery.setVisibility(View.GONE);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private int[] parseDate(String date) {
        int slashCount = 0;
        for (int i = 0; i < date.length(); i++) {
            if (date.charAt(i) == '/') {
                slashCount++;
            }
        }
        if (slashCount != 2) {
            return null;
        }
        String[] splitDate = date.split("/");
        if (splitDate.length != 3) {
            return null;
        }
        int[] dateParts = new int[3];
        try {
            dateParts[0] = Integer.parseInt(splitDate[0]);
        } catch (Exception e) {
            return null;
        }
        try {
            dateParts[1] = Integer.parseInt(splitDate[1]);
        } catch (Exception e) {
            return null;
        }
        try {
            dateParts[2] = Integer.parseInt(splitDate[2]);
        } catch (Exception e) {
            return null;
        }
        return dateParts;
    }

    private boolean isDateNotValid(int[] date) {
        boolean leapYear = PersianCalendarHelper.isLeapYear(date[0]);
        if (date[1] > 12) {
            return true;
        }
        if (date[0] == 0 || date[1] == 0 || date[2] == 0) {
            return true;
        }
        if (date[1] <= 6) {
            return date[2] > 31;
        } else if (date[1] <= 11) {
            return date[2] > 30;
        } else if (leapYear && date[2] > 30) {
            return true;
        } else return !leapYear && date[2] > 29;
    }
}