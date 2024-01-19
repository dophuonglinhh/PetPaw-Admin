package com.example.petpawadmin.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.petpawadmin.R;
import com.example.petpawadmin.databinding.ActivityEditPostBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    private ActivityEditPostBinding binding;
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private StorageReference storageReference;
    private boolean isSelectNewImage = false;
    private DocumentReference postDocRef;
    private List<String> selectedPetListId = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");
        String communityId = intent.getStringExtra("communityId");
        currentUserId = intent.getStringExtra("authorId");

        if(postId != null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Fetching Post Detail....");
            progressDialog.show();

            binding.createPostSelectImageButton.setOnClickListener(v -> {
                selectImage();
                isSelectNewImage = true;
            });

            DocumentReference postRef;
            postRef = db.collection("Posts").document(postId);
            postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            postDocRef = postRef;

//                           ----------- Load image with the URL then the description -----------
                            String imageUrl = document.getString("imageURL");
                            if(imageUrl != null && !imageUrl.isEmpty()) {
                                binding.createPostImageView.setVisibility(View.VISIBLE);
                                Picasso.get()
                                        .load(imageUrl)
                                        .tag(System.currentTimeMillis())
                                        .into(binding.createPostImageView, new com.squareup.picasso.Callback() {
                                            @Override
                                            public void onSuccess() {
                                                binding.createPostImageView.setVisibility(View.VISIBLE);
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                                Log.d("TAG", "Load image successfully");
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Log.e("TAG", "Load image failed");
                                            }
                                        });
                            }else {
                                binding.createPostImageView.setVisibility(View.GONE);
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }

                            String displayDescription = combineContentAndTag(document.getString("content"), (List<String>) document.get("tags"));
                            binding.createPostDescriptionEditText.setText(displayDescription);

//                          ------------ Render Pet List ----------------
                            selectedPetListId = (List<String>) document.get("petIdList");
                            renderPetListView(true);

//                          ------------ Check valid input  -------------
                            binding.createPostSaveButton.setOnClickListener(v -> {
                                boolean isValid = true;

                                String tempDescription = binding.createPostDescriptionEditText.getText().toString();
                                List<String> tags = getTags(tempDescription);
                                String description = removeTag(tempDescription, tags);
                                Log.d("CreatePostActivity", "Description: " + description);

//                              -------- update if valid input --------

                                if(isValid) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("content", description);
                                    data.put("tags", tags);
                                    data.put("dateModified", new Date());
                                    data.put("modified", true);
                                    data.put("petIdList", selectedPetListId);
                                    data.put("communityId", communityId);

                                    postRef.update(data)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("CreatePostActivity", "DocumentSnapshot successfully updated!");
                                                if(isSelectNewImage) {
                                                    uploadImage(postId,true);
                                                } else {
                                                    finish();
                                                }
                                            })      .addOnFailureListener(e->{
                                                Log.e("CreatePostActivity", "Error updating document", e);
                                            });
                                }
                            });

                            storageReference = FirebaseStorage.getInstance().getReference("postImages/"+postId);

                            binding.createPostDeleteButton.setOnClickListener(v -> {
                                postRef.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                storageReference.delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("TAG", "Image deleted from storage!");
                                                            }
                                                        });

                                                Log.d("TAG", "Document deleted from Firestore!");
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Log.w("TAG", "Failed to delete image and/or document", exception);
                                            }
                                        });
                            });

                        } else {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Toast.makeText(EditPostActivity.this, "Post Detail not found", Toast.LENGTH_SHORT).show();
                            Log.d("TAG", "No document found");
                            finish();
                        }
                    } else {
                        Log.d("TAG", "Get failed with ", task.getException());
                    }
                }
            });

        }

    }
    private void renderPetListView(boolean isEditPost){
        Log.d("CreatePostActivity", "Render pet list view");
        CollectionReference petsRef = db.collection("Pets");
        //------- Fetch data ----------
        List<String> petListId = new ArrayList<>();
        List<String> petListName = new ArrayList<>();

        Query query = petsRef.whereEqualTo("ownerId", currentUserId);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("CreatePostActivity", "Fetch pet ID successfully");
                for (QueryDocumentSnapshot document : task.getResult()) {
                    petListId.add(document.getId());
                    petListName.add(document.getString("name"));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        petListName
                );

                binding.createPostTagsListView.setAdapter(adapter);
                Log.d("CreatePostActivity", "Pet list view adapter set");


                //------- Set color to the selected pet -----------
                Log.d("CreatePostActivity", "Selected pet ID: " + selectedPetListId);
                if(isEditPost){
                    binding.createPostTagsListView.post(() -> {
                        Log.d("CreatePostActivity", "Selected pet ID: " + selectedPetListId);
                        for(int i=0; i<selectedPetListId.size(); i++){
                            int index = petListId.indexOf(selectedPetListId.get(i));

                            View view = binding.createPostTagsListView.getChildAt(index);
                            if(view != null) {
                                view.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
                                view.setTag(true);
                            }
                        }
                    });
                }

                //--------- Set on click listener to the list view -----------
                binding.createPostTagsListView.setOnItemClickListener((parent, view, position, id) -> {

                    if(view.getTag() == null) {
                        view.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
                        view.setTag(true);
                        if(!selectedPetListId.contains(petListId.get(position))) {
                            selectedPetListId.add(petListId.get(position));
                        }

                    } else {
                        view.setBackgroundColor(Color.WHITE);
                        view.setTag(null);
                        selectedPetListId.remove(petListId.get(position));
                    }
                    Log.d("CreatePostActivity", "Selected pet ID: " + selectedPetListId.toString());
                });


            } else {
                Log.d("Get pet ID query", "Error getting documents: ", task.getException());
            }

        });
    }

    private void uploadImage(String postId, boolean isEditImage) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading to Database....");
        progressDialog.show();

        //Change the time to VN time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.TAIWAN);
        Date now = new Date();
        String fileName = formatter.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("postImages/"+postId);

        if(imageUri != null){
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            binding.createPostImageView.setImageURI(null);
                            binding.createPostImageView.setVisibility(View.GONE);
                            binding.createPostDescriptionEditText.setText("");
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Log.d("CreatePostActivity", "Image uploaded successfully");
                            // Get download URL after upload completes
                            taskSnapshot.getStorage().getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        Log.d("CreatePostActivity", "Image URL: " + uri);

                                        if(isEditImage){
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("imageURL", uri.toString());
                                            postDocRef.update(data)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("EditPostActivity", "Update image path successfully");

                                                        finish();
                                                    })      .addOnFailureListener(e->{
                                                        Log.e("EditPostActivity", "Error updating document", e);
                                                    });
                                            finish();
                                        }else{
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("imageURL", uri.toString());
                                            postDocRef.update(data)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("EditPostActivity", "DocumentSnapshot successfully updated!");
                                                        finish();
                                                    })      .addOnFailureListener(e->{
                                                        Log.e("EditPostActivity", "Error updating document", e);
                                                    });
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Log.e("EditPostActivity", "Error uploading image", e);
                        }
                    });
        }else {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            finish();
        }
    }

    private String combineContentAndTag(String description, List<String> tags) {
        for(int i=0; i<tags.size(); i++) {
            description += "#" + tags.get(i) + " ";
        }
        return description;
    }

    private List<String> getTags(String description) {
        List<String> tags = new ArrayList<>();
        if(!description.isEmpty()){
            String[] parts = description.split("#");
            for(int i=1; i<parts.length; i++) {
                String part = parts[i];
                if(!part.isEmpty() && part.charAt(0) != '#') {
                    tags.add(part);
                }
            }
            Log.d("CreatePostActivity", "Tags: " + tags.toString());
        }
        return tags;
    }

    private String removeTag(String description, List<String> tags) {
        for(int i=0; i<tags.size(); i++) {
            description = description.replace("#" + tags.get(i), "");
        }
        return description;
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

        if (requestCode == 100 && data != null && data.getData() != null){
            imageUri = data.getData();
            binding.createPostImageView.setImageURI(imageUri);
            binding.createPostImageView.setVisibility(View.VISIBLE);
        }
    }
}