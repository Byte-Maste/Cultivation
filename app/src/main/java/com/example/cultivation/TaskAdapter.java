package com.example.cultivation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cultivation.utils.Task;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTitle.setText(task.title);
        holder.tvDesc.setText(task.description);
        holder.tvPlotName.setText("For " + task.plotName);
        holder.tvDate.setText(task.date);

        if (task.isUrgent) {
            holder.ivUrgent.setImageResource(android.R.drawable.ic_dialog_alert); // Alert Icon
            holder.ivUrgent.setColorFilter(0xFFE74C3C); // Red tint
            holder.tvDate.setTextColor(0xFFE74C3C);
        } else {
            holder.ivUrgent.setImageResource(android.R.drawable.ic_menu_agenda); // Calendar/Check Icon
            holder.ivUrgent.setColorFilter(0xFF2ECC71); // Green tint
            holder.tvDate.setTextColor(0xFF95A5A6);
        }
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvPlotName, tvDate;
        ImageView ivUrgent;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPlotName = itemView.findViewById(R.id.tvPlotName);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivUrgent = itemView.findViewById(R.id.ivUrgent);
        }
    }
}
