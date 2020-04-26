package com.example.androideatit.Delivery.order;

import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androideatit.Adapter.MyOrderAdapter_d;
import com.example.androideatit.Common.Common;
import com.example.androideatit.Common.MySwipeHelper;
import com.example.androideatit.R;
import com.example.androideatit.SignIn;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.OrderModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.internal.operators.flowable.FlowableCache;

import static java.lang.Math.round;

public class OrderFragment extends Fragment {

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;

    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    MyOrderAdapter_d adapter;

    private OrderViewModel orderViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        orderViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);
        View root =  inflater.inflate(R.layout.fragment_order, container, false);
        unbinder = ButterKnife.bind(this, root);
        initViews();

        orderViewModel.getMessageError().observe(this, s->{
            Toast.makeText(getContext(), "s", Toast.LENGTH_SHORT).show();
        });

        orderViewModel.getOrderModelMutableLiveData().observe(this, orderModels->{
                adapter = new MyOrderAdapter_d(getContext(), orderModels);
                recycler_order.setAdapter(adapter);
                recycler_order.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initViews() {
        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;



        MySwipeHelper mySwipeHelper  = new MySwipeHelper(getContext(), recycler_order, width/6)
        {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Accept", 30, 0, Color.parseColor("#9b0000"), pos->{
                    Dexter.withContext(getContext()).withPermission(Manifest.permission.SEND_SMS).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            OrderModel orderModel = adapter.getItemAtPosition(pos);
                            if(orderModel.getOrderStatus() == 1)
                            {
                                Toast.makeText(getContext(), "Order already accepted.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("orderStatus", 1);

                                //to calculate extra charge of delivery (based on distance)
                                Location locationA = new Location("");
                                Location locationB = new Location("");
                                locationB.setLatitude(Double.parseDouble(Common.currentDelivery.getLat()));
                                locationB.setLongitude(Double.parseDouble(Common.currentDelivery.getLng()));
                                locationA.setLatitude(orderModel.getLat());
                                locationA.setLongitude(orderModel.getLng());

                                double newPrice = orderModel.getTotalPayment() + locationA.distanceTo(locationB)*5e-3;
                                updateData.put("finalPayment", newPrice);
                                updateData.put("deliveryNumber", Common.currentDelivery.getPhone());
                                FirebaseDatabase.getInstance().getReference("Orders").child(orderModel.getKey())
                                        .updateChildren(updateData)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //update price locally
                                        orderModel.setFinalPayment(newPrice);

                                        adapter.updateItemStatusAtPosition(pos, 1);
                                        adapter.notifyItemChanged(pos);
                                        String phoneNumber = orderModel.getUserPhone();
                                        String message = new String("Dear " + orderModel.getUserName() + ",\nYour order has been accepted for delivery.\nTotal amount payable is: " + String.valueOf(round(newPrice)));
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                        Toast.makeText(getContext(), "Order Update Successful.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(getContext(), "Please accept the permission to continue.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    }).check();

                }));

                buf.add(new MyButton(getContext(), "Call", 30, 0, Color.parseColor("#560027"), pos->{
                    Dexter.withContext(getActivity()).withPermission(Manifest.permission.CALL_PHONE).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            OrderModel orderModel = adapter.getItemAtPosition(pos);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse(new StringBuilder("tel: ").append(orderModel.getUserPhone()).toString()));
                            startActivity(intent);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(getContext(), "Please accept the permission " + permissionDeniedResponse.getPermissionName() + " to proceed.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    }).check();
                }));

                buf.add(new MyButton(getContext(), "Reject", 30, 0, Color.parseColor("#12005e"), pos -> {
                    Dexter.withContext(getContext()).withPermission(Manifest.permission.SEND_SMS).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("Delete Order")
                                    .setMessage("Do you really want to reject this order?")
                                    .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                                    .setPositiveButton("REJECT", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            OrderModel orderModel = adapter.getItemAtPosition(pos);
                                            FirebaseDatabase.getInstance().getReference("Orders").child(orderModel.getKey()).child("orderStatus").setValue(-1)
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    adapter.removeItem(pos);
                                                    adapter.notifyItemRemoved(pos);
                                                    String phoneNumber = orderModel.getUserPhone();
                                                    String message = new String("Dear " + orderModel.getUserName() + ",\nSorry, We are unable to process your order right now.");
                                                    SmsManager smsManager = SmsManager.getDefault();
                                                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                                    Toast.makeText(getContext(), "Order has been successfully cancelled!", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                        }
                                    });


                            AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(getContext(), "Please accept the permission to continue.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    }).check();


                }));

                buf.add(new MyButton(getContext(), "Fulfilled", 30, 0, Color.parseColor("#336699"), pos->{
//                    showEditDialog(adapter.getItemAtPosition(pos), pos);
                    Dexter.withContext(getContext()).withPermission(Manifest.permission.SEND_SMS).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            OrderModel orderModel = adapter.getItemAtPosition(pos);
                            if(orderModel.getOrderStatus() == 2)
                            {
                                Toast.makeText(getContext(), "Order already completed.", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                FirebaseDatabase.getInstance().getReference("Orders").child(orderModel.getKey())
                                        .child("orderStatus").setValue(2)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        adapter.removeItem(pos);
                                        adapter.notifyItemRemoved(pos);

                                        String phoneNumber = orderModel.getUserPhone();
                                        String message = new String("Dear " + orderModel.getUserName() + ",\nYour order has been fulfilled.\nEnjoy your food!");
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);

                                        Toast.makeText(getContext(), "Order Status Successfully Updated.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(getContext(), "Please accept the permission to continue.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    }).check();



                }));
            }
        };
    }

