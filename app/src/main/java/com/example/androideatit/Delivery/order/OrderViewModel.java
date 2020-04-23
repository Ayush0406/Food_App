package com.example.androideatit.Delivery.order;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.androideatit.Callback.IOrderCallbackListener;
import com.example.androideatit.Common.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Order;
import Model.OrderModel;

public class OrderViewModel extends ViewModel implements IOrderCallbackListener {
    private MutableLiveData<List<OrderModel>> orderModelMutableLiveData;
    private MutableLiveData<String> messageError;
    private IOrderCallbackListener listener;

    public OrderViewModel() {
        orderModelMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<List<OrderModel>> getOrderModelMutableLiveData() {
        loadOrderByStatus(0);
        return orderModelMutableLiveData;
    }

    private void loadOrderByStatus(int status) {
        List<OrderModel> tempList = new ArrayList<>();
//        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF).orderByChild("orderStatus").equalTo(status);
//        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF);
//        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot itemSnapshot: dataSnapshot.getChildren())
//                {
//                    OrderModel orderModel = itemSnapshot.getValue(OrderModel.class);
//                    orderModel.setKey(itemSnapshot.getKey());
//                    tempList.add(orderModel);
//                }
//                listener.onOrderLoadSuccess(tempList);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                listener.orOrderLoadFailed(databaseError.getMessage());
//            }
//        });
        Location locationA = new Location("");
        Location locationB = new Location("");
        locationB.setLatitude(Double.parseDouble(Common.currentDelivery.getLat()));
        locationB.setLongitude(Double.parseDouble(Common.currentDelivery.getLng()));
        int radius = Integer.parseInt(Common.currentDelivery.getRadius());

        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders");
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapShot: dataSnapshot.getChildren())
                {
                    OrderModel orderModel = itemSnapShot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapShot.getKey());
                    locationA.setLatitude(orderModel.getLat());
                    locationA.setLongitude(orderModel.getLng());
                    if(locationA.distanceTo(locationB) <= radius*1000) {
                        tempList.add(orderModel);
                    }
                }
                listener.onOrderLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderModelList) {
//        if(orderModelList.size() > 0)
//        {
//            Collections.sort(orderModelList, (orderModel, t1)->{
//                if(orderModel.getCreateDate()  < t1.getCreateDate())
//                {
//                    return -1;
//                }
//                return orderModel.getCreateDate() == t1.getCreateDate() ? 0 : 1;
//            });
//            orderModelMutableLiveData.setValue(orderModelList);
//        }
        orderModelMutableLiveData.setValue(orderModelList);
    }

    @Override
    public void orOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}
