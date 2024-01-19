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
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.UserViewHolder> {

    Context context;
    List<User> usersList;


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

        Log.d("TAG", "- user id: " + userId);
        holder.userCardViewUsername.setText(usersList.get(position).getName());
        Log.d("TAG", "-- user name: " + usersList.get(position).getName());
        Picasso.get()
                    .load(usersList.get(position).getImageURL())
                    .placeholder(R.drawable.default_avatar)
                    .into(holder.userCardViewProfilePic);

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



