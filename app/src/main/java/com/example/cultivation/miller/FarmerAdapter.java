package com.example.cultivation.miller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cultivation.R;
import java.util.List;

public class FarmerAdapter extends RecyclerView.Adapter<FarmerAdapter.FarmerViewHolder> {

    private List<FarmerItem> farmers;
    private OnConnectClickListener listener;

    public interface OnConnectClickListener {
        void onConnectClick(FarmerItem farmer);
    }

    public FarmerAdapter(List<FarmerItem> farmers, OnConnectClickListener listener) {
        this.farmers = farmers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FarmerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_farmer, parent, false);
        return new FarmerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FarmerViewHolder holder, int position) {
        FarmerItem farmer = farmers.get(position);
        holder.tvName.setText(farmer.name);
        holder.tvLocation.setText(farmer.location);

        if (farmer.isConnected) {
            holder.btnConnect.setText("Request Sent");
            holder.btnConnect.setEnabled(false);
            holder.btnConnect.setBackgroundColor(0xFF95A5A6); // Gray
        } else {
            holder.btnConnect.setText("Connect");
            holder.btnConnect.setEnabled(true);
            holder.btnConnect.setBackgroundColor(0xFF3498DB); // Blue
        }

        holder.btnConnect.setOnClickListener(v -> listener.onConnectClick(farmer));
    }

    @Override
    public int getItemCount() {
        return farmers.size();
    }

    static class FarmerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation;
        Button btnConnect;

        public FarmerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFarmerName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnConnect = itemView.findViewById(R.id.btnConnect);
        }
    }
}
