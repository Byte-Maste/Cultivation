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

import com.example.cultivation.FarmFragment;
import com.example.cultivation.R;
import com.example.cultivation.miller.FarmerItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsFragment extends Fragment {

    private RecyclerView rvConnections;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ConnectionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Reusing fragment_find_farmers layout as it contains just a RecyclerView
        View view = inflater.inflate(R.layout.fragment_find_farmers, container, false);

        rvConnections = view.findViewById(R.id.rvFarmers);
        rvConnections.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadConnections();

        return view;
    }

    private void loadConnections() {
        if (auth.getCurrentUser() == null)
            return;
        String myUid = auth.getCurrentUser().getUid();

        db.collection("connection_requests")
                .whereEqualTo("fromUid", myUid)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        List<FarmerItem> farmers = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String name = doc.getString("toName");
                            String uid = doc.getString("toUid");

                            if (uid != null) {
                                farmers.add(new FarmerItem(uid, name != null ? name : "Unknown", "Connected"));
                            }
                        }

                        if (farmers.isEmpty()) {
                            if (getContext() != null)
                                Toast.makeText(getContext(), "No connections yet", Toast.LENGTH_SHORT).show();
                        }

                        adapter = new ConnectionAdapter(farmers, this::openFarm);
                        if (rvConnections != null) {
                            rvConnections.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(
                        e -> {
                            if (getContext() != null)
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void openFarm(FarmerItem farmer) {
        FarmFragment fragment = new FarmFragment();
        Bundle args = new Bundle();
        args.putString("TARGET_UID", farmer.uid);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
