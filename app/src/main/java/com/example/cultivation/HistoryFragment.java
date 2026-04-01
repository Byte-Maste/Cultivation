package com.example.cultivation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cultivation.data.DatabaseClient;
import com.example.cultivation.data.ScanLog;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter();
        rvHistory.setAdapter(adapter);

        loadHistory();

        return view;
    }

    private void loadHistory() {
        executor.execute(() -> {
            List<ScanLog> logs = DatabaseClient.getInstance(requireContext())
                    .getAppDatabase()
                    .scanLogDao()
                    .getAll();
            requireActivity().runOnUiThread(() -> adapter.setLogs(logs));
        });
    }
}
