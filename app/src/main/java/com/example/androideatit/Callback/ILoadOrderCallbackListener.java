package com.example.androideatit.Callback;

import java.util.List;

import Model.Order;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<Order> orderList);
    void onLoadOrderFailed(String message);
}
