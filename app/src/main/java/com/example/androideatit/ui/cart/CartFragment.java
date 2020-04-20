package com.example.androideatit.ui.cart;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Looper;

import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.room.ColumnInfo;
import androidx.room.Update;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.androideatit.Adapter.MyCartAdapter;
import com.example.androideatit.Callback.ILoadTimeFromFirebaseListener;
import com.example.androideatit.Common.Common;
import com.example.androideatit.Common.MySwipeHelper;
import com.example.androideatit.Database.CartDataSource;
import com.example.androideatit.Database.CartDatabase;
import com.example.androideatit.Database.CartItem;
import com.example.androideatit.Database.LocalCartDataSource;
import com.example.androideatit.EventBus.CounterCartEvent;
import com.example.androideatit.EventBus.HideFABCart;
import com.example.androideatit.EventBus.MenuItemBack;
import com.example.androideatit.EventBus.UpdateItemInCart;
import com.example.androideatit.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.Order;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.OnClick;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.Single;
import io.reactivex.observers.DisposableSingleObserver;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;



import android.widget.EditText;
import android.widget.RadioButton;

//import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;


public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
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
    private MyCartAdapter adapter;
    private CartViewModel cartViewModel;

    ILoadTimeFromFirebaseListener listener;

    String current_value;
    int count = Integer.parseInt(Common.currentUser.getCount());
    Double discount = 1.0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);
        cartViewModel.initCartDataSource(getContext());

        if (count >= 5 && count < 10)
            discount = 0.95;

        else if(count > 10)
            discount = 0.90;

        listener = this;

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

                    adapter = new MyCartAdapter(getContext(),cartItems);
                    recycler_cart.setAdapter(adapter);

                }
            }
        });
        unbinder = ButterKnife.bind(this,root);
        initViews();
        initLocation();
        return root;
    }

    private void initViews() {
        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));


        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_cart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Delete", 30, 0, Color.parseColor("#FF3C30"),
                            pos -> {
                                CartItem cartItem = adapter.getItemAtPositon(pos);
                                cartDataSource.deleteCartItem(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<Integer>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(Integer integer) {
                                                adapter.notifyItemRemoved(pos);
                                                sumAllItemInCart(); //update total price
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true)); //update FAB
                                                Toast.makeText(getContext(), "Item deleted from cart successfully", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }));

            }
        };

        sumAllItemInCart();
    }

    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        
                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        txt_total_price.setText(new StringBuilder("Total: ").append(Common.formatPrice(aDouble*discount)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        cartViewModel.onStop();
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();

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
                                .append(Common.formatPrice(price*discount)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getContext(), "[SUM CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_clear_cart)
        {
            cartDataSource.cleanCart(Common.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(getContext(), "Cart cleared successfully", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_place_order)
    void onPlaceOrderCLick(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Order Details");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_address = view.findViewById(R.id.edt_address);
        EditText edt_comment = view.findViewById(R.id.edt_comment);
        TextView txt_address = view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home = (RadioButton)view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = (RadioButton)view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_here = (RadioButton)view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = (RadioButton)view.findViewById(R.id.rdi_cod);

        //Data
        edt_address.setText(Common.currentUser.getAddress());

        //Event
        rdi_home.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
            {
                    edt_address.setText(Common.currentUser.getAddress());
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
        }).setPositiveButton("Proceed", (dialogInterface, i) -> {
            if(rdi_cod.isChecked())
                paymentCOD(edt_address.getText().toString(), edt_comment.getText().toString());
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.getUid())
            .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CartItem>>() {
                    @Override
                    public void accept(List<CartItem> cartItems) throws Exception {
                        //when we have all cart items we will get price
                        cartDataSource.sumPriceInCart(Common.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Double>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Double totalPrice) {
                                        double finalPrice = totalPrice * discount; //discount will be used later
                                        Order order = new Order();
                                        order.setUserId(Common.getUid());
                                        order.setUserName(Common.currentUser.getName());
                                        order.setUserPhone(Common.currentUser.getPhone());
                                        order.setShippingAddress(address);
                                        order.setComment(comment);

                                        if(currentLocation != null)
                                        {
                                            order.setLat(currentLocation.getLatitude());
                                            order.setLng(currentLocation.getLongitude());
                                        }
                                        else
                                        {
                                            order.setLat(-0.1f);
                                            order.setLng(-0.1f);
                                        }
                                        order.setCartItemList(cartItems);
                                        order.setTotalPayment(totalPrice);
                                        order.setDiscount(0); //to be modified later
                                        order.setFinalPayment(finalPrice);
                                        order.setCod(true);
                                        order.setTransactionID("Cash On Delivery");

                                        //writeOrderToFirebase(order);
                                        syncLocalTimeWithGlobalTime(order);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Toast.makeText(getContext(),"1" + e.getMessage(), Toast.LENGTH_SHORT).show();;
                                    }
                                });

                    }
                }, throwable -> {
                        Toast.makeText(getContext(),"2" + throwable.getMessage(), Toast.LENGTH_SHORT).show();;
                }));
    }

    private void syncLocalTimeWithGlobalTime(Order order) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long offset = dataSnapshot.getValue(Long.class);
                long estimateServerTimeMs = System.currentTimeMillis() + offset;
                //offset is missing time between your local time and server time
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM,yyyy HH:mm");
                Date resultDate = new Date(estimateServerTimeMs);

                Log.d("TEST_DATE", ""+sdf.format(resultDate));
                listener.onLoadTimeSuccess(order, estimateServerTimeMs);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onLoadTimeFailed(databaseError.getMessage());
            }
        });
    }

    private void writeOrderToFirebase(Order order) {

        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),"3"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    //write success

                    cartDataSource.cleanCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    //clean success
                                    updateUserCount();
                                    Toast.makeText(getContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getContext(),"4"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }


    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result="";
        try{
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude, 4);
            if(addressList != null && addressList.size() > 0)
            {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
//                StringBuilder f = new StringBuilder(addressList.get(2).getAddressLine((0))).append(addressList.get(2).getAddressLine(1));
                StringBuilder sb1 = new StringBuilder(address.getAddressLine(1));
                StringBuilder sb2 = new StringBuilder(address.getAddressLine(2));
                StringBuilder sb3 = new StringBuilder(address.getAddressLine(3));
                String[] suggestions = {sb.toString(), sb1.toString(),sb2.toString(), sb3.toString()};
//                float[] resu = new float[4];
//                Location.distanceBetween(latitude,longitude,19.09,73.01,resu);
//                Log.d("Address2",""+String.valueOf(resu[0]));
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
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    public void updateUserCount() {

        current_value = Common.currentUser.getCount();
        current_value = Integer.toString(Integer.parseInt(current_value) + 1);

        FirebaseDatabase.getInstance().getReference("User")
                .child(Common.getUid())
                .child("count")
                .setValue(current_value);

    }

    @Override
    public void onLoadTimeSuccess(Order order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        order.setOrderStatus(0);
        writeOrderToFirebase(order);
    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(),""+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
