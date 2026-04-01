package com.example.cultivation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cultivation.data.ScanLog;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<ScanLog> logs = new ArrayList<>();

    public void setLogs(List<ScanLog> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanLog log = logs.get(position);
        holder.tvDiseaseName.setText(log.disease);
        holder.tvDate.setText(log.date);
        holder.tvConfidenceScore.setText(String.format("Confidence: %.1f%%", log.confidence * 100));
        // Image loading logic would go here
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiseaseName, tvDate, tvConfidenceScore;
        ImageView ivThumb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiseaseName = itemView.findViewById(R.id.tvDiseaseName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvConfidenceScore = itemView.findViewById(R.id.tvConfidenceScore);
            ivThumb = itemView.findViewById(R.id.ivThumb);
        }
    }
}
