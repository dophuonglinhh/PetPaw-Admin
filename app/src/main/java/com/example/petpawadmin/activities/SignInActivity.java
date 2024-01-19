package com.example.petpawadmin.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.petpawadmin.databinding.ActivitySignInBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // admin credentials
//        String adminEmail = "admin@gmail.com";
//        String adminPassword = "admin123";

        binding.btnLogin.setOnClickListener(v -> {
            String txt_email = binding.email.getText().toString();
            String txt_password = binding.password.getText().toString();


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Admin").document("nolD8tefH4w9mwE9efzM").get().addOnSuccessListener(documentSnapshot -> {
                String adminEmail = documentSnapshot.getString("email");
                String adminPassword = documentSnapshot.getString("password");


            if (txt_email.isEmpty() || txt_password.isEmpty()) {
                Toast.makeText(SignInActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (txt_email.equals(adminEmail) && txt_password.equals(adminPassword)) {
                Toast.makeText(SignInActivity.this, "Admin Login successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignInActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }

            });

        });
    }
}