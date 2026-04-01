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

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder> {

    private List<FarmerItem> farmers;
    private OnViewFarmClickListener listener;

    public interface OnViewFarmClickListener {
        void onViewFarm(FarmerItem farmer);
    }

    public ConnectionAdapter(List<FarmerItem> farmers, OnViewFarmClickListener listener) {
        this.farmers = farmers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_connection, parent, false);
        return new ConnectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionViewHolder holder, int position) {
        FarmerItem farmer = farmers.get(position);
        if (farmer.name != null) {
            holder.tvName.setText(farmer.name);
        } else {
            holder.tvName.setText("Unknown Farmer");
        }
        holder.btnViewFarm.setOnClickListener(v -> listener.onViewFarm(farmer));
    }

    @Override
    public int getItemCount() {
        return farmers.size();
    }

    static class ConnectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnViewFarm;

        public ConnectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvConnectionName);
            btnViewFarm = itemView.findViewById(R.id.btnViewFarm);
        }
    }
}
