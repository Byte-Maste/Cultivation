package com.example.cultivation.miller;

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
import com.example.cultivation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindFarmersFragment extends Fragment {

    private RecyclerView rvFarmers;
    private FarmerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_farmers, container, false);

        rvFarmers = view.findViewById(R.id.rvFarmers);
        rvFarmers.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadFarmers();

        return view;
    }

    private void loadFarmers() {
        if (auth.getCurrentUser() == null)
            return;
        String myUid = auth.getCurrentUser().getUid();

        // 1. Get existing connections (pending or accepted) to exclude them
        db.collection("connection_requests")
                .whereEqualTo("fromUid", myUid)
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> connectedUids = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        String status = doc.getString("status");
                        if ("accepted".equals(status) || "pending".equals(status)) {
                            connectedUids.add(doc.getString("toUid"));
                        }
                    }
                    fetchFarmersList(connectedUids); // 2. Fetch Farmers
                })
                .addOnFailureListener(e -> fetchFarmersList(new ArrayList<>())); // Fallback
    }

    private void fetchFarmersList(List<String> excludedUids) {
        db.collection("users")
                .whereEqualTo("role", "farmer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FarmerItem> farmers = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("fullName");
                        String uid = doc.getId();

                        // Skip if connected or pending
                        if (excludedUids.contains(uid))
                            continue;

                        String location = "Nearby"; // Dummy
                        farmers.add(new FarmerItem(uid, name, location));
                    }

                    if (farmers.isEmpty()) {
                        // Only add dummy if exclude list is empty (fresh user)
                        if (excludedUids.isEmpty()) {
                            farmers.add(new FarmerItem("dummy1", "Suresh Patel", "Muzaffarnagar (3km)"));
                        } else {
                            Toast.makeText(getContext(), "No new farmers found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    adapter = new FarmerAdapter(farmers, this::sendConnectionRequest);
                    rvFarmers.setAdapter(adapter);
                })
                .addOnFailureListener(
                        e -> Toast.makeText(getContext(), "Error loading farmers", Toast.LENGTH_SHORT).show());
    }

    private void sendConnectionRequest(FarmerItem farmer) {
        if (auth.getCurrentUser() == null)
            return;

        String myUid = auth.getCurrentUser().getUid();

        Map<String, Object> req = new HashMap<>();
        req.put("fromUid", myUid);
        req.put("toUid", farmer.uid);
        req.put("status", "pending");
        req.put("timestamp", System.currentTimeMillis());
        // Also saving names for easier display
        req.put("fromName", "My Factory"); // Should fetch actual name
        req.put("toName", farmer.name);

        db.collection("connection_requests")
                .add(req)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Request Sent to " + farmer.name, Toast.LENGTH_SHORT).show();
                    farmer.isConnected = true; // Updates UI (Need to notify adapter)
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(
                        e -> Toast.makeText(getContext(), "Failed to send request", Toast.LENGTH_SHORT).show());
    }
}
