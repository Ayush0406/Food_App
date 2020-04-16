package com.example.androideatit.ui.cart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.androideatit.R;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;

public class CartFragment extends Fragment {

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;




    @OnClick(R.id.btn_place_order)
    void onPlaceOrderCLick(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Order Details");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_address = (EditText)view.findViewById(R.id.edt_address);
        EditText edt_comment = (EditText)view.findViewById(R.id.edt_comment);
        TextView txt_address = (TextView)view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home = (RadioButton)view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = (RadioButton)view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_here = (RadioButton)view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = (RadioButton)view.findViewById(R.id.rdi_cod);

//        //Data
//        edt_address.setText(Common.currentUser.getAddress());

        //Event
        rdi_home.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
            {
                //edt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.GONE);
            }
        });
        rdi_other_address.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
            {
                edt_address.setText("");
                edt_address.setHint("Enter your address");
                txt_address.setVisibility(View.GONE);
            }
        });
        rdi_ship_here.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
            {
                 fusedLocationProviderClient.getLastLocation()
                         .addOnFailureListener(e -> {
                             Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                             txt_address.setVisibility(View.GONE);
                         })
                         .addOnCompleteListener(task -> {
                             String coordinates = new StringBuilder()
                                     .append(task.getResult().getLatitude())
                                     .append("/")
                                     .append(task.getResult().getLongitude()).toString();

                             Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(),
                                     task.getResult().getLongitude() ));

                             Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {

                                 @Override
                                 public void onSuccess(String s) {
                                     edt_address.setText(coordinates);
                                     txt_address.setText(s);
                                     txt_address.setVisibility(View.VISIBLE);
                                 }

                                 @Override
                                 public void onError(Throwable e) {
                                     edt_address.setText(coordinates);
                                     txt_address.setText(e.getMessage());
                                     txt_address.setVisibility(View.VISIBLE);

                                 }
                             });


                         });
            }
        });

        builder.setView(view);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("Yes", (dialogInterface, i) -> {
            Toast.makeText(getContext(),"Implement later", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result="";
        try{
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude, 1);
            if(addressList != null && addressList.size() > 0)
            {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            }
            else
                result = "Address not found";
        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    private void initLocation() {
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
    }

    private void buildLocationCallback() {

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }
    /* https://www.youtube.com/watch?v=nZ2gHpbAXHc&list=PLaoF-xhnnrRXx3V3mLCwYzAdcT_I9RxgL&index=21

    9 mins onwards add code
    if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);


     */
}

