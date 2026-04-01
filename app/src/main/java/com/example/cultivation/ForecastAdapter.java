package com.example.cultivation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cultivation.data.ForecastResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastResponse.ForecastItem> forecastList;

    public ForecastAdapter(List<ForecastResponse.ForecastItem> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastResponse.ForecastItem item = forecastList.get(position);

        // Date Format: "2023-10-25 12:00:00" -> "Wed"
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        try {
            Date date = inFormat.parse(item.dt_txt);
            holder.tvDay.setText(outFormat.format(date));
        } catch (Exception e) {
            holder.tvDay.setText("Day");
        }

        // Temp
        holder.tvTemp.setText(Math.round(item.main.temp) + "°");

        // Condition
        if (item.weather != null && !item.weather.isEmpty()) {
            String condition = item.weather.get(0).main;
            holder.tvCondition.setText(condition);

            // Simple Icon logic
            String lowerCondition = condition.toLowerCase();
            if (lowerCondition.contains("rain") || lowerCondition.contains("drizzle")) {
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_compass);
                holder.ivIcon.setColorFilter(0xFF3498DB); // Blue
            } else if (lowerCondition.contains("cloud")) {
                holder.ivIcon.setImageResource(R.drawable.weather_cloudy);
                holder.ivIcon.clearColorFilter(); // Show original image
            } else if (lowerCondition.contains("clear") || lowerCondition.contains("sun")) {
                holder.ivIcon.setImageResource(R.drawable.weather_sunny);
                holder.ivIcon.clearColorFilter(); // Show original image
            } else {
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_help);
                holder.ivIcon.setColorFilter(0xFFBDC3C7);
            }
        }
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTemp, tvCondition;
        ImageView ivIcon;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
