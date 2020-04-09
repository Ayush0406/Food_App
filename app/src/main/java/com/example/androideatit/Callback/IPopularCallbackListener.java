package com.example.androideatit.Callback;

import java.util.List;

import Model.PopularCategoryModel;

public interface IPopularCallbackListener {
    //the below are just two function definitions. Similar to what happens in interfaces.
    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels);
    void onPopularLoadFailed(String message);
}
