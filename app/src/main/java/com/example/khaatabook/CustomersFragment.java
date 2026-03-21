package com.example.khaatabook;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CustomersFragment extends Fragment {

    private EditText etSearch;
    private Button btnAddCustomer;
    private RecyclerView rvCustomers;
    private CustomerAdapter adapter;
    private List<Customer> customerList;
    private List<Customer> filteredList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customers, container, false);

        initializeViews(view);
        setupData();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        etSearch = view.findViewById(R.id.et_search_customers);
        btnAddCustomer = view.findViewById(R.id.btn_add_customer_page);
        rvCustomers = view.findViewById(R.id.rv_all_customers);
    }

    private void setupData() {
        if (getActivity() instanceof MainActivity) {
            customerList = ((MainActivity) getActivity()).getCustomerList();
        } else {
            customerList = new ArrayList<>();
        }

        filteredList = new ArrayList<>(customerList);
        adapter = new CustomerAdapter(filteredList, this::openCustomerDetails);

        rvCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCustomers.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAddCustomer.setOnClickListener(v -> showAddCustomerDialog());

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
            String query = text.toLowerCase().trim();
            for (Customer customer : customerList) {
                if (customer.getName().toLowerCase().contains(query) || 
                    customer.getPhone().contains(query)) {
                    filteredList.add(customer);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openCustomerDetails(Customer customer) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showCustomerDetails(customer);
        }
    }

    private void showAddCustomerDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etPhone = view.findViewById(R.id.et_phone);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Customer")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (!name.isEmpty()) {
                        Customer newCustomer = new Customer(customerList.size() + 1, name, phone, 0);
                        customerList.add(newCustomer);
                        filter(etSearch.getText().toString());
                        Toast.makeText(getContext(), "Customer added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
