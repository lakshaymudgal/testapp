package com.example.viz_it;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;
import java.util.concurrent.TimeUnit;
import de.hdodenhof.circleimageview.CircleImageView;

public class Phone extends AppCompatActivity {


    TextView nextButton;
    EditText phoneNO, code;
    ProgressBar progressBar;
    TextView state;
    CircleImageView cimage;
    CountryCodePicker ccp;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken token;
    String number;

    boolean verificationProgress = false;
    private static  final String TAG = "tag";

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        phoneNO = findViewById(R.id.phoneNo);
        nextButton = findViewById(R.id.nextButton);
        code = findViewById(R.id.code);
        progressBar = findViewById(R.id.progressBr);
        state = findViewById(R.id.state);
        cimage = findViewById(R.id.pimage);
        ccp = findViewById(R.id.ccp);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        fstore = FirebaseFirestore.getInstance();

//        userId = firebaseAuth.getCurrentUser().getUid();

        cimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!verificationProgress) {
                    if (!phoneNO.getText().toString().isEmpty() && phoneNO.getText().toString().length() == 10) {


                        String phoneNumber = "+" + ccp.getSelectedCountryCode() + phoneNO.getText().toString();
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if(currentUser != null){
                            startActivity(new Intent(getApplicationContext(), OutputScreen.class));
                        }
                        if(currentUser == null ){
                            progressBar.setVisibility(View.VISIBLE);
                            state.setText("Sending OTP");
                            state.setVisibility(View.VISIBLE);
                            optRequest(phoneNumber);
                        }
                        else {
                            Snackbar.make(findViewById(android.R.id.content), "Number not match", Snackbar.LENGTH_LONG).show();
                        }

                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Required Valid Phone Number", Snackbar.LENGTH_LONG).show();
                    }
                }else {

                    String userOTP = code.getText().toString();

                    if(userOTP.length() == 6){
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, userOTP);
                            verifyOtp(credential);
                    }else {

                        Snackbar.make(findViewById(android.R.id.content), "Invaid OTP", Snackbar.LENGTH_LONG).show();
                    }


                }
            }
        });
    }


    private void verifyOtp(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {


            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Snackbar.make(findViewById(android.R.id.content), "Verification Successful", Snackbar.LENGTH_LONG).show();


                }
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Invalid Code", Snackbar.LENGTH_LONG).show();
                }

            }
        });

    }



//    <------------method for OTP Request------------->

    private void optRequest(final String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 30, TimeUnit.SECONDS,
                this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);

                        verificationId = s;
                        token = forceResendingToken;
                        progressBar.setVisibility(View.GONE);
                        state.setVisibility(View.GONE);
                        code.setVisibility(View.VISIBLE);
                        nextButton.setText("Verify");
                        verificationProgress = true;
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        Snackbar.make(findViewById(android.R.id.content), "OTP is not Entered", Snackbar.LENGTH_LONG).show();
                        optRequest(phoneNumber);

                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        verificationProgress = false;
                        verifyOtp(phoneAuthCredential);
//                        Upload upload = new Upload();
//                        String number = phoneNO.getText().toString();
//                        upload.setPhoneNumber(number);
//                        databaseReference.push().setValue(upload);

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Snackbar.make(findViewById(android.R.id.content), "Error in Verification", Snackbar.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            progressBar.setVisibility(View.INVISIBLE);
                            state.setVisibility(View.INVISIBLE);
                            Snackbar.make(findViewById(android.R.id.content), "Too many Request. Please Try again after some time", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
