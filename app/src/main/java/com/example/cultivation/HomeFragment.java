package com.example.cultivation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cultivation.data.ForecastResponse;
import com.example.cultivation.data.MarketRate;
import com.example.cultivation.data.Plot;
import com.example.cultivation.data.WeatherService;
import com.example.cultivation.utils.CropCalendar;
import com.example.cultivation.utils.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private static final String API_KEY = "fcbd96fe998afee2062666ca800d4675";

    private RecyclerView rvForecast, rvMarketRates, rvTasks;
    private CardView cardWeatherAlert;
    private TextView tvAlertHeader, tvAlertBody;
    private android.widget.ImageView ivInbox;

    private MarketAdapter marketAdapter;
    private TaskAdapter taskAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String targetUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Firebase Init
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Check for Read-Only Mode
        if (getArguments() != null) {
            targetUid = getArguments().getString("TARGET_UID");
        }

        // ... (rest of onCreateView)

        // Forecast RV
        rvForecast = view.findViewById(R.id.rvForecast);
        rvForecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Market Rates RV
        rvMarketRates = view.findViewById(R.id.rvMarketRates);
        rvMarketRates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Tasks RV
        rvTasks = view.findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext())); // Vertical
        taskAdapter = new TaskAdapter(new ArrayList<>());
        rvTasks.setAdapter(taskAdapter);

        // Weather Alerts
        cardWeatherAlert = view.findViewById(R.id.cardWeatherAlert);
        tvAlertHeader = view.findViewById(R.id.tvAlertHeader);
        tvAlertBody = view.findViewById(R.id.tvAlertBody);

        ivInbox = view.findViewById(R.id.ivInbox);

        if (targetUid != null) {
            ivInbox.setVisibility(View.GONE);
            TextView tvWelcome = view.findViewById(R.id.tvWelcome);
            if (tvWelcome != null)
                tvWelcome.setText("Farmer Dashboard\n(Read-Only)");
        } else {
            ivInbox.setOnClickListener(v -> showInbox());
        }

        setupWeather();
        loadMarketData();

        return view;
    }

    private void showInbox() {
        if (auth.getCurrentUser() == null)
            return;

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.fragment_inbox, null);
        RecyclerView rvRequests = sheetView.findViewById(R.id.rvRequests);
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        String myUid = auth.getCurrentUser().getUid();

        db.collection("connection_requests")
                .whereEqualTo("toUid", myUid)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ConnectionRequest> reqs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ConnectionRequest req = doc.toObject(ConnectionRequest.class);
                        req.id = doc.getId();
                        reqs.add(req);
                    }

                    if (reqs.isEmpty()) {
                        android.widget.Toast
                                .makeText(getContext(), "No pending requests", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }

                    RequestAdapter adapter = new RequestAdapter(reqs, req -> {
                        acceptRequest(req);
                        dialog.dismiss();
                    });
                    rvRequests.setAdapter(adapter);
                });

        dialog.setContentView(sheetView);
        dialog.show();
    }

    private void acceptRequest(ConnectionRequest req) {
        if (req.id == null)
            return;
        db.collection("connection_requests").document(req.id)
                .update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    android.widget.Toast.makeText(getContext(), "Request Accepted!", android.widget.Toast.LENGTH_SHORT)
                            .show();
                });
    }

    private void setupWeather() {
        // Use hardcoded location for demo to ensure it works without waiting for GPS in
        // emulator
        // Muzaffarnagar: 29.47, 77.70
        fetchForecast(29.47, 77.70);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadMarketData() {
        db.collection("market_rates").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MarketRate> rates = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            rates.add(doc.toObject(MarketRate.class));
                        }
                    } else {
                        // Demo Data
                        rates.add(new MarketRate("Muzaffarnagar", "₹345 / qt", "up"));
                        rates.add(new MarketRate("Shamli Mandi", "₹340 / qt", "stable"));
                        rates.add(new MarketRate("Meerut", "₹342 / qt", "down"));
                    }
                    marketAdapter = new MarketAdapter(rates);
                    rvMarketRates.setAdapter(marketAdapter);
                });
    }

    private void loadStats() {
        String uid = null;
        if (targetUid != null) {
            uid = targetUid;
        } else if (auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
        }

        if (uid == null) {
            updateStatsUI(0, null);
            return;
        }

        db.collection("users").document(uid).collection("plots")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    Plot bestPlot = null;
                    int maxScore = -1;
                    List<Plot> allPlots = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String area = doc.getString("area");
                        String variety = doc.getString("cropVariety");
                        String date = doc.getString("plantingDate");
                        Long scoreObj = doc.getLong("healthScore");
                        int score = scoreObj != null ? scoreObj.intValue() : 0;

                        Plot p = new Plot(name, area, variety, date);
                        p.healthScore = score;
                        allPlots.add(p);

                        if (score > maxScore) {
                            maxScore = score;
                            bestPlot = p;
                        }
                    }

                    updateStatsUI(count, bestPlot);
                    generateSmartTasks(allPlots);
                });
    }

    private void generateSmartTasks(List<Plot> plots) {
        List<Task> allTasks = new ArrayList<>();
        for (Plot p : plots) {
            allTasks.addAll(CropCalendar.getTasksForPlot(p));
        }

        if (allTasks.isEmpty()) {
            allTasks.add(new Task("Welcome!", "Add a plot to see smart tasks.", "System", "Today", false));
        }

        taskAdapter.setTasks(allTasks);
    }

    private void updateStatsUI(int count, Plot bestPlot) {
        View view = getView();
        if (view != null) {
            TextView tvTotal = view.findViewById(R.id.tvTotalPlots);
            TextView tvBest = view.findViewById(R.id.tvBestPlotName);

            if (tvTotal != null)
                tvTotal.setText(String.valueOf(count));
            if (tvBest != null) {
                if (bestPlot != null) {
                    String name = bestPlot.name;
                    if (name != null) {
                        name = name.toLowerCase().startsWith("plot") ? name : "Plot # " + name;
                    } else {
                        name = "Plot";
                    }
                    tvBest.setText(name + " (" + bestPlot.healthScore + "%)");
                } else {
                    tvBest.setText("No Plots");
                }
            }
        }
    }

    private void fetchForecast(double lat, double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<ForecastResponse> call = service.getForecast(lat, lon, API_KEY, "metric");

        call.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processForecast(response.body().list);
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                // Handle failure (optional)
            }
        });
    }

    private void processForecast(List<ForecastResponse.ForecastItem> fullList) {
        List<ForecastResponse.ForecastItem> dailyList = new ArrayList<>();
        boolean isRainPredicted = false;
        boolean isCyclonePredicted = false;

        // API returns data every 3 hours (5 days = 40 items).
        // We want one item per day (e.g., 12:00 PM) for the RecyclerView.
        // Also check ALL items for Alerts.

        String lastDate = "";
        for (ForecastResponse.ForecastItem item : fullList) {
            // Check Alert Conditions (Rain: 2xx, 3xx, 5xx. Cyclone: 781 or high wind if
            // available)
            if (item.weather != null && !item.weather.isEmpty()) {
                int id = item.weather.get(0).id;
                if (id >= 200 && id <= 531) { // Thunderstorm, Drizzle, Rain
                    isRainPredicted = true;
                }
            }

            // Filtering for UI (Approx 1 per day)
            // dt_txt format: "2023-10-25 15:00:00"
            String date = item.dt_txt.split(" ")[0];
            if (!date.equals(lastDate)) {
                // Ideally pick the one closest to noon, but first found for the day is okay for
                // now
                if (item.dt_txt.contains("12:00")) {
                    dailyList.add(item);
                    lastDate = date;
                }
            }
        }

        // If we didn't find "12:00" for some days (e.g. today started after 12:00),
        // fallback logic could be added, but simple unique date check is decent.
        // Let's refine: just take the first entry of each distinct day if list is
        // small?
        // Actually, the loop above only adds if 12:00. If we reset lastDate logic:
        if (dailyList.isEmpty()) { // Fallback if 12:00 not found (rare but possible)
            // Simple fallback: take every 8th item (24h / 3h)
            for (int i = 0; i < fullList.size(); i += 8) {
                dailyList.add(fullList.get(i));
            }
        }

        ForecastAdapter adapter = new ForecastAdapter(dailyList);
        rvForecast.setAdapter(adapter);

        // Show Alert
        if (isRainPredicted) {
            tvAlertHeader.setVisibility(View.VISIBLE);
            cardWeatherAlert.setVisibility(View.VISIBLE);
            tvAlertBody.setText("Rain is predicted this week! Ensure proper drainage.");
        } else {
            tvAlertHeader.setVisibility(View.GONE);
            cardWeatherAlert.setVisibility(View.GONE);
        }
    }

}