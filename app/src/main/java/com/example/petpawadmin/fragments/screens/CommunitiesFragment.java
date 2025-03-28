package com.example.petpawadmin.fragments.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.petpawadmin.R;
import com.example.petpawadmin.adapters.CommunitiesListAdapter;
import com.example.petpawadmin.databinding.FragmentCommunitiesBinding;
import com.example.petpawadmin.databinding.FragmentPostsBinding;
import com.example.petpawadmin.models.Community;
import com.example.petpawadmin.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommunitiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommunitiesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private List<Community> communityList = new ArrayList<>();
    private Context context;
    private FragmentCommunitiesBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public CommunitiesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommunitiesFragment newInstance(String param1, String param2) {
        CommunitiesFragment fragment = new CommunitiesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommunitiesBinding.inflate(inflater, container, false);

        binding.communitiesSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getCommunities(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.isEmpty()){
                    getCommunities("");
                }
                return false;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getCommunities("");
    }

    private void getCommunities(String searchValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference communitiesRef = db.collection("Communities");
        Query query = communitiesRef.orderBy("name", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                communityList.clear();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Community community = document.toObject(Community.class);
                        if(searchValue.equals("")){
                            communityList.add(community);
                        } else if (community.getId().contains(searchValue)) {
                            communityList.add(community);
                        } else {
                            if(community.getName().toLowerCase().contains(searchValue.toLowerCase())){
                                communityList.add(community);
                            }
                        }
                    }
                    if (context != null) {
                        binding.communitiesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.communitiesRecyclerView.setAdapter(new CommunitiesListAdapter(requireContext(), communityList, true, getFragmentManager()));
                    }
                }
            }
        });
    }
}