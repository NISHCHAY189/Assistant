package com.example.khaatabook;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionsFragment extends Fragment {

    private TextView tvTotalCount, tvTotalVolume;
    private RecyclerView rvTransactions;
    private ChipGroup cgTypeFilter;
    private TextView btnDateFilter;
    
    private List<Transaction> allTransactions;
    private List<Customer> customerList;
    private List<Transaction> filteredTransactions;
    private TransactionAdapter adapter;
    
    private String currentType = "all";
    private Calendar selectedDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        initializeData();
        initializeViews(view);
        setupRecyclerView();
        setupFilters();
        updateSummary();

        return view;
    }

    private void initializeData() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            allTransactions = activity.getTransactionList();
            customerList = activity.getCustomerList();
        } else {
            allTransactions = new ArrayList<>();
            customerList = new ArrayList<>();
        }
        filteredTransactions = new ArrayList<>(allTransactions);
    }

    private void initializeViews(View view) {
        tvTotalCount = view.findViewById(R.id.tv_total_count);
        tvTotalVolume = view.findViewById(R.id.tv_total_volume);
        rvTransactions = view.findViewById(R.id.rv_all_transactions);
        cgTypeFilter = view.findViewById(R.id.cg_type_filter);
        btnDateFilter = view.findViewById(R.id.btn_date_filter);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(filteredTransactions, customerList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(adapter);
    }

    private void setupFilters() {
        cgTypeFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_all) currentType = "all";
            else if (id == R.id.chip_lend) currentType = "lend";
            else if (id == R.id.chip_payment) currentType = "payment";
            applyFilters();
        });

        btnDateFilter.setOnClickListener(v -> {
            Calendar c = selectedDate != null ? selectedDate : Calendar.getInstance();
            if (getContext() != null) {
                new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    applyFilters();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        
        btnDateFilter.setOnLongClickListener(v -> {
            selectedDate = null;
            applyFilters();
            Toast.makeText(getContext(), "Date filter cleared", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void applyFilters() {
        filteredTransactions.clear();
        for (Transaction t : allTransactions) {
            boolean typeMatch = currentType.equals("all") || t.getType().equals(currentType);
            boolean dateMatch = true;
            
            if (selectedDate != null) {
                Calendar tDate = Calendar.getInstance();
                tDate.setTimeInMillis(t.getDate());
                dateMatch = tDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                            tDate.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR);
            }
            
            if (typeMatch && dateMatch) {
                filteredTransactions.add(t);
            }
        }
        adapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateSummary() {
        int count = filteredTransactions.size();
        int volume = 0;
        for (Transaction t : filteredTransactions) {
            volume += t.getTotal();
        }
        
        tvTotalCount.setText(String.valueOf(count));
        tvTotalVolume.setText(String.format(Locale.getDefault(), "₹%d", volume));
    }
}
