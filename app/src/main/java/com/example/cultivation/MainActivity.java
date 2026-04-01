package com.example.cultivation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cultivation.miller.MillerHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tvTitle, tvSubtitle, tvNameLabel, tvConfirmPassLabel, tvForgotPassword, tvToggle;
    private TextView tvRoleLabel, tvFactoryLabel;
    private EditText etFullName, etMobile, etPassword, etConfirmPassword, etFactoryName;
    private Spinner spinnerRole;
    private Button btnAction;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Auto-login check
        if (mAuth.getCurrentUser() != null) {
            checkUserRoleAndRedirect(mAuth.getCurrentUser().getUid());
        } else {
            setContentView(R.layout.activity_main);
            initializeViews();
            setupListeners();
            updateUI();
        }
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvNameLabel = findViewById(R.id.tvNameLabel);
        etFullName = findViewById(R.id.etFullName);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        tvConfirmPassLabel = findViewById(R.id.tvConfirmPassLabel);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnAction = findViewById(R.id.btnAction);
        tvToggle = findViewById(R.id.tvToggle);

        tvRoleLabel = findViewById(R.id.tvRoleLabel);
        tvFactoryLabel = findViewById(R.id.tvFactoryLabel);
        spinnerRole = findViewById(R.id.spinnerRole);
        etFactoryName = findViewById(R.id.etFactoryName);
    }

    private void setupListeners() {
        tvToggle.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUI();
        });

        btnAction.setOnClickListener(v -> handleAuthAction());

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("Miller".equalsIgnoreCase(selected)) {
                    tvFactoryLabel.setVisibility(View.VISIBLE);
                    etFactoryName.setVisibility(View.VISIBLE);
                } else {
                    tvFactoryLabel.setVisibility(View.GONE);
                    etFactoryName.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateUI() {
        if (isLoginMode) {
            tvTitle.setText("SugarCane Pro");
            tvSubtitle.setText("Monitor, analyze, and optimize your sugarcane farming");
            tvNameLabel.setVisibility(View.GONE);
            etFullName.setVisibility(View.GONE);
            tvConfirmPassLabel.setVisibility(View.GONE);
            etConfirmPassword.setVisibility(View.GONE);

            // Hide Registration specific fields
            tvRoleLabel.setVisibility(View.GONE);
            spinnerRole.setVisibility(View.GONE);
            tvFactoryLabel.setVisibility(View.GONE);
            etFactoryName.setVisibility(View.GONE);

            tvForgotPassword.setVisibility(View.VISIBLE);
            btnAction.setText("Login");
            String text = "Don't have an account? <font color='#2ECC71'><b>Register</b></font>";
            tvToggle.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvTitle.setText("Create Account");
            tvSubtitle.setText("Join SugarCane Pro");
            tvNameLabel.setVisibility(View.VISIBLE);
            etFullName.setVisibility(View.VISIBLE);
            tvConfirmPassLabel.setVisibility(View.VISIBLE);
            etConfirmPassword.setVisibility(View.VISIBLE);

            // Show Role Spinner
            tvRoleLabel.setVisibility(View.VISIBLE);
            spinnerRole.setVisibility(View.VISIBLE);
            // Factory visibility depends on spinner, handled by listener.
            // Reset to current spinner state or default farmer
            if (spinnerRole.getSelectedItem() != null
                    && "Miller".equalsIgnoreCase(spinnerRole.getSelectedItem().toString())) {
                tvFactoryLabel.setVisibility(View.VISIBLE);
                etFactoryName.setVisibility(View.VISIBLE);
            } else {
                tvFactoryLabel.setVisibility(View.GONE);
                etFactoryName.setVisibility(View.GONE);
            }

            tvForgotPassword.setVisibility(View.GONE);
            btnAction.setText("Create Account");
            String text = "Already have an account? <font color='#2ECC71'><b>Login</b></font>";
            tvToggle.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        }
    }

    private void handleAuthAction() {
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(mobile) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String fakeEmail = mobile + "@sugarcane.app";

        if (isLoginMode) {
            loginUser(fakeEmail, password);
        } else {
            registerUser(fakeEmail, password);
        }
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                checkUserRoleAndRedirect(mAuth.getCurrentUser().getUid());
            } else {
                Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String email, String password) {
        String confirmPass = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        if (!password.equals(confirmPass)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        String role = spinnerRole.getSelectedItem().toString();
        String factoryName = "";

        if ("Miller".equalsIgnoreCase(role)) {
            factoryName = etFactoryName.getText().toString().trim();
            if (TextUtils.isEmpty(factoryName)) {
                etFactoryName.setError("Factory Name Required");
                return;
            }
        }

        String finalFactoryName = factoryName;
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();

                // Save User Data
                Map<String, Object> user = new HashMap<>();
                user.put("fullName", fullName);
                user.put("mobile", etMobile.getText().toString().trim());
                user.put("role", role.toLowerCase()); // "farmer" or "miller"
                if (!finalFactoryName.isEmpty()) {
                    user.put("factoryName", finalFactoryName);
                }

                db.collection("users").document(uid).set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            isLoginMode = true;
                            updateUI();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                        });

            } else {
                Toast.makeText(MainActivity.this, "Registration Failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRoleAndRedirect(String uid) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();
                String role = doc.getString("role");

                if ("miller".equalsIgnoreCase(role)) {
                    startActivity(new Intent(MainActivity.this, MillerHomeActivity.class));
                } else {
                    // Default to Farmer if role missing or "farmer"
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                }
                finish();
            } else {
                // If fetching fails, maybe offline? Default to HomeActivity for now or retry
                Toast.makeText(this, "Error fetching profile. Loading Home...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            }
        });
    }
}