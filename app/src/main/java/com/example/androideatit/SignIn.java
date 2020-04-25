package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.androideatit.Common.Common;
import com.example.androideatit.Delivery.order.OrderFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Model.User;
import dmax.dialog.SpotsDialog;


public class SignIn extends AppCompatActivity {

    EditText edtPhone, edtPassword;
    Button btnSignIn;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    FirebaseAuth mAuth;
    int RC_SIGN_IN = 1;
    FirebaseDatabase database;
    DatabaseReference table_user;
    AlertDialog dialog;

    Button btnDeliverySignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPhone = (MaterialEditText)findViewById((R.id.edtPhone));
        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        signInButton = findViewById(R.id.sign_in_button);
        mAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User"); //name of database in firebase

        btnDeliverySignIn = (Button) findViewById(R.id.btn_delivery_signin);



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mdialog = new ProgressDialog(SignIn.this);
                mdialog.setMessage("Loading, Please wait.....");
                mdialog.show();

                table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Check if the user information exists in the database and fetch, if it does.

                        mdialog.dismiss();

                        if(dataSnapshot.child(edtPhone.getText().toString().replace(".", ",")).exists()) {
                            User user = dataSnapshot.child(edtPhone.getText().toString().replace(".", ",")).getValue(User.class);
                            if (user.getPassword().equals(edtPassword.getText().toString())) {
                                Common.currentUser = user;
                                Common.setUid(edtPhone.getText().toString().replace(".", ","));
//                                Toast.makeText(SignIn.this, user.getAddress() + user.getEmail() + user.getCount(), Toast.LENGTH_SHORT).show();
//                                database.getReference("Address").child(Common.getUid()).setValue("udaipur akashvani colony");
//                                database.getReference("Address").child(Common.getUid()).child("abc").setValue(123);
                                Toast.makeText(SignIn.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
//                                getLatLngFromAddress(user.getAddress());
//                                Log.d("address", lat+" "+lng);
                                Intent home = new Intent(SignIn.this, Home.class);
                                FirebaseInstanceId.getInstance()
                                        .getInstanceId()
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SignIn.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            startActivity(home);
                                            finish();
                                        }).addOnCompleteListener(task -> {
                                    Common.updateToken(SignIn.this, task.getResult().getToken());
                                    startActivity(home);
                                    finish();
                                });

                            } else {
                                Toast.makeText(SignIn.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
//                            String phoneNumber = "+919660059982";
//                            String message = "You do not exist.";
//                            final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
//                            if(!checkPermission(Manifest.permission.SEND_SMS))
//                            {
//                                ActivityCompat.requestPermissions(SignIn.this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
//                            }
//
//                            if(checkPermission(Manifest.permission.SEND_SMS)) {
//                                SmsManager smsManager = SmsManager.getDefault();
//                                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
//                                Toast.makeText(SignIn.this, "User does not exit.", Toast.LENGTH_SHORT).show();
//                            }
                            Toast.makeText(SignIn.this, "User does not exit.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        btnDeliverySignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInDelivery = new Intent(SignIn.this, SignInDelivery.class);
                startActivity(signInDelivery);
            }
        });
    }

//    String getLatLngFromAddress(String address)
//    {
//        Geocoder geocoder = new Geocoder(this);
//        String result="";
//        try
//        {
//            List<Address> addressList = geocoder.getFromLocationName(address, 1);
//            if(addressList != null && addressList.size() > 0) {
//                double lat = addressList.get(0).getLatitude();
//                double lng = addressList.get(0).getLongitude();
//                result = String.valueOf(lat) + " " + String.valueOf(lng);
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//            result = e.getMessage();
//        }
//        Log.d("test address", result);
//        return result;
//    }

    boolean checkPermission(String permission)
    {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private void signIn()
    {
        mGoogleSignInClient.signOut(); //to clear previously cached account.
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task)
    {
        dialog.show();
        try
        {
            GoogleSignInAccount acc = task.getResult(ApiException.class);
            Toast.makeText(this, "Google account validated successfully!", Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(acc);
        }
        catch (ApiException e)
        {
            Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

    //Convert Google SignIn credential to firebase credential
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information.
                            Toast.makeText(SignIn.this, "Firebase Authentication Successful!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignIn.this, "Firebase Authentication Failed!", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user)
    {
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(user != null)
        {
            edtPhone.setText(user.getEmail());
            table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //Check if the user information exists in the database and fetch, if it does.
                    if(dataSnapshot.child(edtPhone.getText().toString().replace(".", ",")).exists()) {
                        User user = dataSnapshot.child(edtPhone.getText().toString().replace(".", ",")).getValue(User.class);
                        Common.currentUser = user;
                        Common.setUid(edtPhone.getText().toString().replace(".", ","));
                        Toast.makeText(SignIn.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                        Intent home = new Intent(SignIn.this, Home.class);
                        startActivity(home);
                        finish();
                    }
                    else
                    {
                        edtPhone.setText("");
                        Toast.makeText(SignIn.this, "User does not exit.", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


}
