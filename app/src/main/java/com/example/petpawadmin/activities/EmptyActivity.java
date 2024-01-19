package com.example.petpawadmin.activities;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petpawadmin.R;


public class EmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        finish();
    }
}