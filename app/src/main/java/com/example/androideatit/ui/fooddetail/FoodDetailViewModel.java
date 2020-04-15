package com.example.androideatit.ui.fooddetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.androideatit.Common.Common;

import Model.FoodModel;

public class FoodDetailViewModel extends ViewModel {

    private MutableLiveData<FoodModel> foodModelMutableLiveData;

    public FoodDetailViewModel() {
    }

    public MutableLiveData<FoodModel> getFoodModelMutableLiveData() {
        if(foodModelMutableLiveData == null)
        {
            foodModelMutableLiveData = new MutableLiveData<>();
        }
        foodModelMutableLiveData.setValue(Common.selectedFood);
        return foodModelMutableLiveData;
    }
}
