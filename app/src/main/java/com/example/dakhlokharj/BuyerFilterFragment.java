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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


public class BuyerFilterFragment extends Fragment {
    AutoCompleteTextView acTvBuyer;
    TextInputLayout tilBuyer;
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
        return inflater.inflate(R.layout.fragment_buyer_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        acTvBuyer = view.findViewById(R.id.autoCompleteTextViewBuyerFilter);
        tilBuyer = view.findViewById(R.id.textInputLayoutBuyerFilter);
        ibFilter = view.findViewById(R.id.imageButtonFilterByBuyer);
        rvOrders = view.findViewById(R.id.recyclerViewOrdersFilteredByBuyer);
        tvSum = view.findViewById(R.id.textViewOrdersSumFilteredByBuyer);
        tvNoOrdersFound = view.findViewById(R.id.textViewNoOrdersFilteredByBuyer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvOrders.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                layoutManager.getOrientation());
        rvOrders.addItemDecoration(dividerItemDecoration);
        dbHelper = new DatabaseHelper(requireContext());

        ArrayList<Resident> residents = dbHelper.getAllResidents(true);
        ArrayList<String> residentNames = new ArrayList<>();
        for (int i = 0; i < residents.size(); i++) {
            residentNames.add(residents.get(i).getName());
        }
        ArrayAdapter<String> residentArrayAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                residentNames);
        acTvBuyer.setAdapter(residentArrayAdapter);

        ibFilter.setOnClickListener(v -> {
            String buyerName = Objects.requireNonNull(acTvBuyer.getText()).toString().trim();
            if (buyerName.isEmpty()) {
                tilBuyer.setError(getString(R.string.its_empty));
                return;
            }
            orders = dbHelper.getAllOrdersWithBuyer(buyerName);
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

        acTvBuyer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilBuyer.setErrorEnabled(false);
            }
        });

        showDeleteOption = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().invalidateOptionsMenu();
        new Handler().postDelayed(() -> acTvBuyer.requestFocus(), 30);
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