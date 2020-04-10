package com.example.androideatit.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.androideatit.Callback.IBestDealCallbackListener;
import com.example.androideatit.Callback.IPopularCallbackListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.BestDealModel;
import Model.PopularCategoryModel;

public class HomeViewModel extends ViewModel implements IPopularCallbackListener, IBestDealCallbackListener {

    private MutableLiveData<List<PopularCategoryModel>> popularList;
    private MutableLiveData<List<BestDealModel>> bestDealList;
    private MutableLiveData<String> messageError;
    private IPopularCallbackListener popularCallbackListener;
    private IBestDealCallbackListener bestDealCallbackListener;

    public HomeViewModel() {
        popularCallbackListener = this;
        bestDealCallbackListener = this;
    }

    public MutableLiveData<List<BestDealModel>> getBestDealList() {
        if(bestDealList == null)
        {
            bestDealList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadBestDealList();
        }
        return bestDealList;
    }

    private void loadBestDealList() {
        List<BestDealModel> tempList = new ArrayList<>();
        DatabaseReference bestDealRef = FirebaseDatabase.getInstance().getReference("BestDeals");
        bestDealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapShot: dataSnapshot.getChildren())
                {
                    tempList.add(itemSnapShot.getValue(BestDealModel.class));
                }
                bestDealCallbackListener.onBestDealLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                bestDealCallbackListener.onBestDealLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<List<PopularCategoryModel>> getPopularList() {
        if(popularList == null) //then we will load list from our database.
        {
            popularList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadPopularList(); //function to load list from database
        }
        return popularList;
    }

    private void loadPopularList() {
        final List<PopularCategoryModel> tempList = new ArrayList<>();
        DatabaseReference popularRef = FirebaseDatabase.getInstance().getReference("MostPopular");
        popularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapShot: dataSnapshot.getChildren())
                {
                    PopularCategoryModel model = itemSnapShot.getValue(PopularCategoryModel.class);
                    tempList.add(model);
                }
                popularCallbackListener.onPopularLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                popularCallbackListener.onPopularLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels) {
        popularList.setValue(popularCategoryModels);
    }

    @Override
    public void onPopularLoadFailed(String message) {
        messageError.setValue(message)  ;
    }

    @Override
    public void onBestDealLoadSuccess(List<BestDealModel> bestDealModels) {
        bestDealList.setValue(bestDealModels);
    }

    @Override
    public void onBestDealLoadFailed(String message) {
        messageError.setValue(message);
    }
}