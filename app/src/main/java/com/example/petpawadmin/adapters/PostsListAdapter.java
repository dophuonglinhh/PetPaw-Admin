package com.example.petpawadmin.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petpawadmin.R;
import com.example.petpawadmin.activities.EditPostActivity;
import com.example.petpawadmin.models.Post;
import com.example.petpawadmin.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostsListAdapter extends RecyclerView.Adapter<PostsListAdapter.PostViewHolder> {

    Context context;
    List<Post> postList;

    public PostsListAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_post_card_view,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("TAG", "** post number: " + position);

        String postId = postList.get(position).getPostId();
        Log.d("TAG", "- postId: " + postId);
        List<String> likes = postList.get(position).getLikes();

//        -------------- String CommnunityId -------------
        String communityId = postList.get(position).getCommunityId();
        Log.d("TAG", "- communityId: " + communityId);

//        ------------ isModified -------------------
        if (postList.get(position).isModified()) {
            holder.postCardViewIsModified.setVisibility(View.VISIBLE);
        } else {
            holder.postCardViewIsModified.setVisibility(View.GONE);
        }

        String authorId = postList.get(position).getAuthorId();
        holder.postCardViewEditImageView.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditPostActivity.class);
            intent.putExtra("postId", postId);
            intent.putExtra("communityId", communityId);
            intent.putExtra("authorId", authorId);
            context.startActivity(intent);
        });
//        -------------- Show post description -------------
        holder.postCardViewContentTextView.setText(postList.get(position).getContent());
//        -------------- Show post description -------------
        holder.postId.setText(postList.get(position).getPostId());

//        -------------- Format Date -------------
        Date date = postList.get(position).getDateModified();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        holder.postCardViewDate.setText(sdf.format(date));

//        -------------- Show tags -------------
        if (postList.get(position).getTags().size() == 0) {
            holder.postCardViewTagsTextView.setVisibility(View.GONE);
        } else {
            holder.postCardViewTagsTextView.setVisibility(View.VISIBLE);
            StringBuilder tagList = new StringBuilder();
            for (String tag : postList.get(position).getTags()) {
                tagList.append("#").append(tag).append(" ");
            }
            holder.postCardViewTagsTextView.setText(tagList.toString());
        }

//        -------------- Show pet name -------------
        StringBuilder petNameList = new StringBuilder();
        if(postList.get(position).getPetIdList().size() == 0){
            holder.postCardViewPetNameLinearLayout.setVisibility(View.GONE);
        }else{
            for (String petId : postList.get(position).getPetIdList()) {
                db.collection("Pets").document(petId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            String petName = documentSnapshot.getString("name");
                            if(petNameList.length() == 0) {
                                petNameList.append(petName + " ");
                            }else{
                                petNameList.append(", " + petName + " ");
                            }
                            holder.postCardViewPetNameTextView.setText(petNameList.toString());
                        }
                    } else {
                        Log.e("PostListAdapter", "Error fetching pet data", task.getException());
                    }
                });
            }
        }

//        ---------------  Load username and avatar start --------------
        Post post = postList.get(position);
        // Fetch the user's name and avatar URL from Firestore
        db.collection("users").document(post.getAuthorId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        // Set user's name
                        holder.postCardViewUserNameTextView.setText(user.getName());

                        // Set user's avatar
                        if (user.getImageURL() != null && !user.getImageURL().isEmpty()) {
                            Picasso.get()
                                    .load(user.getImageURL())
                                    .placeholder(R.drawable.default_avatar) // A default placeholder if needed
                                    .into(holder.postCardViewProfilePic);
                        } else {
                            holder.postCardViewProfilePic.setImageResource(R.drawable.default_avatar);
                        }
                    }
                }
            } else {
                Log.e("PostListAdapter", "Error fetching user data", task.getException());
            }
        });

//        ---------------  Load image start -----------
        String imageUrl = postList.get(position).getImageURL();
        if(imageUrl == null || imageUrl.isEmpty()) {
            holder.postCardImageView.setVisibility(View.GONE);
        } else {
            Picasso.get()
                    .load(imageUrl)
                    .tag(System.currentTimeMillis())
                    .into(holder.postCardImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            //Log.d("TAG", "Load image successfully at " + System.currentTimeMillis());
                        }
                        @Override
                        public void onError(Exception e) {
                            Log.d("TAG", "Load image successfully");
                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
    public class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView postCardViewProfilePic, postCardImageView, postCardViewLikeImageView, postCardViewCommentImageView, postCardViewEditImageView;
        TextView postCardViewIsModified, postCardViewUserNameTextView, postCardViewDate, postCardViewContentTextView, postCardViewLikeCountTextView, postCardViewCommentCountTextView, postCardViewTagsTextView, postCardViewPetNameTextView;
        LinearLayout postCardViewPetNameLinearLayout;
        TextView postId;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
//            --------TextView----------
            postCardViewUserNameTextView = itemView.findViewById(R.id.postCardViewUserNameTextView);
            postCardViewDate = itemView.findViewById(R.id.postCardViewDate);
            postCardViewContentTextView = itemView.findViewById(R.id.postCardViewContentTextView);
            postCardViewTagsTextView = itemView.findViewById(R.id.postCardViewTagsTextView);
            postCardViewPetNameTextView = itemView.findViewById(R.id.postCardViewPetNameTextView);
            postCardViewIsModified = itemView.findViewById(R.id.postCardViewIsModified);
            postId = itemView.findViewById(R.id.postCardViewPostID);

//          --------ImageView----------
            postCardImageView = itemView.findViewById(R.id.postCardImageView);
            postCardViewProfilePic = itemView.findViewById(R.id.postCardViewProfilePic);
            postCardViewEditImageView = itemView.findViewById(R.id.postCardViewEditImageView);

//            --------LinearLayout----------
            postCardViewPetNameLinearLayout = itemView.findViewById(R.id.postCardViewPetNameLinearLayout);

            itemView.setOnClickListener(v -> {
                Log.d("PostListAdapter", "onClick: " + getAdapterPosition());
            });
        }
    }
}
