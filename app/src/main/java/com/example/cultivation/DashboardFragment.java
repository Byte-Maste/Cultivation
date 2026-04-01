package com.example.cultivation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.cultivation.data.WeatherResponse;
import com.example.cultivation.data.WeatherService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardFragment extends Fragment {

    private LineChart chartGrowth;
    private FusedLocationProviderClient fusedLocationClient;
    private TextToSpeech tts;
    private static final String API_KEY = "fcbd96fe998afee2062666ca800d4675"; // Replace with real key
    private ListenerRegistration firestoreListener;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        setupChart(view);

        return view;
    }

    private void setupChart(View view) {
        chartGrowth = view.findViewById(R.id.chartGrowth);

        // Customize Chart
        Description description = new Description();
        description.setText("Brix Data Stream");
        chartGrowth.setDescription(description);

        XAxis xAxis = chartGrowth.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        chartGrowth.getAxisRight().setEnabled(false);
        chartGrowth.getAxisLeft().setDrawGridLines(true);

        setupFirestoreListener();

        setupWeather(view);
    }

    private void setupFirestoreListener() {
        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "CkglRu6XjVfdT1fgk4mpf91NsWA3"; // Fallback to provided ID for testing

        // Path: users/{uid}/plots/61/sensor_logs
        String path = "users/" + uid + "/plots/61/sensor_logs";

        firestoreListener = db.collection(path)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("DashboardFragment", "Listen failed.", error);
                            return;
                        }

                        List<Entry> entries = new ArrayList<>();
                        int index = 0;
                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                if (doc.contains("value")) {
                                    Double val = doc.getDouble("value");
                                    if (val != null) {
                                        entries.add(new Entry(index++, val.floatValue()));
                                    }
                                }
                            }
                        }
                        updateChart(entries);
                    }
                });
    }

    private void updateChart(List<Entry> entries) {
        if (entries.isEmpty())
            return;

        LineDataSet dataSet = new LineDataSet(entries, "Brix Value (%)");
        dataSet.setColor(Color.parseColor("#2ECC71"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#2ECC71"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chartGrowth.setData(lineData);
        chartGrowth.notifyDataSetChanged();
        chartGrowth.invalidate();
    }

    private void setupWeather(View view) {
        TextView tvTemp = view.findViewById(R.id.tvTemp);
        TextView tvCondition = view.findViewById(R.id.tvCondition);

        // Initialize TTS
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // Initialize Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                fetchWeatherData(location.getLatitude(), location.getLongitude(), tvTemp, tvCondition);
            } else {
                tvCondition.setText("Locating...");
            }
        });

        // Voice Button
        View weatherCard = view.findViewById(R.id.cvWeather); // Ensure this ID exists or use generic
        if (weatherCard != null) {
            weatherCard.setOnClickListener(v -> {
                String text = "Today's weather is " + tvCondition.getText() + ", temperature " + tvTemp.getText();
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            });
        }
    }

    private void fetchWeatherData(double lat, double lon, TextView tvTemp, TextView tvCondition) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeather(lat, lon, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    float temp = response.body().main.temp;
                    String condition = response.body().weather.get(0).main;

                    tvTemp.setText(Math.round(temp) + "°C");
                    tvCondition.setText(condition);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvCondition.setText("Weather Error");
            }
        });
    }

    @Override
    public void onDestroy() {
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
