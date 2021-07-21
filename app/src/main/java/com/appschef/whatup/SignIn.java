package com.appschef.whatup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SignIn extends AppCompatActivity {
    private static final String TAG = "TAG";
    String phoneNumber, otpManual;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String mVerificationID = "";
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mRef = FirebaseDatabase.getInstance().getReference();
        findViewById(R.id.sendOtp).setOnClickListener(v -> {
            verify();
        });
        findViewById(R.id.verifyOtp).setOnClickListener(v -> {
            EditText editText = findViewById(R.id.otp);
            otpManual = editText.getText().toString();
            if (otpManual.length() == 6) {
                PhoneAuthCredential cred = PhoneAuthProvider.getCredential(mVerificationID, otpManual);
                signInWithPhoneAuthCredential(cred);
            }
            else {
                editText.setError("Enter correctly");
            }
        });
    }

    void verify() {

        EditText editText = findViewById(R.id.phone);
        phoneNumber = "+91" + editText.getText().toString();
        if (phoneNumber.length() != 13) {
            editText.setError("Phone number must be 10 digits");
        } else {
            PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    Log.w(TAG, "onVerificationFailed", e);
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                    }

                    // Show a message and update the UI
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    Log.d(TAG, "onCodeSent:" + verificationId);
Toast.makeText(SignIn.this,"Otp Sent",Toast.LENGTH_SHORT).show();
                    // Save verification ID and resending token so we can use them later
                    mVerificationID = verificationId;
//                mResendToken = token;
                }
            };


            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                 // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information


                        Log.d(TAG, "signInWithCredential:success");
                        Toast.makeText(SignIn.this, "Success", Toast.LENGTH_LONG).show();
                        FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                        assert user != null;
                        mRef.child("customers").child(Objects.requireNonNull(user.getPhoneNumber())).setValue(user.getUid()).addOnSuccessListener(aVoid -> {
                            mRef.child("tokens").child(user.getUid()).setValue(MyFirebaseMessagingService.getToken(SignIn.this))
                                    .addOnSuccessListener(aVoid1 -> startActivity(new Intent(SignIn.this,MainActivity.class)));
                        });
                        // Update UI
                    } else {
                        Toast.makeText(SignIn.this, Objects.requireNonNull(task.getException()).toString(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}