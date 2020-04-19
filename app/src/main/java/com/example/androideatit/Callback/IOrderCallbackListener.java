package com.example.androideatit.Callback;

import java.util.List;

import Model.OrderModel;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void orOrderLoadFailed(String message);
}
