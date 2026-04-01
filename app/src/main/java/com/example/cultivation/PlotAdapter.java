package com.example.cultivation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cultivation.data.Plot;
import java.util.ArrayList;
import java.util.List;

public class PlotAdapter extends RecyclerView.Adapter<PlotAdapter.PlotViewHolder> {

    private List<Plot> plots = new ArrayList<>();

    public void setPlots(List<Plot> plots) {
        this.plots = plots;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plot, parent, false);
        return new PlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlotViewHolder holder, int position) {
        Plot plot = plots.get(position);

        // Format Name
        if (plot.name.toLowerCase().startsWith("plot")) {
            holder.tvPlotName.setText(plot.name);
        } else {
            holder.tvPlotName.setText("Plot # " + plot.name);
        }

        // Format Area
        if (plot.area.toLowerCase().contains("acres")) {
            holder.tvArea.setText(plot.area);
        } else {
            holder.tvArea.setText(plot.area + " Acres");
        }

        holder.tvCropVariety.setText("Variety: " + plot.cropVariety);

        holder.btnView.setOnClickListener(v -> {
            if (holder.itemView.getContext() instanceof DashboardNavigator) {
                ((DashboardNavigator) holder.itemView.getContext()).openDashboard(plot.name, plot.cropVariety,
                        plot.plantingDate);
            }
        });
    }

    @Override
    public int getItemCount() {
        return plots.size();
    }

    static class PlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlotName, tvCropVariety, tvArea;
        View btnView;

        public PlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlotName = itemView.findViewById(R.id.tvPlotName);
            tvCropVariety = itemView.findViewById(R.id.tvCropVariety);
            tvArea = itemView.findViewById(R.id.tvArea);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
