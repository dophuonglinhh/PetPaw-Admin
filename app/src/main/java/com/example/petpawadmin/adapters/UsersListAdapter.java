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
import com.example.petpawadmin.activities.UserDetailsActivity;
import com.example.petpawadmin.models.User;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.UserViewHolder> {

    private Context context;
    private List<User> usersList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("users");


    public UsersListAdapter(Context context, List<User> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_user_card_view,
                        parent,
                        false
                ));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Log.d("TAG", "** user number: " + position);

        String userId = usersList.get(position).getUid();
        holder.userCardViewUsername.setText(usersList.get(position).getName());

        Log.d("TAG", "- user id: " + userId);
        Log.d("TAG", "-- user name: " + usersList.get(position).getName());

        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userImageUrl = (String) task.getResult().get("imageURL");
                Log.d("TAG", "--- user image url: " + userImageUrl);
                if (userImageUrl == null || userImageUrl.equals("")) {
                    Log.d("User List Adapter", "image null ");
                    holder.userCardViewProfilePic.setImageResource(R.drawable.default_avatar);
                } else {
                    Picasso.get()
                            .load(userImageUrl)
                            .tag(usersList.get(position).getEmail())
                            .placeholder(R.drawable.default_avatar)
                            .into(holder.userCardViewProfilePic);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    String selectedUserId = usersList.get(currentPosition).getUid();
                    Intent intent = new Intent(context, UserDetailsActivity.class);
                    intent.putExtra("userId", selectedUserId);
                    context.startActivity(intent);
                }
            }
        });

    }

        @Override
        public int getItemCount() {
            return usersList.size();
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {
        LinearLayout userCardViewLinearLayout;
        ImageView userCardViewProfilePic;
        TextView userCardViewUsername;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userCardViewProfilePic = itemView.findViewById(R.id.userCardViewProfilePic);
            userCardViewUsername = itemView.findViewById(R.id.userCardViewUsername);
            userCardViewLinearLayout = itemView.findViewById(R.id.userCardViewLinearLayout);
        }
    }
}



