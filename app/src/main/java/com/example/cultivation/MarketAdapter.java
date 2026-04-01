package com.example.cultivation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cultivation.data.MarketRate;
import java.util.List;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.MarketViewHolder> {

    private List<MarketRate> rates;

    public MarketAdapter(List<MarketRate> rates) {
        this.rates = rates;
    }

    @NonNull
    @Override
    public MarketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_market, parent, false);
        return new MarketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarketViewHolder holder, int position) {
        MarketRate rate = rates.get(position);
        holder.tvLocation.setText(rate.location);
        holder.tvPrice.setText(rate.price);

        if ("up".equalsIgnoreCase(rate.trend)) {
            holder.ivTrend.setImageResource(android.R.drawable.arrow_up_float);
            holder.ivTrend.setColorFilter(0xFF2ECC71); // Green
            holder.tvTrend.setText("Rising");
        } else if ("down".equalsIgnoreCase(rate.trend)) {
            holder.ivTrend.setImageResource(android.R.drawable.arrow_down_float);
            holder.ivTrend.setColorFilter(0xFFE74C3C); // Red
            holder.tvTrend.setText("Falling");
        } else {
            holder.ivTrend.setImageResource(android.R.drawable.ic_menu_sort_by_size); // Flatish icon
            holder.ivTrend.setColorFilter(0xFF95A5A6); // Gray
            holder.tvTrend.setText("Stable");
        }
    }

    @Override
    public int getItemCount() {
        return rates.size();
    }

    static class MarketViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation, tvPrice, tvTrend;
        ImageView ivTrend;

        public MarketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTrend = itemView.findViewById(R.id.tvTrend);
            ivTrend = itemView.findViewById(R.id.ivTrend);
        }
    }
}
