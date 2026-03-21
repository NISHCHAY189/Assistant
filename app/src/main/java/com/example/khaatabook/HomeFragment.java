package com.example.khaatabook;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvTransactions;
    private EditText etSearch;
    private TextView tvTotalBalance, tvTotalCustomers;
    private Button btnAddCustomer;
    private List<Customer> customerList;
    private List<Transaction> transactionList;
    private List<Transaction> filteredTransactions;
    private TransactionAdapter transactionAdapter;
    private LinearLayout chartContainer;
    private ChipGroup chipGroupFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeData();
        initializeViews(view);
        setupRecyclerView();
        setupSearch();
        setupChartFilters();
        updateSummary();
        updateChart("week");

        return view;
    }

    private void initializeData() {
        if (getActivity() instanceof MainActivity) {
            customerList = ((MainActivity) getActivity()).getCustomerList();
            transactionList = ((MainActivity) getActivity()).getTransactionList();
        } else {
            customerList = new ArrayList<>();
            transactionList = new ArrayList<>();
        }
        filteredTransactions = new ArrayList<>(transactionList);
    }

    private void initializeViews(View view) {
        rvTransactions = view.findViewById(R.id.rv_transactions);
        etSearch = view.findViewById(R.id.et_search);
        tvTotalBalance = view.findViewById(R.id.tv_total_outstanding);
        tvTotalCustomers = view.findViewById(R.id.tv_customer_count);
        btnAddCustomer = view.findViewById(R.id.btn_add_customer);
        chartContainer = view.findViewById(R.id.chart_container);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);

        if (btnAddCustomer != null) {
            btnAddCustomer.setOnClickListener(v -> showAddCustomerDialog());
        }

        // Navigation for Quick Actions
        view.findViewById(R.id.btn_voice).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showVoice();
        });
        view.findViewById(R.id.btn_customers).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showCustomers();
        });
        view.findViewById(R.id.btn_bill).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showBill();
        });
        
        view.findViewById(R.id.btn_view_all).setOnClickListener(v -> {
             if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showBill();
        });
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(filteredTransactions, customerList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.setHint("Search transactions...");
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupChartFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_week) {
                updateChart("week");
            } else if (id == R.id.chip_month) {
                updateChart("month");
            }
        });
    }

    private void filter(String text) {
        filteredTransactions.clear();
        if (text == null || text.isEmpty()) {
            filteredTransactions.addAll(transactionList);
        } else {
            String query = text.toLowerCase();
            for (Transaction t : transactionList) {
                if (t.getItem() != null && t.getItem().toLowerCase().contains(query)) {
                    filteredTransactions.add(t);
                } else if ("payment".equals(t.getType()) && "payment".contains(query)) {
                    filteredTransactions.add(t);
                }
            }
        }
        transactionAdapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        int total = 0;
        for (Customer c : customerList) {
            total += c.getOutstanding();
        }
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%d", total));
        tvTotalCustomers.setText(String.format(Locale.getDefault(), "%d active", customerList.size()));
    }

    private void updateChart(String filter) {
        chartContainer.removeAllViews();
        int days = filter.equals("week") ? 7 : 30;
        
        long now = System.currentTimeMillis();
        long dayMillis = 24 * 60 * 60 * 1000L;
        
        int maxVal = 1000;
        for (Transaction t : transactionList) if (t.getTotal() > maxVal) maxVal = t.getTotal();

        for (int i = days - 1; i >= 0; i--) {
            long startTime = now - (i + 1) * dayMillis;
            long endTime = now - i * dayMillis;
            
            int lendAmount = 0;
            int payAmount = 0;
            
            for (Transaction t : transactionList) {
                if (t.getDate() >= startTime && t.getDate() < endTime) {
                    if ("lend".equals(t.getType())) lendAmount += t.getTotal();
                    else payAmount += t.getTotal();
                }
            }

            addBarToChart(lendAmount, payAmount, maxVal);
        }
    }

    private void addBarToChart(int lend, int pay, int max) {
        LinearLayout barLayout = new LinearLayout(getContext());
        barLayout.setOrientation(LinearLayout.VERTICAL);
        barLayout.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        params.setMargins(4, 0, 4, 0);
        barLayout.setLayoutParams(params);

        if (lend > 0) {
            View lendBar = new View(getContext());
            int height = Math.max(10, (lend * 100) / max); 
            lendBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height * 2));
            lendBar.setBackgroundColor(getResources().getColor(R.color.red_error));
            barLayout.addView(lendBar);
        }

        if (pay > 0) {
            View payBar = new View(getContext());
            int height = Math.max(10, (pay * 100) / max); 
            payBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height * 2));
            payBar.setBackgroundColor(getResources().getColor(R.color.green_success));
            barLayout.addView(payBar);
        }
        
        if (lend == 0 && pay == 0) {
            View emptyBar = new View(getContext());
            emptyBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
            emptyBar.setBackgroundColor(getResources().getColor(R.color.text_muted));
            emptyBar.setAlpha(0.2f);
            barLayout.addView(emptyBar);
        }

        chartContainer.addView(barLayout);
    }

    public void processVoiceCommand(String command) {
        VoiceParser.ParseResult result = VoiceParser.parse(command, customerList);
        if (result != null) {
            if ("new_customer".equals(result.type)) {
                customerList.add(new Customer(customerList.size() + 1, result.customerName, result.phone, 0));
            } else if (result.customerId != null) {
                for (Customer c : customerList) {
                    if (c.getId() == result.customerId) {
                        int amount = 0;
                        if ("lend".equals(result.type)) {
                            amount = (result.price != null && result.qty != null) ? (int)(result.price * result.qty) : (result.amount != null ? result.amount : 0);
                            c.updateBalance(amount, true);
                        } else if ("payment".equals(result.type)) {
                            amount = result.amount != null ? result.amount : 0;
                            c.updateBalance(amount, false);
                        }
                        
                        if (getActivity() instanceof MainActivity) {
                            MainActivity activity = (MainActivity) getActivity();
                            int nextId = activity.getTransactionList().size() + 1;
                            Transaction t;
                            if ("lend".equals(result.type)) {
                                t = new Transaction(nextId, c.getId(), "lend", result.item, result.qty, result.unit, result.price, amount, System.currentTimeMillis(), "Voice Entry");
                            } else {
                                t = new Transaction(nextId, c.getId(), "payment", amount, System.currentTimeMillis(), "Voice Payment");
                            }
                            activity.getTransactionList().add(t);
                        }
                        break;
                    }
                }
            }
            filter(etSearch.getText().toString());
            updateSummary();
            updateChart(chipGroupFilter.getCheckedChipId() == R.id.chip_month ? "month" : "week");
            Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddCustomerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Create Account", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (!name.isEmpty()) {
                        customerList.add(new Customer(customerList.size() + 1, name, phone, 0));
                        updateSummary();
                        Toast.makeText(getContext(), "Customer added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Name is required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
