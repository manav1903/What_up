package com.appschef.whatup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity2 extends AppCompatActivity {
    String myNo, sendNo, sendUid, token, who;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mRef;
    String uid = mAuth.getCurrentUser().getUid();
    SharedPref sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        sharedPref= new SharedPref(this);
        mRef = FirebaseDatabase.getInstance().getReference();
        who = getIntent().getStringExtra("who");
        sendNo = who.substring(who.length() - 13);
        myToolbar.setTitle(sendNo);
        myNo = mAuth.getCurrentUser().getPhoneNumber();
        Log.d("Tag", who);
        if (isNetworkAvailable()) {
            mRef.child("chats").child(uid).child(who).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    JSONArray jsonArray = new JSONArray();
                    LinearLayout linearLayout = findViewById(R.id.chat);
                    linearLayout.removeAllViews();
                    String lastSet="";
                    for (DataSnapshot s : snapshot.getChildren()) {
                        long yourmilliseconds = Long.parseLong(s.getKey().toString());
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
                        Date resultdate = new Date(yourmilliseconds);
                        String date=sdf.format(resultdate);
                        if(!date.equals(lastSet)){
                            TextView textView2 = new TextView(MainActivity2.this);
                            RelativeLayout linearLayout2 = new RelativeLayout(MainActivity2.this);
                            textView2.setBackgroundColor(getColor(R.color.teal_700));
                            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            textView2.setText(date);
                            textView2.setTextSize(20);
                            textView2.setTextColor(Color.WHITE);
                            layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
                            textView2.setPadding(20,20,20,20);
                            textView2.setLayoutParams(layoutParams2);
                            textView2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            linearLayout2.addView(textView2);
                            linearLayout.addView(linearLayout2);
                            lastSet=date;
                        }
                        TextView textView = new TextView(MainActivity2.this);
                        RelativeLayout linearLayout1 = new RelativeLayout(MainActivity2.this);
                        textView.setBackgroundResource(R.drawable.bg3);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                        JSONObject jsonObject = new JSONObject();
                        String text = s.getValue().toString();
                        textView.setText(text.substring(3));
                        textView.setTextSize(20);
                        textView.setTextColor(Color.WHITE);
                        try {
                            jsonObject.put(s.getKey().toString(), s.getValue().toString());
                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (text.contains("@@!")) {
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                            layoutParams.setMarginStart(90);
                            textView.setPadding(20,20,20,20);
                            textView.setLayoutParams(layoutParams);
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        }
                        else {
                            layoutParams.setMarginEnd(90);
                            textView.setPadding(20,20,20,20);
                            textView.setLayoutParams(layoutParams);
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        }

                        textView.setFocusableInTouchMode(true);
                        textView.requestFocus();
                        linearLayout1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linearLayout1.addView(textView);
                        linearLayout.addView(linearLayout1);
                    }
                    chatLocal(jsonArray.toString());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            mRef.child("customers").child(sendNo).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    sendUid = snapshot.getValue().toString();
                    mRef.child("tokens").child(sendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            token = snapshot1.getValue().toString();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });



        } else {
            loadLocalChats();
        }
              findViewById(R.id.add).setOnClickListener(v -> {

                  if(isNetworkAvailable()) {
                      EditText editText = findViewById(R.id.text);
                      String text = editText.getText().toString();
                      if (text.length() >= 3) {
                          String substring = text.substring(Math.max(text.length() - 4, 0));
                          if (substring.contains("\n\n")) {
                              text = text.substring(0, text.length() - 2);
                          }
                      }
                      if (text.length() >= 2) {
                          String substring = text.substring(Math.max(text.length() - 2, 0));
                          if (substring.contains("\n")) {
                              text = text.substring(0, text.length() - 1);
                          }

                      }

                      if (!text.equals("") && !text.equals("\n") && !text.equals("\n\n")) {
                          String milli = String.valueOf(System.currentTimeMillis());

                          mRef.child("chats").child(uid).child(who).child(milli).setValue("@@!" + text);
                          mRef.child("chats").child(sendUid).child(sendNo + mAuth.getCurrentUser().getPhoneNumber()).child(milli).setValue("!@@" + editText.getText().toString());

                          try {
                              func("What Up", editText.getText().toString());
                          } catch (MalformedURLException e) {
                              e.printStackTrace();
                          }
                          editText.setText("");
                      }
                  }
                  else {
                      Toast.makeText(this,"Internet Connection Required",Toast.LENGTH_SHORT).show();
                  }
            });



    }
    void chatLocal(String json) {
        sharedPref.setChats(who, json);
    }

    void loadLocalChats() {
        LinearLayout linearLayout = findViewById(R.id.chat);
        linearLayout.removeAllViews();
        String lastSet="";
        try {
            JSONArray jsonArray = new JSONArray(sharedPref.getChats(who));
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Log.e("polw",jsonArray.toString());
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
//                    Log.d("Tagi",keys.next());
                    String key = keys.next().toString();
                    RelativeLayout linearLayout1 = new RelativeLayout(MainActivity2.this);
                    TextView textView = new TextView(MainActivity2.this);
                    String text = jsonObject.getString(key);
                    long yourmilliseconds = Long.parseLong(key);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
                    Date resultdate = new Date(yourmilliseconds);
                    String date=sdf.format(resultdate);
                    if(!date.equals(lastSet)){
                        TextView textView2 = new TextView(MainActivity2.this);
                        RelativeLayout linearLayout2 = new RelativeLayout(MainActivity2.this);
                        textView2.setBackgroundColor(getColor(R.color.teal_700));
                        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        textView2.setText(date);
                        textView2.setTextSize(20);
                        textView2.setTextColor(Color.WHITE);
                        layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
                        textView2.setPadding(20,20,20,20);
                        textView2.setLayoutParams(layoutParams2);
                        textView2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linearLayout2.addView(textView2);
                        linearLayout.addView(linearLayout2);
                        lastSet=date;
                    }
                    textView.setText(text.substring(3));
                    textView.setTextSize(20);
                    textView.setTextColor(Color.WHITE);
                    textView.setBackgroundResource(R.drawable.bg3);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (text.contains("@@!")) {
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        layoutParams.setMarginStart(90);
                        textView.setPadding(20,20,20,20);
                        textView.setLayoutParams(layoutParams);
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    }
                    else {
                        layoutParams.setMarginEnd(90);
                        textView.setPadding(20,20,20,20);
                        textView.setLayoutParams(layoutParams);
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    }
                    textView.setFocusableInTouchMode(true);
                    textView.requestFocus();
                    linearLayout1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayout1.addView(textView);
                    linearLayout.addView(linearLayout1);
                }


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void func(String title, String body) throws MalformedURLException {
        try {


            JSONObject jsonObject = new JSONObject();
            JSONObject param = new JSONObject();
            JSONObject object = new JSONObject();
            object.put("title", title);
            object.put("body", body);
            object.put("action", sendNo + myNo);
            jsonObject.put("to", token);

//            jsonObject.put("to", "/topics/customer");
            jsonObject.put("data", object);
//            jsonObject.put("data", param);
            Log.d("kopl", jsonObject.toString());
            post("https://fcm.googleapis.com/fcm/send", jsonObject.toString(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            //Something went wrong
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String responseStr = response.body().string();
                                Log.d("ResponseSuccess", responseStr);
                                // Do what you want to do with the response.
                            } else {

                                Log.d("Response", response.body().string());
                                // Request not successful
                            }
                        }
                    }
            );
        } catch (JSONException ex) {
            Log.d("Exception", "JSON exception", ex);
        }
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    void post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "key=AAAAKOxdWSE:APA91bGHz_-ifJG5iR0qZAR18VgjJUnDKH6ZdhC6avoIgAV2Tp7AfySFotiJtJ2SIJQcFXCWe2eV799MHR-w_yWby_8vxOYKvJF0ZC75xrbpjPipLvnPbTG8bMUCG6ZOKmJ42rgaXz2p")
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
    }
}