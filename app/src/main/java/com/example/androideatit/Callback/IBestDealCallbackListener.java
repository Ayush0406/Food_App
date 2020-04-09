package com.example.androideatit.Callback;

import java.util.List;

import Model.BestDealModel;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
