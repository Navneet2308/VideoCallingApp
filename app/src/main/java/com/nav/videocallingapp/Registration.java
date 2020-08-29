package com.nav.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class Registration extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText phonetext,codetext;
    private Button continueAndnextBtn;
    private String checker="", phonenumber="";
    private RelativeLayout relativeLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private  String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(Registration.this);
        phonetext=findViewById(R.id.phoneText);
        codetext=findViewById(R.id.codeText);
        continueAndnextBtn=findViewById(R.id.continueNextButton);
        relativeLayout=findViewById(R.id.phoneAuth);
        ccp=(CountryCodePicker)findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phonetext);

        continueAndnextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueAndnextBtn.getText().equals("Submit")|| checker.equals("Code Sent"))
                {
                    String verificationCode = codetext.getText().toString();


                    if (verificationCode.equals(""))
                    {
                        Toast.makeText(Registration.this, "Please wirte verification Code First", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        loadingBar.setTitle("Code Verifying");
                        loadingBar.setMessage("Please wait, while we are verifying your code");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();


                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
                else
                {
                    phonenumber = ccp.getFullNumberWithPlus();
                    if (!phonenumber.equals(""))
                    {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait, while we are verifying your phone number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                      PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenumber,60,TimeUnit.SECONDS,Registration.this,mCallbacks);
                    }
                    else
                    {
                        Toast.makeText(Registration.this, "Please write vaild number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingBar.dismiss();
                Toast.makeText(Registration.this, "Invaild Phone no", Toast.LENGTH_SHORT).show();
                relativeLayout.setVisibility(View.VISIBLE);

                continueAndnextBtn.setText("Continue");
                codetext.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId=s;
                mResendToken=forceResendingToken;
                relativeLayout.setVisibility(View.GONE);
                checker="Code Sent";
                continueAndnextBtn.setText("Submit");
                codetext.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(Registration.this, "Code has been sent,Please check.", Toast.LENGTH_SHORT).show();

            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            loadingBar.dismiss();
                            Toast.makeText(Registration.this, "Congatulation You are login successfully", Toast.LENGTH_SHORT).show();
                            sendUserTOMainActivity();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            loadingBar.dismiss();
                            String e=task.getException().getMessage().toString();
                            Toast.makeText(Registration.this, e, Toast.LENGTH_SHORT).show();
                            }
                        }

                });
    }

    private  void  sendUserTOMainActivity()
    {

        Intent intent=new Intent(Registration.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser!=null)
        {
            Intent homeintent=new Intent(Registration.this, ContactsActivity.class);
            startActivity(homeintent);
            finish();
        }
    }
}
