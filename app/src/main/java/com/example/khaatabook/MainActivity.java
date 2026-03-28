package com.example.khaatabook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.khaatabook.voice.VoiceInputManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private TextView btnBack;
    private Button btnVoiceHeader;
    private TextView tvTitle;
    private TextView btnNavHome, btnNavTransactions, btnNavVoice, btnNavCustomers, btnNavBills;

    private List<Customer> customerList;
    private List<Transaction> transactionList;

    private Fragment currentFragment;
    private String currentScreen = "home";

    private VoiceInputManager voiceInputManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupData();
        setupNavigation();
        setupVoiceInput();
        showHome();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        btnVoiceHeader = findViewById(R.id.btn_voice_header);
        tvTitle = findViewById(R.id.tv_title);
        btnNavHome = findViewById(R.id.btn_nav_home);
        btnNavTransactions = findViewById(R.id.btn_nav_transactions);
        btnNavVoice = findViewById(R.id.btn_nav_voice);
        btnNavCustomers = findViewById(R.id.btn_nav_customers);
        btnNavBills = findViewById(R.id.btn_nav_bills);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnVoiceHeader.setOnClickListener(v -> showVoice());
    }

    private void setupData() {
        customerList = new ArrayList<>();
        customerList.add(new Customer(1, "Ramesh Kumar", "9876543210", 300));
        customerList.add(new Customer(2, "Suresh Patel", "9823456789", 85));
        customerList.add(new Customer(3, "Anita Sharma", "9012345678", 180));

        transactionList = new ArrayList<>();
        transactionList.add(new Transaction(1, 1, "lend", "Rice", 5, "kg", 60, 300, System.currentTimeMillis(), "Initial Entry"));
    }

    private void setupNavigation() {
        btnNavHome.setOnClickListener(v -> showHome());
        btnNavTransactions.setOnClickListener(v -> showTransactions());
        btnNavVoice.setOnClickListener(v -> showVoice());
        btnNavCustomers.setOnClickListener(v -> showCustomers());
        btnNavBills.setOnClickListener(v -> showBill());
    }

    private void setupVoiceInput() {
        voiceInputManager = new VoiceInputManager();
        
        // Check for permissions
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            voiceInputManager.init(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceInputManager.init(this);
            } else {
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public VoiceInputManager getVoiceInputManager() {
        return voiceInputManager;
    }

    public void showHome() {
        currentScreen = "home";
        updateUI();
        replaceFragment(new HomeFragment());
    }

    public void showTransactions() {
        currentScreen = "transactions";
        updateUI();
        replaceFragment(new TransactionsFragment());
    }

    public void showVoice() {
        currentScreen = "voice";
        updateUI();
        replaceFragment(new VoiceFragment());
    }

    public void showCustomers() {
        currentScreen = "customers";
        updateUI();
        replaceFragment(new CustomersFragment());
    }

    public void showBill() {
        currentScreen = "bill";
        updateUI();
        replaceFragment(new BillFragment());
    }

    public void showCustomerDetails(Customer customer) {
        currentScreen = "customer_details";
        updateUI();

        CustomerDetailsFragment fragment = new CustomerDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("customer", customer);
        fragment.setArguments(args);

        replaceFragment(fragment);
    }

    public void showBillModal(Customer customer, String template) {
        Toast.makeText(this, "Generating " + template + " bill for " + customer.getName(), Toast.LENGTH_LONG).show();
    }

    private void updateUI() {
        tvTitle.setText(getTitleForScreen(currentScreen));
        btnBack.setVisibility(currentScreen.equals("home") ? View.GONE : View.VISIBLE);
        btnVoiceHeader.setVisibility(currentScreen.equals("home") ? View.VISIBLE : View.GONE);

        // Update nav button colors
        int activeColor = getResources().getColor(R.color.action_voice);
        int inactiveColor = getResources().getColor(R.color.text_muted);

        btnNavHome.setTextColor(currentScreen.equals("home") ? activeColor : inactiveColor);
        btnNavTransactions.setTextColor(currentScreen.equals("transactions") ? activeColor : inactiveColor);
        btnNavVoice.setTextColor(currentScreen.equals("voice") ? activeColor : inactiveColor);
        btnNavCustomers.setTextColor(currentScreen.equals("customers") ? activeColor : inactiveColor);
        btnNavBills.setTextColor(currentScreen.equals("bill") ? activeColor : inactiveColor);
    }

    private String getTitleForScreen(String screen) {
        switch (screen) {
            case "home": return "🏪 Udhar Khata";
            case "transactions": return "💸 All Transactions";
            case "voice": return "🎙️ Voice Entry";
            case "customers": return "👥 Customers";
            case "bill": return "📄 Generate Bill";
            case "customer_details": return "Customer Details";
            default: return "🏪 Udhar Khata";
        }
    }

    private void replaceFragment(Fragment fragment) {
        currentFragment = fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void processVoiceResult(VoiceParser.ParseResult result) {
        if ("new_customer".equals(result.type)) {
            Customer c = new Customer(customerList.size() + 1, result.customerName, result.phone, 0);
            customerList.add(c);
        } else if ("lend".equals(result.type) && result.customerId != null) {
            int total = (result.price != null && result.qty != null) ? (int)(result.price * result.qty) : (result.amount != null ? result.amount : 0);
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
        Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
        
        // Refresh fragment if on home or transactions
        if (currentScreen.equals("home") || currentScreen.equals("transactions")) {
            if (currentScreen.equals("home")) showHome();
            else showTransactions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceInputManager != null) {
            voiceInputManager.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (!currentScreen.equals("home")) {
            showHome();
        } else {
            super.onBackPressed();
        }
    }
}
