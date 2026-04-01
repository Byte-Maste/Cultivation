package com.example.cultivation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<ConnectionRequest> requests;
    private OnAcceptClickListener listener;

    public interface OnAcceptClickListener {
        void onAccept(ConnectionRequest request);
    }

    public RequestAdapter(List<ConnectionRequest> requests, OnAcceptClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        ConnectionRequest req = requests.get(position);
        holder.tvName.setText(req.fromName != null ? req.fromName : "Unknown Miller");
        holder.btnAccept.setOnClickListener(v -> listener.onAccept(req));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnAccept;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMillerName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }
}
