package com.appschef.whatup;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mRef;
    EditText newguy;
    String uid = mAuth.getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRef = FirebaseDatabase.getInstance().getReference();
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        SharedPref sharedPref = new SharedPref(this);
        if (isNetworkAvailable()) {

            mRef.child("chats").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    LinearLayout linearLayout = findViewById(R.id.allchats);
                    linearLayout.removeAllViews();
                    Set<String> strings = new HashSet<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        LinearLayout linearLayout1 = new LinearLayout(MainActivity.this);
                        TextView textView = new TextView(MainActivity.this);
                        textView.setText(s.getKey().substring(s.getKey().length() - 13));
                        textView.setTextColor(Color.WHITE);
                        textView.setTextSize(50);
                        strings.add(s.getKey());
                        Log.d("t", s.getKey());

                        linearLayout1.setBackgroundResource(R.drawable.bg2);
                        linearLayout1.setOnClickListener(v1 -> {
                            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                            intent.putExtra("who", s.getKey());
                            startActivity(intent);
                        });
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.setMargins(10, 35, 0, 10);
                        linearLayout1.setLayoutParams(params);

                        linearLayout1.addView(textView);
                        linearLayout.addView(linearLayout1);
                    }
                    sharedPref.setWhos(strings);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            LinearLayout linearLayout = findViewById(R.id.allchats);
            linearLayout.removeAllViews();
            Set<String> strings = sharedPref.getWhos();
            for (String string : strings) {
                LinearLayout linearLayout1 = new LinearLayout(MainActivity.this);
                TextView textView = new TextView(MainActivity.this);
                textView.setText(string.substring(string.length() - 13));
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(50);
                linearLayout1.setBackgroundResource(R.drawable.bg2);
                linearLayout1.setOnClickListener(v1 -> {
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    intent.putExtra("who", string);
                    startActivity(intent);
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(10, 35, 0, 10);
                linearLayout1.setLayoutParams(params);
                linearLayout1.addView(textView);
                linearLayout.addView(linearLayout1);
            }


        }
        findViewById(R.id.add).setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                showAddDialog();
            } else {
                Toast.makeText(this, "No Internet Available", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void showAddDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new);
        dialog.setCancelable(true);
        dialog.show();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        dialog.getWindow().setLayout((6 * width) / 7, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        newguy = dialog.findViewById(R.id.newGuy);
        dialog.findViewById(R.id.add).setOnClickListener(v -> {
            mRef.child("customers").child("+91" + newguy.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        mRef.child("chats").child(snapshot.getValue().toString()).child("+91" + newguy.getText().toString() + mAuth.getCurrentUser().getPhoneNumber()).child(String.valueOf(System.currentTimeMillis())).setValue("hel");
                        mRef.child("chats").child(uid).child(mAuth.getCurrentUser().getPhoneNumber() + "+91" + newguy.getText().toString()).child(String.valueOf(System.currentTimeMillis())).setValue("hel");
                    } else {
                        Toast.makeText(MainActivity.this, "No Such user exist", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });


        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.signOut);
        menuItem.setOnMenuItemClickListener(item -> {
            if(isNetworkAvailable()) {
                mRef.child("tokens").child(uid).setValue("");
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, SignIn.class));
            }
            else {
                Toast.makeText(MainActivity.this,"No Internet",Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}