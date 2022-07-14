package com.example.dakhlokharj;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ghasemkiani.util.icu.PersianCalendar;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    DrawerLayout drawer;
    ArrayList<Order> orders;
    OrdersAdapter ordersAdapter;
    RecyclerView rvOrders;
    ArrayList<Resident> residents;
    ArrayList<String> residentNames;
    ArrayAdapter<String> buyerAdapter;
    ArrayList<String> availableConsumers;
    ArrayAdapter<String> availableConsumersAdapter;
    ArrayList<String> selectedConsumers;
    SelectedConsumerAdapter selectedConsumersAdapter;
    DatabaseHelper dbHelper;
    TextInputLayout tilOrderName;
    TextInputEditText tiEtOrderName;
    TextInputLayout tilOrderPrice;
    TextInputEditText tiEtOrderPrice;
    TextInputLayout tilBuyer;
    AutoCompleteTextView acTvBuyer;
    TextInputLayout tilConsumer;
    AutoCompleteTextView acTvConsumer;
    ImageButton ibAddConsumer;
    RecyclerView rvSelectedConsumers;
    Button btnAddOrder;
    BottomSheetBehavior<View> behavior;
    View sheetView;
    TextView tvNoOrdersFound;
    ActivityResultLauncher<Intent> startActivityForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.settings_shared_preferences), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.settings_night_mode), false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbarMainActivity);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        rvOrders = findViewById(R.id.recyclerViewOrders);
        tvNoOrdersFound = findViewById(R.id.textViewNoOrdersFound);
        NavigationView navigationView = findViewById(R.id.nav_view);
        FloatingActionButton fabOpenAddOrderDialog = findViewById(R.id.floatingActionButtonAddOrder);
        BottomSheetDialog bottomSheet = new BottomSheetDialog(MainActivity.this);
        sheetView = MainActivity.this.getLayoutInflater().inflate(R.layout.modal_bottom_sheet_layout,
                findViewById(R.id.modalRoot));
        bottomSheet.setContentView(sheetView);
        behavior = BottomSheetBehavior.from(Objects.requireNonNull(bottomSheet.findViewById(com.google.android.material.R.id.design_bottom_sheet)));
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        tilOrderName = bottomSheet.findViewById(R.id.textInputLayoutOrderName);
        tiEtOrderName = bottomSheet.findViewById(R.id.textInputEditTextOrderName);
        tilOrderPrice = bottomSheet.findViewById(R.id.textInputLayoutOrderPrice);
        tiEtOrderPrice = bottomSheet.findViewById(R.id.textInputEditTextOrderPrice);
        tilBuyer = bottomSheet.findViewById(R.id.textInputLayoutBuyer);
        acTvBuyer = bottomSheet.findViewById(R.id.autoCompleteTextViewBuyer);
        tilConsumer = bottomSheet.findViewById(R.id.textInputLayoutConsumer);
        acTvConsumer = bottomSheet.findViewById(R.id.autoCompleteTextViewConsumer);
        ibAddConsumer = bottomSheet.findViewById(R.id.imageButtonAddConsumer);
        rvSelectedConsumers = bottomSheet.findViewById(R.id.recyclerViewConsumers);
        btnAddOrder = bottomSheet.findViewById(R.id.buttonAddOrder);
        dbHelper = new DatabaseHelper(MainActivity.this);

        switch (sharedPreferences.getInt(getString(R.string.settings_default_order), DatabaseHelper.ORDER_MODE_TIME_DESC)) {
            case DatabaseHelper.ORDER_MODE_TIME_DESC:
                toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_time_desc));
                break;
            case DatabaseHelper.ORDER_MODE_TIME_ASC:
                toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_time_asc));
                break;
            case DatabaseHelper.ORDER_MODE_PRICE_DESC:
                toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_price_desc));
                break;
            case DatabaseHelper.ORDER_MODE_PRICE_ASC:
                toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_price_asc));
                break;
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        rvOrders.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this,
                layoutManager.getOrientation());
        rvOrders.addItemDecoration(dividerItemDecoration);

        refreshOrdersList();

        bottomSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                fabOpenAddOrderDialog.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationEnd(animation);
                                fabOpenAddOrderDialog.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });
        bottomSheet.setOnShowListener(dialogInterface -> refreshBottomSheetHeight());

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_manage_residents) {
                Intent manageResidentsIntent = new Intent(MainActivity.this, ManageResidentsActivity.class);
                startActivityForResult.launch(manageResidentsIntent);
            } else if (itemId == R.id.nav_summery) {
                Intent summeryIntent = new Intent(MainActivity.this, SummeryActivity.class);
                startActivity(summeryIntent);
            } else if (itemId == R.id.nav_settings) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult.launch(settingsIntent);
            } else if (itemId == R.id.nav_about_us) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setMessage(R.string.about_us_text);
                dialog.setPositiveButton(R.string.ok, null);
                dialog.show();
            }

            drawer.closeDrawer(GravityCompat.START);
            return false;
        });

        fabOpenAddOrderDialog.setOnClickListener(view -> {
            fabOpenAddOrderDialog.animate()
                    .scaleX(0.0f)
                    .scaleY(0.0f)
                    .setDuration(100)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            fabOpenAddOrderDialog.setVisibility(View.INVISIBLE);
                        }
                    });
            bottomSheet.show();
        });

        tiEtOrderName.requestFocus();
        selectedConsumers = new ArrayList<>();
        refreshAdapters();
        selectedConsumersAdapter = new SelectedConsumerAdapter(MainActivity.this, selectedConsumers);
        rvSelectedConsumers.setAdapter(selectedConsumersAdapter);
        rvSelectedConsumers.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                LinearLayoutManager.HORIZONTAL,
                false));

        btnAddOrder.setOnClickListener(view -> {
            AtomicBoolean errorFlag = new AtomicBoolean(false);
            int orderPrice = 1;
            String orderName = Objects.requireNonNull(tiEtOrderName.getText()).toString().trim();
            String orderPriceText = Objects.requireNonNull(tiEtOrderPrice.getText()).toString().trim();
            String buyerName = acTvBuyer.getText().toString().trim();
            String consumerName = acTvConsumer.getText().toString().trim();
            if (orderName.isEmpty()) {
                tilOrderName.setError(getString(R.string.its_empty));
                errorFlag.set(true);
            }
            if (orderPriceText.isEmpty()) {
                tilOrderPrice.setError(getString(R.string.its_empty));
                errorFlag.set(true);
            } else {
                try {
                    orderPrice = Integer.parseInt(orderPriceText);
                    if (orderPrice < 0) {
                        tilOrderPrice.setError(getString(R.string.can_not_be_negative));
                        errorFlag.set(true);
                    }
                    if (orderPrice < 0) {
                        tilOrderPrice.setError(getString(R.string.can_not_be_zero));
                        errorFlag.set(true);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    tilOrderPrice.setError(getString(R.string.invalid_value));
                    errorFlag.set(true);
                }
            }
            if (buyerName.isEmpty()) {
                tilBuyer.setError(getString(R.string.its_empty));
                errorFlag.set(true);
            } else if (!residentNames.contains(buyerName)) {
                errorFlag.set(true);
                AlertDialog.Builder saveNewResidentDialog = new AlertDialog.Builder(MainActivity.this);
                saveNewResidentDialog.setMessage(R.string.buyer_not_found_do_you_want_to_save_new);
                saveNewResidentDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    String errorMsg = dbHelper.addResident(buyerName);
                    if (errorMsg == null) {
                        Toast.makeText(MainActivity.this, getString(R.string.new_resident_saved_successfully), Toast.LENGTH_SHORT).show();
                        addResidentToList(residentNames, buyerName);
                        refreshAdapters();
                        tilBuyer.setErrorEnabled(false);
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.an_error_occurred_during_save), Toast.LENGTH_SHORT).show();
                    }
                });
                saveNewResidentDialog.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> tilBuyer.setError(getString(R.string.not_found_in_residents)));
                saveNewResidentDialog.setOnCancelListener(dialogInterface -> tilBuyer.setError(getString(R.string.not_found_in_residents)));
                saveNewResidentDialog.show();
            }
            if (consumerName.isEmpty() && selectedConsumers.isEmpty()) {
                tilConsumer.setError(getString(R.string.no_consumer_selected));
                errorFlag.set(true);
            }

            if (!consumerName.isEmpty()) {
                if (!residentNames.contains(consumerName)) {
                    errorFlag.set(true);
                    AlertDialog.Builder saveNewResidentDialog = new AlertDialog.Builder(MainActivity.this);
                    saveNewResidentDialog.setMessage(R.string.consumer_not_found_do_you_want_to_save_new);
                    saveNewResidentDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        String errorMsg = dbHelper.addResident(consumerName);
                        if (errorMsg == null) {
                            Toast.makeText(MainActivity.this, getString(R.string.new_resident_saved_successfully), Toast.LENGTH_SHORT).show();
                            addResidentToList(residentNames, consumerName);
                            addResidentToList(selectedConsumers, consumerName);
                            refreshAdapters();
                            tilConsumer.setErrorEnabled(false);
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.an_error_occurred_during_save), Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveNewResidentDialog.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> tilConsumer.setError(getString(R.string.not_found_in_residents)));
                    saveNewResidentDialog.setOnCancelListener(dialogInterface -> tilConsumer.setError(getString(R.string.not_found_in_residents)));
                    saveNewResidentDialog.show();
                } else {
                    addResidentToList(selectedConsumers, consumerName);
                }
            }
            if (!errorFlag.get()) {
                PersianCalendar persianCalendar = new PersianCalendar(new Date());
                Order newOrder = new Order(0, orderName, orderPrice, buyerName,
                        persianCalendar.get(Calendar.YEAR),
                        persianCalendar.get(Calendar.MONTH) + 1,
                        persianCalendar.get(Calendar.DAY_OF_MONTH),
                        persianCalendar.get(Calendar.HOUR_OF_DAY),
                        persianCalendar.get(Calendar.MINUTE),
                        persianCalendar.get(Calendar.SECOND));
                String errorMsg = dbHelper.addOrder(newOrder, selectedConsumers);
                int newOrderId;
                try {
                    newOrderId = Integer.parseInt(errorMsg);
                    newOrder.setId(newOrderId);
                    if (orders != null) {
                        orders.add(0, newOrder);
                    }
                    tvNoOrdersFound.setVisibility(View.GONE);
                    ordersAdapter.notifyItemInserted(0);
                    for (int i = 0; i < orders.size(); i++) {
                        ordersAdapter.notifyItemChanged(i);
                    }
                    bottomSheet.dismiss();
                    rvOrders.smoothScrollToPosition(0);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, getString(R.string.an_error_occurred_during_save), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                tiEtOrderName.setText("");
                tiEtOrderPrice.setText("");
                acTvBuyer.setText("");
                acTvConsumer.setText("");
                tilOrderName.setErrorEnabled(false);
                tilOrderPrice.setErrorEnabled(false);
                tilBuyer.setErrorEnabled(false);
                tilConsumer.setErrorEnabled(false);
                selectedConsumers = new ArrayList<>();
                selectedConsumersAdapter = new SelectedConsumerAdapter(MainActivity.this, selectedConsumers);
                rvSelectedConsumers.setAdapter(selectedConsumersAdapter);
                rvSelectedConsumers.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                        LinearLayoutManager.HORIZONTAL,
                        false));
                refreshAdapters();
                tiEtOrderName.requestFocus();
            }
            new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
        });

        ibAddConsumer.setOnClickListener(view -> {
            String name = acTvConsumer.getText().toString().trim();
            if (!name.isEmpty()) {
                if (!residentNames.contains(name)) {
                    AlertDialog.Builder saveNewResidentDialog = new AlertDialog.Builder(MainActivity.this);
                    saveNewResidentDialog.setMessage(R.string.consumer_not_found_do_you_want_to_save_new);
                    saveNewResidentDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        String errorMsg = dbHelper.addResident(name);
                        if (errorMsg == null) {
                            Toast.makeText(MainActivity.this, getString(R.string.new_resident_saved_successfully), Toast.LENGTH_SHORT).show();
                            addResidentToList(residentNames, name);
                            selectedConsumers.add(name);
                            selectedConsumersAdapter.notifyItemInserted(selectedConsumers.size() - 1);
                            availableConsumersAdapter.remove(name);
                            acTvConsumer.setText("");
                            refreshAdapters();
                            TextView tvConsumers = bottomSheet.findViewById(R.id.textViewConsumers);
                            if (tvConsumers != null) {
                                tvConsumers.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.an_error_occurred_during_save), Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveNewResidentDialog.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                        tilConsumer.setError(getString(R.string.not_found_in_residents));
                        new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
                    });
                    saveNewResidentDialog.setOnCancelListener(dialogInterface -> {
                        tilConsumer.setError(getString(R.string.not_found_in_residents));
                        new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
                    });
                    saveNewResidentDialog.show();
                } else {
                    selectedConsumers.add(name);
                    selectedConsumersAdapter.notifyItemInserted(selectedConsumers.size() - 1);
                    availableConsumersAdapter.remove(name);
                    acTvConsumer.setText("");
                    TextView tvConsumers = bottomSheet.findViewById(R.id.textViewConsumers);
                    if (tvConsumers != null) {
                        tvConsumers.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                tilConsumer.setError(getString(R.string.its_empty));
                new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
            }
        });

        ibAddConsumer.setOnLongClickListener(view -> {
            while (0 < availableConsumers.size()) {
                selectedConsumers.add(availableConsumers.get(0));
                selectedConsumersAdapter.notifyItemInserted(selectedConsumers.size() - 1);
                availableConsumersAdapter.remove(availableConsumers.get(0));
            }
            new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
            return true;
        });

        tiEtOrderName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilOrderName.setErrorEnabled(false);
                new Handler().postDelayed(() -> refreshBottomSheetHeight(), 500);
            }
        });

        tiEtOrderPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilOrderPrice.setErrorEnabled(false);
                new Handler().postDelayed(() -> refreshBottomSheetHeight(), 500);
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
                new Handler().postDelayed(() -> refreshBottomSheetHeight(), 500);
            }
        });

        acTvConsumer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilConsumer.setErrorEnabled(false);
                new Handler().postDelayed(() -> refreshBottomSheetHeight(), 500);
            }
        });

        acTvBuyer.setOnFocusChangeListener((view, b) -> {
            String name = acTvBuyer.getText().toString().trim();
            if (!b && !name.isEmpty()) {
                if (!residentNames.contains(name)) {
                    AlertDialog.Builder saveNewResidentDialog = new AlertDialog.Builder(MainActivity.this);
                    saveNewResidentDialog.setMessage(R.string.buyer_not_found_do_you_want_to_save_new);
                    saveNewResidentDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        String errorMsg = dbHelper.addResident(name);
                        if (errorMsg == null) {
                            Toast.makeText(MainActivity.this, getString(R.string.new_resident_saved_successfully), Toast.LENGTH_SHORT).show();
                            addResidentToList(residentNames, name);
                            refreshAdapters();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.an_error_occurred_during_save), Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveNewResidentDialog.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                        tilBuyer.setError(getString(R.string.not_found_in_residents));
                        new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
                    });
                    saveNewResidentDialog.setOnCancelListener(dialogInterface -> {
                        tilBuyer.setError(getString(R.string.not_found_in_residents));
                        new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
                    });
                    saveNewResidentDialog.show();
                }
            }
        });

        acTvConsumer.setOnFocusChangeListener((view, b) -> {
            String name = acTvConsumer.getText().toString().trim();
            if (!b && !name.isEmpty()) {
                if (!residentNames.contains(name)) {
                    AlertDialog.Builder saveNewResidentDialog = new AlertDialog.Builder(MainActivity.this);
                    saveNewResidentDialog.setMessage(R.string.consumer_not_found_do_you_want_to_save_new);
                    saveNewResidentDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        String errorMsg = dbHelper.addResident(name);
                        if (errorMsg == null) {
                            Toast.makeText(MainActivity.this, getString(R.string.new_resident_saved_successfully), Toast.LENGTH_SHORT).show();
                            addResidentToList(residentNames, name);
                            selectedConsumers.add(name);
                            selectedConsumersAdapter.notifyItemInserted(selectedConsumers.size());
                            acTvConsumer.setText("");
                            refreshAdapters();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.an_error_occurred_during_save), Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveNewResidentDialog.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                        tilConsumer.setError(getString(R.string.not_found_in_residents));
                        new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
                    });
                    saveNewResidentDialog.setOnCancelListener(dialogInterface -> {
                        tilConsumer.setError(getString(R.string.not_found_in_residents));
                        new Handler().postDelayed(this::refreshBottomSheetHeight, 500);
                    });
                    saveNewResidentDialog.show();
                }
            }
        });

        selectedConsumersAdapter.setOnItemClickListener(position -> {
            addResidentToList(availableConsumers, selectedConsumers.get(position));
            availableConsumersAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, availableConsumers);
            acTvConsumer.setAdapter(availableConsumersAdapter);
            selectedConsumers.remove(position);
            selectedConsumersAdapter.notifyItemRemoved(position);
            for (int i = position; i < selectedConsumers.size(); i++) {
                selectedConsumersAdapter.notifyItemChanged(i);
            }
            if (selectedConsumers.size() == 0) {
                TextView tvConsumers = bottomSheet.findViewById(R.id.textViewConsumers);
                if (tvConsumers != null) {
                    tvConsumers.setVisibility(View.GONE);
                }
            }
        });

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AtomicBoolean undoFlag = new AtomicBoolean(false);
                int position = viewHolder.getAdapterPosition();
                Order orderBackup = orders.get(position);
                orders.remove(position);
                ordersAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                for (int i = position; i < orders.size(); i++) {
                    ordersAdapter.notifyItemChanged(i);
                }
                if (orders.size() == 0) {
                    tvNoOrdersFound.setVisibility(View.VISIBLE);
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setMessage(R.string.are_you_sure_to_delete);
                alertDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    CoordinatorLayout root = findViewById(R.id.rootCoordinatorLayout);
                    Snackbar snackbar = Snackbar.make(root, getString(R.string.order_got_deleted), Snackbar.LENGTH_LONG);
                    BaseTransientBottomBar.Behavior behavior = new BaseTransientBottomBar.Behavior();
                    behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
                    snackbar.setBehavior(behavior);
                    snackbar.setAction(R.string.cancel, view -> {
                        addOrderToAdapter(orderBackup);
                        undoFlag.set(true);
                    });
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (!undoFlag.get()) {
                                dbHelper.deleteOrderById(orderBackup.getId());
                            }
                        }
                    });
                    snackbar.show();
                });
                alertDialog.setNegativeButton(R.string.no, (dialogInterface, i) -> addOrderToAdapter(orderBackup));
                alertDialog.setOnCancelListener(dialogInterface -> addOrderToAdapter(orderBackup));
                alertDialog.show();
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvOrders);

        startActivityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if ((result.getResultCode() & 8) > 0) {
                        LocaleHelper.setLocale(MainActivity.this, sharedPreferences.getString(getString(R.string.locale), "en"));
                        recreate();
                    }
                    if ((result.getResultCode() & 4) > 0) {
                        switch (sharedPreferences.getInt(getString(R.string.settings_default_order), DatabaseHelper.ORDER_MODE_TIME_DESC)) {
                            case DatabaseHelper.ORDER_MODE_TIME_DESC:
                                toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_time_desc));
                                dbHelper.setToOrderByTimeDesc();
                                break;
                            case DatabaseHelper.ORDER_MODE_TIME_ASC:
                                toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_time_asc));
                                dbHelper.setToOrderByTimeAsc();
                                break;
                            case DatabaseHelper.ORDER_MODE_PRICE_DESC:
                                toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_price_desc));
                                dbHelper.setToOrderByPriceDesc();
                                break;
                            case DatabaseHelper.ORDER_MODE_PRICE_ASC:
                                toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_price_asc));
                                dbHelper.setToOrderByPriceAsc();
                                break;
                        }
                    }
                    if ((result.getResultCode() & 2) > 0) {
                        refreshAdapters();
                    }
                    if ((result.getResultCode() & 1) > 0) {
                        refreshOrdersList();
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void refreshBottomSheetHeight() {
        behavior.setPeekHeight(sheetView.getHeight());
    }

    private void refreshOrdersList() {
        orders = dbHelper.getAllOrders();
        if (orders == null) {
            Toast.makeText(MainActivity.this, R.string.an_error_has_occurred, Toast.LENGTH_SHORT).show();
        } else {
            ordersAdapter = new OrdersAdapter(MainActivity.this, orders);
            rvOrders.setAdapter(ordersAdapter);
            if (orders.isEmpty()) {
                tvNoOrdersFound.setVisibility(View.VISIBLE);
            } else {
                tvNoOrdersFound.setVisibility(View.GONE);
            }
        }
    }

    private void refreshAdapters() {
        residents = dbHelper.getAllResidents(true);
        residentNames = new ArrayList<>();
        for (int i = 0; i < residents.size(); i++) {
            residentNames.add(residents.get(i).getName());
        }
        buyerAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1,
                residentNames);
        acTvBuyer.setAdapter(buyerAdapter);
        availableConsumers = new ArrayList<>(residentNames);
        availableConsumersAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1,
                availableConsumers);
        acTvConsumer.setAdapter(availableConsumersAdapter);
        for (int i = 0; i < selectedConsumers.size(); i++) {
            availableConsumersAdapter.remove(selectedConsumers.get(i));
        }
    }

    private void addResidentToList(ArrayList<String> list, String name) {
        if (list.size() == 0) {
            list.add(name);
        } else if (name.compareTo(list.get(0)) < 0) {
            list.add(0, name);
        } else if (name.compareTo(list.get(list.size() - 1)) > 0) {
            list.add(list.size(), name);
        } else {
            for (int i = 0; i < list.size() - 1; i++) {
                if (name.compareTo(list.get(i)) > 0 && name.compareTo(list.get(i + 1)) < 0) {
                    list.add(i + 1, name);
                    break;
                }
            }
        }
    }

    private void addOrderToAdapter(Order newOrder) {
        if (orders.size() == 0) {
            tvNoOrdersFound.setVisibility(View.GONE);
            orders.add(newOrder);
            ordersAdapter.notifyItemInserted(0);
        } else if (newOrder.compareTo(orders.get(0)) > 0) {
            orders.add(0, newOrder);
            ordersAdapter.notifyItemInserted(0);
            for (int i = 1; i < orders.size(); i++) {
                ordersAdapter.notifyItemChanged(i);
            }
        } else if (newOrder.compareTo(orders.get(orders.size() - 1)) < 0) {
            orders.add(orders.size(), newOrder);
            ordersAdapter.notifyItemInserted(orders.size() - 1);
        } else {
            for (int i = 0; i < orders.size() - 1; i++) {
                if (newOrder.compareTo(orders.get(i)) < 0 &&
                        newOrder.compareTo(orders.get(i + 1)) > 0) {
                    orders.add(i + 1, newOrder);
                    ordersAdapter.notifyItemInserted(i + 1);
                    for (int j = i + 1; j < orders.size(); j++) {
                        ordersAdapter.notifyItemChanged(j);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuItemFilter) {
            Intent intent = new Intent(MainActivity.this, FilterOrdersActivity.class);
            startActivityForResult.launch(intent);
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.menuOrderByTimeDesc) {
            dbHelper.setToOrderByTimeDesc();
            toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_time_desc));
            refreshOrdersList();
        } else if (id == R.id.menuOrderByTimeAsc) {
            dbHelper.setToOrderByTimeAsc();
            toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_time_asc));
            refreshOrdersList();
        } else if (id == R.id.menuOrderByPriceDesc) {
            dbHelper.setToOrderByPriceDesc();
            toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_price_desc));
            refreshOrdersList();
        } else if (id == R.id.menuOrderByPriceAsc) {
            dbHelper.setToOrderByPriceAsc();
            toolbar.setOverflowIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_order_price_asc));
            refreshOrdersList();
        }
        return super.onOptionsItemSelected(item);
    }
}