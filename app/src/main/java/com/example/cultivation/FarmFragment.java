package com.example.cultivation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cultivation.data.Plot;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FarmFragment extends Fragment {

    private RecyclerView rvPlots;
    private FloatingActionButton fabAddPlot;
    private PlotAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String targetUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_farm, container, false);

        rvPlots = view.findViewById(R.id.rvPlots);
        fabAddPlot = view.findViewById(R.id.fabAddPlot);

        // Setup RecyclerView
        rvPlots.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlotAdapter();
        rvPlots.setAdapter(adapter);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Check for specific target (Read-Only Mode)
        if (getArguments() != null) {
            targetUid = getArguments().getString("TARGET_UID");
        }

        if (targetUid != null) {
            fabAddPlot.setVisibility(View.GONE); // Hide Add Button
        }

        // Load Data
        loadPlots();

        // FAB Click (Show Add Dialog)
        fabAddPlot.setOnClickListener(v -> showAddPlotDialog());

        return view;
    }

    private void loadPlots() {
        String uid = null;
        if (targetUid != null) {
            uid = targetUid;
        } else if (auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
        }

        if (uid == null) {
            Toast.makeText(getContext(), "User not identified", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid).collection("plots")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    List<Plot> plots = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            String name = doc.getString("name");
                            String area = doc.getString("area");
                            String variety = doc.getString("cropVariety");
                            String date = doc.getString("plantingDate");
                            // Default to random score if not present (or we save it)
                            Long scoreObj = doc.getLong("healthScore");
                            int score = scoreObj != null ? scoreObj.intValue() : 0;

                            // We need to use a constructor that sets these.
                            // The existing constructor sets a random score.
                            // We might want to fix that in Plot.java later, but for now:
                            Plot p = new Plot(name, area, variety, date);
                            // If we saved the score, override it.
                            if (scoreObj != null) {
                                p.healthScore = score;
                            }
                            plots.add(p);
                        }
                    }
                    adapter.setPlots(plots);
                });
    }

    private void showAddPlotDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Plot");

        // Layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Inputs
        final android.widget.EditText inputPlotNo = new android.widget.EditText(requireContext());
        inputPlotNo.setHint("Enter Plot Number");
        layout.addView(inputPlotNo);

        final android.widget.TextView tvVarietyLabel = new android.widget.TextView(requireContext());
        tvVarietyLabel.setText("Choose Sugarcane Variety:");
        tvVarietyLabel.setPadding(0, 30, 0, 10);
        layout.addView(tvVarietyLabel);

        final android.widget.Spinner spinnerVariety = new android.widget.Spinner(requireContext());
        String[] varieties = { "Co 0238", "Co 86032", "Co 80058", "CoM 0265", "VSI 434" };
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, varieties);
        spinnerVariety.setAdapter(adapter);
        layout.addView(spinnerVariety);

        // Area Input
        final android.widget.EditText inputArea = new android.widget.EditText(requireContext());
        inputArea.setHint("Area (e.g., 2 Acres)");
        layout.addView(inputArea);

        builder.setView(layout);

        // Buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String plotNo = inputPlotNo.getText().toString().trim();
            String variety = spinnerVariety.getSelectedItem().toString();
            String area = inputArea.getText().toString().trim();
            if (area.isEmpty())
                area = "N/A Acres";

            if (!plotNo.isEmpty()) {
                savePlot(plotNo, variety, area);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void savePlot(String name, String variety, String area) {
        if (auth.getCurrentUser() == null)
            return;
        String uid = auth.getCurrentUser().getUid();

        String date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        int randomScore = (int) (Math.random() * 20) + 80;

        java.util.Map<String, Object> plot = new java.util.HashMap<>();
        plot.put("name", name);
        plot.put("area", area);
        plot.put("cropVariety", variety);
        plot.put("plantingDate", date);
        plot.put("healthScore", randomScore);

        db.collection("users").document(uid).collection("plots")
                .add(plot)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Plot Saved to Cloud", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving plot", Toast.LENGTH_SHORT).show();
                });
    }
}
