package com.example.petpawadmin.fragments.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.petpawadmin.R;
import com.example.petpawadmin.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ListenerRegistration listener;

    private FragmentHomeBinding binding;
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public void onPause() {
        super.onPause();
        Log.d("Home Fragment", "onPause");
        listener.remove();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Home Fragment", "onResume");
        notificationListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding= FragmentHomeBinding.inflate(inflater, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            int usersCount = queryDocumentSnapshots.size();
            binding.adminHomeUsersTextView.setText(String.valueOf(usersCount));
        });

        db.collection("Communities").get().addOnSuccessListener(queryDocumentSnapshots -> {
            int communitiesCount = queryDocumentSnapshots.size();
            binding.adminHomeCommunitiesTextView.setText(String.valueOf(communitiesCount));
        });

        db.collection("Posts").get().addOnSuccessListener(queryDocumentSnapshots -> {
            int postsCount = queryDocumentSnapshots.size();
            binding.adminHomePostsTextView.setText(String.valueOf(postsCount));
        });

        db.collection("Pets").get().addOnSuccessListener(queryDocumentSnapshots -> {
            int petsCount = queryDocumentSnapshots.size();
            binding.adminHomePetsTextView.setText(String.valueOf(petsCount));
        });

        //display the listview
//        db.collection("Admin").document("nolD8tefH4w9mwE9efzM").get().addOnSuccessListener(documentSnapshot -> {
//            List<String> adminNotifications = (List<String>) documentSnapshot.get("notifications");
//
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                    requireContext(),
//                    android.R.layout.simple_list_item_1,
//                    adminNotifications
//            );
//
//            binding.adminHomeNotificationListView.setAdapter(adapter);
//        });

        return binding.getRoot();
    }

    private void notificationListener(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Admin");
        DocumentReference docRef = usersRef.document("nolD8tefH4w9mwE9efzM");
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> adminNotifications = (List<String>) documentSnapshot.get("notifications");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    adminNotifications
            );

            binding.adminHomeNotificationListView.setAdapter(adapter);
        });

        listener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("Home Fragment", "get failed with " + error);
                    return;
                }

                if (value != null && value.exists()) {
                    List<String> adminNotifications = (List<String>) value.get("notifications");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            adminNotifications
                    );

                    binding.adminHomeNotificationListView.setAdapter(adapter);
                }
            }
        });
    }
}