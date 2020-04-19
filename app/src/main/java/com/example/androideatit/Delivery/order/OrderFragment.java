package com.example.androideatit.Delivery.order;

import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.androideatit.Adapter.MyOrderAdapter_d;
import com.example.androideatit.Common.MySwipeHelper;
import com.example.androideatit.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

import Model.OrderModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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
                buf.add(new MyButton(getContext(), "Directions", 30, 0, Color.parseColor("#9b0000"), pos->{

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


                buf.add(new MyButton(getContext(), "Remove", 30, 0, Color.parseColor("#12005e"), pos -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("Delete Order")
                            .setMessage("Do you really want to delete this order?")
                            .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    OrderModel orderModel = adapter.getItemAtPosition(pos);
                                    FirebaseDatabase.getInstance().getReference("Orders").child(orderModel.getKey()).removeValue()
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
                                            Toast.makeText(getContext(), "Order has been successfully cancelled!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });


                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
                }));

                buf.add(new MyButton(getContext(), "Edit", 30, 0, Color.parseColor("#336699"), pos->{

                }));
            }
        };
    }

}
