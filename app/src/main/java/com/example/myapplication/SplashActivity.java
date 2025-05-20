package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.login.LoginActivity;
import com.example.myapplication.textstyle.TextGradientUtil;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        TextView textView = findViewById(R.id.Textgradient);
        TextGradientUtil.applyGradient(textView);

        textView.setOnClickListener(v->{
            setContentView(R.layout.activity_intro);
            Button button = findViewById(R.id.button);
            button.setOnClickListener(v1->{
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            });

        });


}}