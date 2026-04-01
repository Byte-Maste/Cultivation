package com.example.cultivation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnViewHistory = view.findViewById(R.id.btnViewHistory);
        android.widget.TextView tvName = view.findViewById(R.id.tvProfileName);
        android.widget.TextView tvMobile = view.findViewById(R.id.tvProfileMobile);

        // Check Role to hide History and Load Details
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(auth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            com.google.firebase.firestore.DocumentSnapshot doc = task.getResult();
                            String role = doc.getString("role");
                            String name = doc.getString("fullName");
                            String mobile = doc.getString("mobile");

                            // Fallback: Get mobile from email if missing in Firestore
                            if (mobile == null && auth.getCurrentUser().getEmail() != null) {
                                String email = auth.getCurrentUser().getEmail();
                                if (email.contains("@")) {
                                    mobile = email.split("@")[0];
                                }
                            }

                            if (name != null)
                                tvName.setText(name);
                            if (mobile != null)
                                tvMobile.setText(mobile);

                            if ("miller".equalsIgnoreCase(role)) {
                                btnViewHistory.setVisibility(View.GONE);
                            } else {
                                btnViewHistory.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }

        btnViewHistory.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnLogout.setOnClickListener(v -> {
            // 1. Delete Session
            FirebaseAuth.getInstance().signOut();

            // 2. Navigate back to Login Screen
            Intent intent = new Intent(getActivity(), MainActivity.class);
            // Clear back stack so they can't press "Back" to return to the dashboard
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}