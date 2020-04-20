package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import Model.User;
import dmax.dialog.SpotsDialog;

public class SignUp extends AppCompatActivity {

    MaterialEditText edtPhone, edtName, edtPassword, edtEmail, edtAddress;
    Button btnSignUp;

    GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    FirebaseAuth mAuth;
    int RC_SIGN_IN = 1;

    AlertDialog dialog;
    boolean flag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = (MaterialEditText)findViewById(R.id.edtName);
        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        edtPhone = (MaterialEditText)findViewById(R.id.edtPhone);
        edtEmail = (MaterialEditText)findViewById(R.id.edtEmail);
        edtAddress = (MaterialEditText)findViewById(R.id.edtAddress);

        btnSignUp = (Button)findViewById(R.id.btnSignUp);

        //social sign-up fields
        signInButton = findViewById(R.id.sign_up_button);
        mAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        AlertDialog mdialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User"); //name of database in firebase

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

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mdialog.setMessage("Loading, Please wait.....");
                mdialog.show();

                table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //to check whether user already exists
                        mdialog.dismiss();
                        if(flag == false) {
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                Toast.makeText(SignUp.this, "Account already registered! Please Sign in.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                User user = new User(edtName.getText().toString(), edtPassword.getText().toString(), edtAddress.getText().toString(), edtEmail.getText().toString(), "0", edtPhone.getText().toString(), edtPhone.getText().toString());
                                table_user.child(edtPhone.getText().toString().replace(".", ",")).setValue(user);
                                Toast.makeText(SignUp.this, "Sign Up successful. Please Sign In.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        else
                        {
                            if (dataSnapshot.child(edtEmail.getText().toString().replace(".", ",")).exists()) {
                                Toast.makeText(SignUp.this, "Account already registered! Please Sign in.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                User user = new User(edtName.getText().toString(), edtPassword.getText().toString(), edtAddress.getText().toString(), edtEmail.getText().toString(), "0", edtPhone.getText().toString(), edtPhone.getText().toString());
                                table_user.child(edtEmail.getText().toString().replace(".", ",")).setValue(user);
                                Toast.makeText(SignUp.this, "Sign Up successful. Please Sign In.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
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
            Toast.makeText(this, "Account Validation Successful", Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(acc);
        }
        catch (ApiException e)
        {
            Toast.makeText(this, "Account Validation Failed", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SignUp.this, "Firebase Authentication Successful!", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(SignUp.this, "Firebase Authentication Failed!", Toast.LENGTH_SHORT).show();
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
            signInButton.setVisibility(View.INVISIBLE);
            edtEmail.setText(user.getEmail());
            edtName.setText(user.getDisplayName());
            edtPhone.setText(user.getPhoneNumber());
            if(edtEmail.getText().length()>0)
            {
                edtEmail.setEnabled(false);
                edtEmail.setPrimaryColor(Color.WHITE);
            }

            if(edtName.getText().length()>0)
            {
                edtName.setEnabled(false);
            }

            if(edtPhone.getText().length()>0)
            {
                edtPhone.setEnabled(false);
            }
            flag = true;
            Toast.makeText(this, "Please enter your credentials.", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            dialog.dismiss();
        }
    }
}
