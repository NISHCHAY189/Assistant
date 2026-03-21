package com.example.khaatabook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BillFragment extends Fragment {

    private RecyclerView rvCustomers;
    private Button btnSimple, btnDetailed, btnFancy;
    private String selectedTemplate = "simple";
    private List<Customer> customerList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bill, container, false);

        initializeViews(view);
        setupTemplateButtons();
        setupRecyclerView();

        return view;
    }

    private void initializeViews(View view) {
        rvCustomers = view.findViewById(R.id.rv_bill_customers);
        btnSimple = view.findViewById(R.id.btn_template_simple);
        btnDetailed = view.findViewById(R.id.btn_template_detailed);
        btnFancy = view.findViewById(R.id.btn_template_fancy);
    }

    private void setupTemplateButtons() {
        btnSimple.setOnClickListener(v -> updateTemplate("simple"));
        btnDetailed.setOnClickListener(v -> updateTemplate("detailed"));
        btnFancy.setOnClickListener(v -> updateTemplate("fancy"));
    }

    private void updateTemplate(String template) {
        selectedTemplate = template;
        
        int activeBg = R.drawable.bg_button_primary;
        int inactiveBg = R.drawable.bg_button_secondary;
        int activeText = getResources().getColor(R.color.white);
        int inactiveText = getResources().getColor(R.color.text_dark);

        btnSimple.setBackgroundResource(template.equals("simple") ? activeBg : inactiveBg);
        btnSimple.setTextColor(template.equals("simple") ? activeText : inactiveText);

        btnDetailed.setBackgroundResource(template.equals("detailed") ? activeBg : inactiveBg);
        btnDetailed.setTextColor(template.equals("detailed") ? activeText : inactiveText);

        btnFancy.setBackgroundResource(template.equals("fancy") ? activeBg : inactiveBg);
        btnFancy.setTextColor(template.equals("fancy") ? activeText : inactiveText);
    }

    private void setupRecyclerView() {
        if (getActivity() instanceof MainActivity) {
            customerList = ((MainActivity) getActivity()).getCustomerList();
        } else {
            customerList = new ArrayList<>();
        }

        CustomerAdapter adapter = new CustomerAdapter(customerList, this::showBillPreview);
        rvCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCustomers.setAdapter(adapter);
    }

    private void showBillPreview(Customer customer) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bill_preview, null);
        
        TextView tvShopName = dialogView.findViewById(R.id.tv_bill_shop_name);
        TextView tvCustomerName = dialogView.findViewById(R.id.tv_bill_customer_name);
        TextView tvCustomerPhone = dialogView.findViewById(R.id.tv_bill_customer_phone);
        TextView tvDate = dialogView.findViewById(R.id.tv_bill_date);
        LinearLayout containerItems = dialogView.findViewById(R.id.container_bill_items);
        TextView tvTotalBilled = dialogView.findViewById(R.id.tv_bill_total_billed);
        TextView tvTotalPaid = dialogView.findViewById(R.id.tv_bill_total_paid);
        TextView tvOutstanding = dialogView.findViewById(R.id.tv_bill_outstanding);
        TextView tvFooter = dialogView.findViewById(R.id.tv_bill_footer);

        // Header
        tvShopName.setText("KOTHARI SUPER SHOPEE");
        tvCustomerName.setText(customer.getName());
        tvCustomerPhone.setText(customer.getPhone());
        tvDate.setText(new java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new java.util.Date()));

        // Transactions
        List<Transaction> allTransactions = ((MainActivity) getActivity()).getTransactionList();
        int totalLend = 0;
        int totalPaid = 0;

        containerItems.removeAllViews();
        for (Transaction t : allTransactions) {
            if (t.getCustomerId() == customer.getId()) {
                if ("lend".equals(t.getType())) {
                    totalLend += t.getTotal();
                    addItemRow(containerItems, t.getItem() + " (" + t.getQty() + t.getUnit() + ")", "₹" + t.getTotal());
                } else {
                    totalPaid += t.getTotal();
                }
            }
        }

        tvTotalBilled.setText(String.format(Locale.getDefault(), "₹%d", totalLend));
        tvTotalPaid.setText(String.format(Locale.getDefault(), "₹%d", totalPaid));
        tvOutstanding.setText(String.format(Locale.getDefault(), "₹%d", customer.getOutstanding()));

        // Fancy template styling
        if ("fancy".equals(selectedTemplate)) {
            tvShopName.setTextColor(getResources().getColor(R.color.action_bill));
            tvFooter.setVisibility(View.VISIBLE);
        } else {
            tvFooter.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("📤 Share", (dialog, which) -> {
                    Toast.makeText(getContext(), "Sharing bill...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void addItemRow(LinearLayout container, String name, String amount) {
        View row = getLayoutInflater().inflate(R.layout.item_bill_row, null);
        ((TextView) row.findViewById(R.id.tv_row_name)).setText(name);
        ((TextView) row.findViewById(R.id.tv_row_amount)).setText(amount);
        container.addView(row);
    }
}
