package com.example.khaatabook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        
        holder.tvDate.setText(dateFormat.format(new Date(t.getDate())));
        
        if ("lend".equals(t.getType())) {
            holder.tvTitle.setText(t.getItem());
            holder.tvSubtitle.setText(String.format(Locale.getDefault(), "%.1f %s x ₹%d", t.getQty(), t.getUnit(), t.getPrice()));
            holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%d", t.getTotal()));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red_error));
        } else {
            holder.tvTitle.setText("Payment Received");
            holder.tvSubtitle.setText(t.getNote() != null ? t.getNote() : "Cash/UPI");
            holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%d", t.getTotal()));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green_success));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvAmount, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_trans_title);
            tvSubtitle = itemView.findViewById(R.id.tv_trans_subtitle);
            tvAmount = itemView.findViewById(R.id.tv_trans_amount);
            tvDate = itemView.findViewById(R.id.tv_trans_date);
        }
    }
}
