package com.example.petpawadmin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.petpawadmin.R;
import com.example.petpawadmin.databinding.ActivityUserDetailsBinding;
import com.example.petpawadmin.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDetailsActivity extends AppCompatActivity {

    private String userId;
    private User user;
    private ActivityUserDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        displayUserInfo();

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserDetailsActivity.this, EditUserActivity.class);
                i.putExtra("userId", userId);
                i.putExtra("avatarURL", user.getImageURL());
                i.putExtra("name", user.getName());
                i.putExtra("address", user.getAddress());
                startActivity(i);
            }
        });


        ArrayList<String> reasonToBan = new ArrayList<String>(Arrays.asList(
                "Spreading misinformation",
                "Bullying or harassment",
                "Impersonation",
                "Posting illegal content",
                "Animal abuse or cruelty",
                "Posting harmful misinformation about pet care"
        ));
        List<String> selectedReason = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                reasonToBan
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.banReasonSpinner.setAdapter(adapter);

        binding.banReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(selectedReason.size() > 0){
                    selectedReason.remove(0);
                }
                selectedReason.add(reasonToBan.get(position));
                Log.d("CreatePostActivity", "Selected pet ID: " + selectedReason.get(0).toString());
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.banBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedReason.isEmpty()) {
                    String banReason = selectedReason.get(0);
                    saveUserToDb(banReason);

                    binding.banBtn.setVisibility(View.GONE);
                    binding.unbanBtn.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(v.getContext(), "Please select a ban reason.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        binding.unbanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserToDb(null);
                binding.unbanBtn.setVisibility(View.GONE);
                binding.banBtn.setVisibility(View.VISIBLE);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //displayUserInfo();
    }

    private void displayUserInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersCollection = db.collection("users"); // Get reference to users collection
        Query query = usersCollection.whereEqualTo("uid", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("UserDetailsActivity", "task.getResult().size() = " + task.getResult().size());

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        user = documentSnapshot.toObject(User.class);
                        Log.d("UserDetailsActivity", "image URL: " + user.getImageURL());
                        if (user.getImageURL() != null && !(user.getImageURL().isEmpty())) {
                            Picasso.get()
                                    .load(user.getImageURL())
                                    .tag(System.currentTimeMillis())
                                    .into(binding.avatar, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d("TAG", "Load avatar successfully at " + System.currentTimeMillis());
                                        }
                                        @Override
                                        public void onError(Exception e) {
                                            Log.e("TAG", "Load image failed");
                                        }
                                    });
                        } else {
                            binding.avatar.setImageResource(R.drawable.default_avatar);
                        }
                        binding.userid.setText(user.getUid());
                        binding.name.setText(user.getName());
                        if (user.getEmail() != null) {
                            binding.email.setText(user.getEmail());
                        } else {
                            binding.email.setText(" ");
                        }
                        if (user.getPhone() != null) {
                            binding.phoneNum.setText(user.getPhone());
                        } else {
                            binding.phoneNum.setText(" ");
                        }
                        binding.address.setText(user.getAddress());
                        if (user.getBanReason() == null || user.getBanReason().isEmpty()) {
                            binding.banBtn.setVisibility(View.VISIBLE);
                            binding.unbanBtn.setVisibility(View.GONE);
                        } else {
                            binding.banBtn.setVisibility(View.GONE);
                            binding.unbanBtn.setVisibility(View.VISIBLE);
                        }
                        break;
                    }

                } else {
                    Log.e("UserDetailsActivity", "Error getting user data: ", task.getException());
                }
            }
        });
    }

    private void saveUserToDb(String banReason) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (userId != null) {
            db.collection("users").document(userId).update("banReason", banReason)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("UpdateUser", "User ban reason updated successfully.");
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Log.d("UpdateUser", "Error updating user ban reason", e);
                    });
        }
    }

}