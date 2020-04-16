package com.example.androideatit.ui.cart;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.androideatit.Common.Common;
import com.example.androideatit.Database.CartDataSource;
import com.example.androideatit.Database.CartDatabase;
import com.example.androideatit.Database.CartItem;
import com.example.androideatit.Database.LocalCartDataSource;

import java.util.List;
import java.util.function.Consumer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private MutableLiveData<List<CartItem>> mutableLiveDataCartItems;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CartDataSource cartDataSource;
    public CartViewModel() {

        compositeDisposable = new CompositeDisposable();

    }

    public void onStop(){
        compositeDisposable.clear();
    }

    public void initCartDataSource(Context context){
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItems() {
        if(mutableLiveDataCartItems == null)
            mutableLiveDataCartItems = new MutableLiveData<>();
        getAllCartItems();
        
        return mutableLiveDataCartItems;
    }

    private void getAllCartItems() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    mutableLiveDataCartItems.setValue(cartItems);
                }, throwable -> {
                    mutableLiveDataCartItems.setValue(null);
                }));
    }
}

