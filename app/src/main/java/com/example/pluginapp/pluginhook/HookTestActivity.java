package com.example.pluginapp.pluginhook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.pluginapp.R;

public class HookTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hook_test);
    }
}