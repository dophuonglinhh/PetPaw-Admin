package com.example.petpawadmin.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.petpawadmin.R;
import com.example.petpawadmin.databinding.ActivityEditUserBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class EditUserActivity extends AppCompatActivity {
    private ActivityEditUserBinding binding;
    private Uri avatarImageUri;
    private ProgressDialog progressDialog;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.name.setVisibility(View.VISIBLE);
        binding.address.setVisibility(View.VISIBLE);
        binding.editNameText.setVisibility(View.INVISIBLE);
        binding.editAddressText.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        uid = intent.getStringExtra("userId");
        String avatarURL = intent.getStringExtra("avatarURL");
        String userName = intent.getStringExtra("name");
        String userAddress = intent.getStringExtra("address");

        if (avatarURL != null) {
            Picasso.get().load(avatarURL).into(binding.avatar);
        } else {
            binding.avatar.setImageResource(R.drawable.default_avatar);
        }
        binding.name.setText(userName);
        binding.editNameText.setText(userName);
        binding.address.setText(userAddress);
        binding.editAddressText.setText(userAddress);

        binding.editAvatarBtn.setOnClickListener(v -> {
            selectImage();
        });
        binding.editInformationBtn.setOnClickListener(v -> {
            binding.name.setVisibility(View.INVISIBLE);
            binding.address.setVisibility(View.INVISIBLE);
            binding.editNameText.setVisibility(View.VISIBLE);
            binding.editAddressText.setVisibility(View.VISIBLE);
        });

        binding.saveBtn.setOnClickListener(v-> {
            saveUserInfo();
        });

        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
    }

    private void saveUserInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (uid != null) {
            String updatedName = binding.editNameText.getText().toString();
            String updatedAddress = binding.editAddressText.getText().toString();

            if (updatedName.equals(binding.name.getText().toString()) && updatedAddress.equals(binding.address.getText().toString()) && (avatarImageUri == null)){
                finish();
            }else{
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Updating Profile...");
                progressDialog.show();

                // Update Firestore
                db.collection("users").document(uid).update("name", updatedName, "address", updatedAddress)
                        .addOnSuccessListener(aVoida->{
                            // Update Storage if avatar is changed
                            if (avatarImageUri != null) {
                                StorageReference avatarRef = FirebaseStorage.getInstance().getReference("avatarImages/" + uid);
                                avatarRef.putFile(avatarImageUri)
                                        .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            String avatarUrl = uri.toString();
                                            db.collection("users").document(uid).update("imageURL", avatarUrl)
                                                    .addOnSuccessListener(aVoid -> {
                                                        //setResult(Activity.RESULT_OK);
                                                        if (progressDialog.isShowing()) {
                                                            progressDialog.dismiss();
                                                        }
                                                        finish();
                                                    });
                                        }));
                            } else{
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                finish();
                            }
                        });
            }

        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            avatarImageUri = data.getData();
            binding.avatar.setImageURI(avatarImageUri);
        }
    }
}