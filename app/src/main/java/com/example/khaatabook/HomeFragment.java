package com.example.khaatabook;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvDate, tvTotalOutstanding, tvStatsSummary, tvCustomerCount;
    private CardView btnVoice, btnCustomers, btnReports, btnBill;
    private Button btnAddCustomer;
    private EditText etSearch;
    private RecyclerView rvCustomers;

    private CustomerAdapter adapter;
    private List<Customer> customerList;
    private List<Customer> filteredList;
    private List<Transaction> transactionList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupData();
        setupClickListeners();
        setupSearch();

        return view;
    }

    private void initializeViews(View view) {
        tvDate = view.findViewById(R.id.tv_date);
        tvTotalOutstanding = view.findViewById(R.id.tv_total_outstanding);
        tvStatsSummary = view.findViewById(R.id.tv_stats_summary);
        tvCustomerCount = view.findViewById(R.id.tv_customer_count);

        btnVoice = view.findViewById(R.id.btn_voice);
        btnCustomers = view.findViewById(R.id.btn_customers);
        btnReports = view.findViewById(R.id.btn_reports);
        btnBill = view.findViewById(R.id.btn_bill);

        btnAddCustomer = view.findViewById(R.id.btn_add_customer);
        etSearch = view.findViewById(R.id.et_search);
        rvCustomers = view.findViewById(R.id.rv_customers);
    }

    private void setupData() {
        if (getActivity() instanceof MainActivity) {
            customerList = ((MainActivity) getActivity()).getCustomerList();
            transactionList = ((MainActivity) getActivity()).getTransactionList();
        } else {
            customerList = new ArrayList<>();
            transactionList = new ArrayList<>();
        }

        filteredList = new ArrayList<>(customerList);
        adapter = new CustomerAdapter(filteredList, this::openCustomerDetails);

        rvCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCustomers.setAdapter(adapter);

        updateSummary();
    }

    private void openCustomerDetails(Customer customer) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showCustomerDetails(customer);
        }
    }

    private void updateSummary() {
        int total = 0;
        for (Customer c : customerList) {
            total += c.getOutstanding();
        }
        tvTotalOutstanding.setText(String.format(Locale.getDefault(), "₹%d", total));
        tvCustomerCount.setText(String.format(Locale.getDefault(), "%d active", customerList.size()));
        tvStatsSummary.setText(String.format(Locale.getDefault(), "%d customers • %d transactions", customerList.size(), transactionList.size()));
    }

    private void setupClickListeners() {
        btnVoice.setOnClickListener(v -> startVoiceSimulation());
        btnBill.setOnClickListener(v -> {
            if (customerList.isEmpty()) return;
            showBillTemplateSelection(customerList.get(0));
        });
        btnAddCustomer.setOnClickListener(v -> showAddCustomerDialog());
    }

    private void setupSearch() {
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

    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(customerList);
        } else {
            for (Customer item : customerList) {
                if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void startVoiceSimulation() {
        String simulatedVoice = "Ramesh ne 2 kg dal liya 120 rupaye";
        VoiceParser.ParseResult result = VoiceParser.parse(simulatedVoice, customerList);

        new AlertDialog.Builder(getContext())
                .setTitle("Voice Action Detected")
                .setMessage("Confirm action: " + result.toString())
                .setPositiveButton("Confirm", (dialog, which) -> processVoiceResult(result))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processVoiceResult(VoiceParser.ParseResult result) {
        if ("new_customer".equals(result.type)) {
            Customer c = new Customer(customerList.size() + 1, result.customerName, result.phone, 0);
            customerList.add(c);
        } else if ("lend".equals(result.type) && result.customerId != null) {
            int total = (result.price != null && result.qty != null) ? (int)(result.price * result.qty) : 0;
            transactionList.add(new Transaction(transactionList.size() + 1, result.customerId, "lend", result.item, result.qty, result.unit, result.price, total, System.currentTimeMillis(), "Voice Entry"));
            for (Customer c : customerList) {
                if (c.getId() == result.customerId) {
                    c.updateBalance(total, true);
                    break;
                }
            }
        } else if ("payment".equals(result.type) && result.customerId != null) {
            int amount = result.amount != null ? result.amount : 0;
            transactionList.add(new Transaction(transactionList.size() + 1, result.customerId, "payment", amount, System.currentTimeMillis(), "Voice Payment"));
            for (Customer c : customerList) {
                if (c.getId() == result.customerId) {
                    c.updateBalance(amount, false);
                    break;
                }
            }
        }
        filter(etSearch.getText().toString());
        updateSummary();
        Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private void showAddCustomerDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etPhone = view.findViewById(R.id.et_phone);

        new AlertDialog.Builder(getContext())
                .setTitle("Add New Customer")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String phone = etPhone.getText().toString();
                    if (!name.isEmpty()) {
                        customerList.add(new Customer(customerList.size() + 1, name, phone, 0));
                        filter(etSearch.getText().toString());
                        updateSummary();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showBillTemplateSelection(Customer customer) {
        String[] templates = {"Simple Bill", "Detailed Bill", "Fancy Bill"};
        new AlertDialog.Builder(getContext())
                .setTitle("Select Bill Template")
                .setItems(templates, (dialog, which) -> {
                    Toast.makeText(getContext(), "Generating " + templates[which] + " for " + customer.getName(), Toast.LENGTH_LONG).show();
                })
                .show();
    }
}
