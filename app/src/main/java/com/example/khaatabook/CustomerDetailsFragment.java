package com.example.khaatabook;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerDetailsFragment extends Fragment {

    private TextView tvAvatar, tvName, tvPhone, tvBalance;
    private RecyclerView rvTransactions;
    private ExtendedFloatingActionButton fabAdd;
    private TransactionAdapter adapter;
    private List<Transaction> customerTransactions;
    private Customer selectedCustomer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_details, container, false);

        if (getArguments() != null) {
            selectedCustomer = (Customer) getArguments().getSerializable("customer");
        }

        if (selectedCustomer == null) {
            // Go back if no customer selected
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showHome();
            }
            return view;
        }

        initializeViews(view);
        setupTransactions();

        return view;
    }

    private void initializeViews(View view) {
        tvAvatar = view.findViewById(R.id.tv_detail_avatar);
        tvName = view.findViewById(R.id.tv_detail_name);
        tvPhone = view.findViewById(R.id.tv_detail_phone);
        tvBalance = view.findViewById(R.id.tv_detail_balance);
        rvTransactions = view.findViewById(R.id.rv_transactions);
        fabAdd = view.findViewById(R.id.fab_add_transaction);

        tvAvatar.setText(String.valueOf(selectedCustomer.getName().charAt(0)));
        tvName.setText(selectedCustomer.getName());
        tvPhone.setText(selectedCustomer.getPhone());
        updateBalanceDisplay(selectedCustomer.getOutstanding());

        fabAdd.setOnClickListener(v -> showAddTransactionDialog());

        Button btnAddPayment = view.findViewById(R.id.btn_add_payment);
        Button btnGenerateBill = view.findViewById(R.id.btn_generate_bill);

        btnAddPayment.setOnClickListener(v -> showAddPaymentDialog());
        btnGenerateBill.setOnClickListener(v -> generateBill());
    }

    private void updateBalanceDisplay(int balance) {
        tvBalance.setText(String.format(Locale.getDefault(), "₹%d", balance));
        if (balance > 0) {
            tvBalance.setTextColor(getResources().getColor(R.color.accent_yellow));
        } else {
            tvBalance.setTextColor(getResources().getColor(R.color.green_success));
        }
    }

    private void setupTransactions() {
        customerTransactions = new ArrayList<>();
        // In a real app, load from database where customerId matches
        adapter = new TransactionAdapter(customerTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(adapter);
    }

    private void showAddTransactionDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        android.widget.RadioGroup rgType = view.findViewById(R.id.rg_type);
        EditText etItem = view.findViewById(R.id.et_item_name);
        EditText etAmount = view.findViewById(R.id.et_amount);

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_payment) {
                etItem.setHint("Note (Optional)");
            } else {
                etItem.setHint("Item Name (e.g. Rice)");
            }
        });

        new AlertDialog.Builder(getContext())
                .setTitle("New Entry for " + selectedCustomer.getName())
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String type = rgType.getCheckedRadioButtonId() == R.id.rb_lend ? "lend" : "payment";
                    String item = etItem.getText().toString();

                    if (!etAmount.getText().toString().isEmpty()) {
                        int amount = Integer.parseInt(etAmount.getText().toString());
                        Transaction t;
                        if (type.equals("lend")) {
                            t = new Transaction(customerTransactions.size() + 1, selectedCustomer.getId(), "lend", item, 1, "pcs", amount, amount, System.currentTimeMillis(), "");
                        } else {
                            t = new Transaction(customerTransactions.size() + 1, selectedCustomer.getId(), "payment", amount, System.currentTimeMillis(), item);
                        }
                        customerTransactions.add(0, t);
                        adapter.notifyItemInserted(0);
                        rvTransactions.scrollToPosition(0);

                        // Update customer balance
                        selectedCustomer.updateBalance(amount, type.equals("lend"));
                        updateBalanceDisplay(selectedCustomer.getOutstanding());

                        Toast.makeText(getContext(), "Transaction Saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddPaymentDialog() {
        // Implementation for adding a payment
    }

    private void generateBill() {
        // Implementation for generating a bill
    }
}
