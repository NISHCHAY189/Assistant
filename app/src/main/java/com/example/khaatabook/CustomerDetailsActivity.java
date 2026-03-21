package com.example.khaatabook;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerDetailsActivity extends AppCompatActivity {

    private TextView tvAvatar, tvName, tvPhone, tvBalance;
    private RecyclerView rvTransactions;
    private ExtendedFloatingActionButton fabAdd;
    private TransactionAdapter adapter;
    private List<Transaction> customerTransactions;
    private int customerId;
    private String customerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        customerId = getIntent().getIntExtra("customer_id", -1);
        customerName = getIntent().getStringExtra("customer_name");
        String phone = getIntent().getStringExtra("customer_phone");
        int balance = getIntent().getIntExtra("customer_balance", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvAvatar = findViewById(R.id.tv_detail_avatar);
        tvName = findViewById(R.id.tv_detail_name);
        tvPhone = findViewById(R.id.tv_detail_phone);
        tvBalance = findViewById(R.id.tv_detail_balance);
        rvTransactions = findViewById(R.id.rv_transactions);
        fabAdd = findViewById(R.id.fab_add_transaction);

        tvAvatar.setText(String.valueOf(customerName.charAt(0)));
        tvName.setText(customerName);
        tvPhone.setText(phone);
        updateBalanceDisplay(balance);

        setupTransactions();
        
        fabAdd.setOnClickListener(v -> showAddTransactionDialog());
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
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void showAddTransactionDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        RadioGroup rgType = view.findViewById(R.id.rg_type);
        EditText etItem = view.findViewById(R.id.et_item_name);
        EditText etAmount = view.findViewById(R.id.et_amount);
        
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_payment) {
                etItem.setHint("Note (Optional)");
            } else {
                etItem.setHint("Item Name (e.g. Rice)");
            }
        });

        new AlertDialog.Builder(this)
            .setTitle("New Entry for " + customerName)
            .setView(view)
            .setPositiveButton("Save", (dialog, which) -> {
                String type = rgType.getCheckedRadioButtonId() == R.id.rb_lend ? "lend" : "payment";
                String item = etItem.getText().toString();
                String amountStr = etAmount.getText().toString();
                
                if (!amountStr.isEmpty()) {
                    int amount = Integer.parseInt(amountStr);
                    Transaction t;
                    if (type.equals("lend")) {
                        t = new Transaction(customerTransactions.size() + 1, customerId, "lend", item, 1, "pcs", amount, amount, System.currentTimeMillis(), "");
                    } else {
                        t = new Transaction(customerTransactions.size() + 1, customerId, "payment", amount, System.currentTimeMillis(), item);
                    }
                    customerTransactions.add(0, t);
                    adapter.notifyItemInserted(0);
                    rvTransactions.scrollToPosition(0);
                    
                    // Logic to update customer balance would go here
                    Toast.makeText(this, "Transaction Saved", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
