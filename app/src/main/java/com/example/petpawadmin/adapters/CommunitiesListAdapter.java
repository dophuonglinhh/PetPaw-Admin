package com.example.petpawadmin.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petpawadmin.R;
import com.example.petpawadmin.activities.CommunityDetailsActivity;
import com.example.petpawadmin.activities.EmptyActivity;
import com.example.petpawadmin.models.Community;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommunitiesListAdapter extends RecyclerView.Adapter<CommunitiesListAdapter.CommunityViewHolder>{
    Context context;
    List<Community> communityList;

    boolean isSearch;
    private FragmentManager fragmentManager;



    public CommunitiesListAdapter(Context context, List<Community> communityList, Boolean isSearch, FragmentManager fragmentManager) {
        this.context = context;
        this.communityList = communityList;
        this.isSearch = isSearch;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommunityViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_community_card_view,
                        parent,
                        false
                ));
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        Log.d("TAG", "** user number: " + position);
        String communityId = communityList.get(position).getId();

        holder.communityCardViewName.setText(communityList.get(position).getName());
        Picasso.get()
                .load(communityList.get(position).getImageURL())
                .placeholder(R.drawable.default_avatar)
                .into(holder.communityCardViewPic);
        holder.communityCardViewId.setText(communityList.get(position).getId());
        /*

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        holder.deleteCommunityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Communities").document(communityId)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(context, "Community Deleted", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(context, EmptyActivity.class);
                                context.startActivity(intent);
                            }
                        });
            }
        });

         */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommunityDetailsActivity.class);
                intent.putExtra("communityId", communityId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }


    public class CommunityViewHolder extends RecyclerView.ViewHolder {
        ImageView communityCardViewPic;
        TextView communityCardViewName;
        TextView communityCardViewId;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            communityCardViewPic = itemView.findViewById(R.id.communityCardViewPic);
            communityCardViewName = itemView.findViewById(R.id.communityCardViewName);
            communityCardViewId = itemView.findViewById(R.id.communityCardViewId);

        }
    }
}