//    private void showEditDialog(OrderModel orderModel, int pos) {
//        View layout_dialog;
//        AlertDialog.Builder builder;
//        if(orderModel.getOrderStatus() == 0) //pending
//        {
//            layout_dialog = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_shipping, null);
//            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen).setView(layout_dialog);
//        }
//        else if(orderModel.getOrderStatus() == -1) //cancelled
//        {
//            layout_dialog = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_cancelled, null);
//            builder = new AlertDialog.Builder(getContext()).setView(layout_dialog);
//        }
//        else
//        {
//            layout_dialog = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_shipped, null);
//            builder = new AlertDialog.Builder(getContext()).setView(layout_dialog);
//        }
//
//        Button btn_ok = (Button)layout_dialog.findViewById(R.id.btn_ok_d);
//        Button btn_cancel = (Button)layout_dialog.findViewById(R.id.btn_cancel_d);
//
//        RadioButton rdi_shipping = (RadioButton)layout_dialog.findViewById(R.id.rdi_shipping_d);
//        RadioButton rdi_shipped = (RadioButton)layout_dialog.findViewById(R.id.rdi_shipped_d);
//        RadioButton rdi_cancelled = (RadioButton)layout_dialog.findViewById(R.id.rdi_cancelled_d);
//        RadioButton rdi_delete = (RadioButton)layout_dialog.findViewById(R.id.rdi_delete_d);
//        RadioButton rdi_restore_placed = (RadioButton)layout_dialog.findViewById(R.id.rdi_restore_placed_d);
//
//        //set Data
//        TextView txt_status = (TextView)layout_dialog.findViewById(R.id.txt_status_d);
//        txt_status.setText(new StringBuilder("Order Staus(").append(Common.convertStatusToString_d(orderModel.getOrderStatus())));
//
//        //Create Dialog
//        AlertDialog dialog = builder.create();
//        dialog.show();
//        //Adding customizations to the dialog
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().setGravity(Gravity.CENTER);
//
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        btn_ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//                if(rdi_cancelled != null && rdi_cancelled.isChecked())
//                    updateOrder(pos, orderModel, -1);
//                else if(rdi_shipping != null && rdi_shipping.isChecked())
//                    updateOrder(pos, orderModel, 1);
//                else if(rdi_shipped != null && rdi_shipped.isChecked())
//                    updateOrder(pos, orderModel, 2);
//                else if(rdi_restore_placed != null && rdi_restore_placed.isChecked())
//                    updateOrder(pos, orderModel, 0);
//                else if(rdi_delete != null && rdi_delete.isChecked())
//                    deleteOrder(pos, orderModel);
//            }
//        });
//    }
//
//    private void updateOrder(int pos, OrderModel orderModel, int status)
//    {
//
//    }

}
