package com.example.petpawadmin.fragments.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.petpawadmin.R;
import com.example.petpawadmin.adapters.UsersListAdapter;
import com.example.petpawadmin.databinding.FragmentUsersBinding;
import com.example.petpawadmin.models.User;
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
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private List<User> userList = new ArrayList<>();
    private Context context;

    private FragmentUsersBinding binding;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    public UsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
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
        binding = FragmentUsersBinding.inflate(inflater, container, false);

        binding.usersSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getUsers(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.isEmpty()){
                    getUsers("");
                }
                return false;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getUsers("");
    }

    private void getUsers(String searchValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        Query query = usersRef.orderBy("name", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                userList.clear();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
//                        if(user.getImageURL() == null){
//                            Log.d("User Fragment", "user image null ");
//                        }else{
//                            Log.d("User Fragment", "user: " + user.getImageURL());
//                        }
                        if(searchValue.equals("")){
                            User userTemp = new User(user.getUid(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress(), user.getImageURL());
                            userList.add(userTemp);
                        } else if (user.getUid().contains(searchValue)) {
                            User userTemp = new User(user.getUid(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress(), user.getImageURL());
                            userList.add(userTemp);
                        } else {
                            if(user.getName().toLowerCase().contains(searchValue.toLowerCase())){
                                User userTemp = new User(user.getUid(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress(), user.getImageURL());
                                userList.add(userTemp);
                            }
                        }
                    }
//                    for(User user: userList){
//                        Log.d("User Fragment", "user: " + user.getImageURL());
//                    }
                    if (context != null) {
                        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.usersRecyclerView.setAdapter(new UsersListAdapter(requireContext(), userList));
                    }
                }
            }
        });
    }
}