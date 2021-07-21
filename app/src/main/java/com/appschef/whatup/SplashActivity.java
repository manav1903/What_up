package com.appschef.whatup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(this,MainActivity.class));
        }
        else {
            startActivity(new Intent(this,SignIn.class));
        }
    }
}