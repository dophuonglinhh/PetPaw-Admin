package com.example.petpawadmin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.petpawadmin.R;
import com.example.petpawadmin.adapters.PostsListAdapter;
import com.example.petpawadmin.databinding.ActivityCommunityDetailsBinding;
import com.example.petpawadmin.databinding.ActivityUserDetailsBinding;
import com.example.petpawadmin.models.Community;
import com.example.petpawadmin.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommunityDetailsActivity extends AppCompatActivity {
    private ActivityCommunityDetailsBinding binding;
    private String communityId;
    private Community community;
    private FirebaseFirestore db;
    private List<Post> postList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        communityId = intent.getStringExtra("communityId");

        db = FirebaseFirestore.getInstance();

        displayCommunityInfo();
        getCommunityPosts();
        binding.deleteCommunityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Communities").document(communityId)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CommunityDetailsActivity.this, "Community Deleted", Toast.LENGTH_SHORT).show();
                                finish();

                            }
                        });
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayCommunityInfo() {
        db.collection("Communities")
                .document(communityId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            community = document.toObject(Community.class);
//                            ----------------- bind Name and Description -----------------
                            binding.communityDetailName.setText(community.getName());
                            binding.communityDetailDescription.setText(community.getDescription());
                            binding.communityId.setText(community.getId());

//                            ------------------- community Image --------------------
                            if (community.getImageURL() != null && !(community.getImageURL().isEmpty())) {
                                Picasso.get()
                                        .load(community.getImageURL())
                                        .tag(System.currentTimeMillis())
                                        .into(binding.communityImageURL, new com.squareup.picasso.Callback() {
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
                                binding.communityImageURL.setImageResource(R.drawable.default_avatar);
                            }
                        }
                    }
                });
        }

    private void getCommunityPosts() {
        CollectionReference postsRef = db.collection("Posts");
        Query query = postsRef.whereEqualTo("communityId", communityId).orderBy("communityId").orderBy("dateModified", Query.Direction.DESCENDING); // Order documents by dateModified in ascending order
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    postList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Post post = document.toObject(Post.class);
                        Post postTemp = new Post(post.getAuthorId(), post.getDateModified(), post.getContent(), post.isModified(), post.getImageURL(), post.getLikes(), post.getComments(), post.getPostId(), post.getTags(), post.getPetIdList(), post.getCommunityId());
                        postList.add(postTemp);
                    }

                    binding.communityDetailPostRecyclerView.setLayoutManager(new LinearLayoutManager(CommunityDetailsActivity.this));
                    binding.communityDetailPostRecyclerView.setAdapter(new PostsListAdapter(CommunityDetailsActivity.this, postList));

                }else{
                    Log.d("TAG", "ERROR: " + task.getException());
                }
            }
        });
    }

}