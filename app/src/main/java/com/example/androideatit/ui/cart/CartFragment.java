package com.example.androideatit.ui.cart;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Update;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.androideatit.Adapter.MyCartAdapter;
import com.example.androideatit.Common.Common;
import com.example.androideatit.Database.CartDataSource;
import com.example.androideatit.Database.CartDatabase;
import com.example.androideatit.Database.CartItem;
import com.example.androideatit.Database.LocalCartDataSource;
import com.example.androideatit.EventBus.HideFABCart;
import com.example.androideatit.EventBus.UpdateItemInCart;
import com.example.androideatit.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment {

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;
    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;


    private Unbinder unbinder;

    private CartViewModel cartViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);
        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(this, new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                if(cartItems == null || cartItems.isEmpty()){
                        recycler_cart.setVisibility(View.GONE);
                        group_place_holder.setVisibility(View.GONE);
                        txt_empty_cart.setVisibility(View.VISIBLE);

                }
                else{

                    recycler_cart.setVisibility(View.VISIBLE);
                    group_place_holder.setVisibility(View.VISIBLE);
                    txt_empty_cart.setVisibility(View.GONE);

                    MyCartAdapter adapter = new MyCartAdapter(getContext(),cartItems);
                    recycler_cart.setAdapter(adapter);

                }
            }
        });
        unbinder = ButterKnife.bind(this,root);
        initViews();
        return root;
    }

    private void initViews() {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        cartViewModel.onStop();
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event)
    {
        if (event.getCartItem() != null)
        {
            //first save state of recycler view
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                            //fix err refresh recycler view after update

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        txt_total_price.setText(new StringBuilder("Total: ")
                                .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getContext(), "[SUM CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

/*

<<<<<<< HEAD
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
=======


<<<<<<< HEAD
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


     *
}

=======
 */
