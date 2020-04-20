package com.example.androideatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.androideatit.Common.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import Model.DeliveryPerson;
import Model.User;
import dmax.dialog.SpotsDialog;

public class SignInDelivery extends AppCompatActivity {

    MaterialEditText edtPhone, edtPassword;
    Button btnSignIn;
    FirebaseDatabase database;
    DatabaseReference table_user;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_delivery);

        edtPhone = (MaterialEditText)findViewById(R.id.edt_phone_d);
        edtPassword = (MaterialEditText)findViewById(R.id.edt_password_d);
        btnSignIn = (Button)findViewById(R.id.btn_sign_in_d);

        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("DeliveryPerson");

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Check if the user information exists in the database and fetch, if it does.

                        dialog.dismiss();

                        if(dataSnapshot.child(edtPhone.getText().toString().replace(".", ",")).exists()) {
                            DeliveryPerson deliveryPerson = dataSnapshot.child(edtPhone.getText().toString().replace(".", ",")).getValue(DeliveryPerson.class);
                            if (deliveryPerson.getPassword().equals(edtPassword.getText().toString())) {
                                Common.currentDelivery = deliveryPerson;
//                                Toast.makeText(SignIn.this, user.getAddress() + user.getEmail() + user.getCount(), Toast.LENGTH_SHORT).show();
//                                database.getReference("Address").child(Common.getUid()).setValue("udaipur akashvani colony");
//                                database.getReference("Address").child(Common.getUid()).child("abc").setValue(123);
                                Toast.makeText(SignInDelivery.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                                Intent home_delivery = new Intent(SignInDelivery.this, HomeDelivery.class);
                                startActivity(home_delivery);
                                finish();
                            } else {
                                Toast.makeText(SignInDelivery.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(SignInDelivery.this, "User does not exit.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SignInDelivery.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }
}
