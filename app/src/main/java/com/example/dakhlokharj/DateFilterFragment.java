package com.example.dakhlokharj;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ghasemkiani.util.PersianCalendarHelper;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


public class DateFilterFragment extends Fragment {
    TextInputEditText tiEtFromDate;
    TextInputLayout tilFromDate;
    TextInputEditText tiEtToDate;
    TextInputLayout tilToDate;
    ImageButton ibFilter;
    RecyclerView rvOrders;
    TextView tvSum, tvNoOrdersFound;
    ArrayList<Order> orders;
    DatabaseHelper dbHelper;
    Boolean showDeleteOption;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_date_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tiEtFromDate = view.findViewById(R.id.textInputEditTextFromDateFilter);
        tilFromDate = view.findViewById(R.id.textInputLayoutFromDateFilter);
        tiEtToDate = view.findViewById(R.id.textInputEditTextToDateFilter);
        tilToDate = view.findViewById(R.id.textInputLayoutToDateFilter);
        ibFilter = view.findViewById(R.id.imageButtonFilterByDate);
        rvOrders = view.findViewById(R.id.recyclerViewOrdersFilteredByDate);
        tvSum = view.findViewById(R.id.textViewOrdersSumFilteredByDate);
        tvNoOrdersFound = view.findViewById(R.id.textViewNoOrdersFilteredByDate);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvOrders.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                layoutManager.getOrientation());
        rvOrders.addItemDecoration(dividerItemDecoration);
        dbHelper = new DatabaseHelper(requireContext());

        tilFromDate.setErrorIconDrawable(null);
        tilToDate.setErrorIconDrawable(null);

        ibFilter.setOnClickListener(v -> {
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
                tilFromDate.setError("فرمت تاریخ صحیح نیست");
                errorFlag = true;
            }
            if (toDate == null) {
                tilToDate.setError("فرمت تاریخ صحیح نیست");
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
            orders = dbHelper.getAllOrdersWithDateBetween(fromDate[0], toDate[0],
                    fromDate[1], toDate[1],
                    fromDate[2], toDate[2]);
            OrdersAdapter ordersAdapter = new OrdersAdapter(requireContext(), orders);
            rvOrders.setAdapter(ordersAdapter);
            if (orders.size() == 0) {
                tvNoOrdersFound.setVisibility(View.VISIBLE);
                tvSum.setVisibility(View.GONE);
                showDeleteOption = false;
                requireActivity().invalidateOptionsMenu();
            } else {
                tvNoOrdersFound.setVisibility(View.GONE);
                tvSum.setVisibility(View.VISIBLE);
                showDeleteOption = true;
                requireActivity().invalidateOptionsMenu();
                int sum = 0;
                for (int i = 0; i < orders.size(); i++) {
                    sum += orders.get(i).getPrice();
                }
                DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(new Locale("fa", "IR"));
                format.applyPattern("#,###");

                tvSum.setText(getString(R.string.orders_sum).concat(" " + format.format(sum) + " تومان"));
            }
        });

        tiEtFromDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilFromDate.setErrorEnabled(false);
            }
        });

        tiEtToDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilToDate.setErrorEnabled(false);
            }
        });

        showDeleteOption = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().invalidateOptionsMenu();
        new Handler().postDelayed(() -> tiEtFromDate.requestFocus(), 30);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuItemDeleteAll) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
            alertDialog.setMessage(R.string.are_you_sure_to_delete_all);
            alertDialog.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                AtomicBoolean undoFlag = new AtomicBoolean(false);
                rvOrders.setAdapter(new OrdersAdapter(requireContext(), new ArrayList<>()));
                CoordinatorLayout root = requireActivity().findViewById(R.id.rootCoordinatorLayoutFilterActivity);
                Snackbar snackbar = Snackbar.make(root, getString(R.string.order_got_deleted), Snackbar.LENGTH_LONG);
                BaseTransientBottomBar.Behavior behavior = new BaseTransientBottomBar.Behavior();
                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
                snackbar.setBehavior(behavior);
                snackbar.setAction(R.string.cancel, view -> {
                    rvOrders.setAdapter(new OrdersAdapter(requireContext(), orders));
                    showDeleteOption = true;
                    requireActivity().invalidateOptionsMenu();
                    tvSum.setVisibility(View.VISIBLE);
                    undoFlag.set(true);
                });
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (!undoFlag.get()) {
                            for (int i = 0; i < orders.size(); i++) {
                                dbHelper.deleteOrderById(orders.get(i).getId());
                            }
                            Intent intent = new Intent();
                            requireActivity().setResult(1, intent);
                        }
                    }
                });
                snackbar.show();
                showDeleteOption = false;
                requireActivity().invalidateOptionsMenu();
                tvSum.setVisibility(View.GONE);
            });
            alertDialog.setNegativeButton(R.string.no, null);
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menuItemDeleteAll).setVisible(showDeleteOption);
    }
}