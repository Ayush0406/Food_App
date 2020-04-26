package com.example.androideatit.ui.fooddetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.androideatit.Common.Common;
import com.example.androideatit.Database.CartDataSource;
import com.example.androideatit.Database.CartDatabase;
import com.example.androideatit.Database.CartItem;
import com.example.androideatit.Database.LocalCartDataSource;
import com.example.androideatit.EventBus.CounterCartEvent;
import com.example.androideatit.EventBus.FoodItemClick;
import com.example.androideatit.EventBus.MenuItemBack;
import com.example.androideatit.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import Model.AddonModel;
import Model.FoodModel;
import Model.SizeModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment implements TextWatcher {
    FoodDetailViewModel foodDetailViewModel;

    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    Unbinder unbinder;
    BottomSheetDialog addonBottomSheetDialog;
    ChipGroup chip_group_addon;
    EditText edt_search;
    Double discount = 1.0;
    @BindView(R.id.img_food)
    ImageView img_food;
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @BindView(R.id.food_name)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.food_price_up)
    TextView food_price_up;
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
//    @BindView(R.id.ratingBar)
//    RatingBar ratingBar;
//    @BindView(R.id.btnShowComment)
//    Button btnShowComment;

    @BindView(R.id.rdi_group_size)
    RadioGroup rdi_group_size;
    @BindView(R.id.img_add_addon)
    ImageView img_add_on;
    @BindView(R.id.chip_group_user_selected_addon)
    ChipGroup chip_group_user_selected_addon;

    @OnClick(R.id.img_add_addon)
    void onAddonClick(){
//        FirebaseDatabase.getInstance().getReference("category").child(Common.currentUser.get)
        if(Common.selectedFood.getAddon() != null){
            displayAddonList(); //show all addon options
            addonBottomSheetDialog.show();
        }

//        else{
//            List
//            Common.selectedFood.setAddon(List<AddonModel> addon);
//        }
    }

    @OnClick(R.id.btnCart)
    void onCartItemAdd(){
        CartItem cartItem = new CartItem();
        cartItem.setUid(Common.getUid());
        cartItem.setFoodId(Common.selectedFood.getId());
        cartItem.setFoodName(Common.selectedFood.getName());
        cartItem.setFoodImage(Common.selectedFood.getImage());
        cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
        cartItem.setFoodQuantity(Integer.valueOf(numberButton.getNumber()));
        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(), Common.selectedFood.getUserSelectedAddon())); // default setting
        if(Common.selectedFood.getUserSelectedSize() != null)
            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
        else
            cartItem.setFoodSize("Default");

        if(Common.selectedFood.getUserSelectedAddon() != null)
            cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
        else
            cartItem.setFoodAddon("Default");

        cartDataSource.getItemWithAllOptionsInCart(Common.getUid(),
                cartItem.getFoodId(),
                cartItem.getFoodSize(),
                cartItem.getFoodAddon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CartItem cartItemFromDB) {
                        if(cartItemFromDB.equals(cartItem)){
                            //only update needed
                            cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                            cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                            cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                            cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity() + cartItem.getFoodQuantity());

                            cartDataSource.updateCartItems(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            Toast.makeText(getContext(), "Cart Updated", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        else{
                            // item needs to be inserted
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Added to Cart", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "CART ERROR" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("empty")){
                            // Code for when cart is empty
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Added to Cart", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "CART ERROR" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }
                        else
                            Toast.makeText(getContext(), "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void displayAddonList() {
        if (Common.selectedFood.getAddon().size() > 0){
            chip_group_addon.clearCheck(); //clear check all views
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener(this);

            //add all views
            for(AddonModel addonModel: Common.selectedFood.getAddon()){

                    Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                    chip.setText(new StringBuilder(addonModel.getName()).append("(+\u20B9")
                            .append(addonModel.getPrice()).append(")"));
                    chip.setOnCheckedChangeListener(((compoundButton, b) -> {
                        if(b){
                            if(Common.selectedFood.getUserSelectedAddon() == null)
                                Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                            Common.selectedFood.getUserSelectedAddon().add(addonModel);
                        }
                    }));

                    chip_group_addon.addView(chip);

            }

        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        foodDetailViewModel = ViewModelProviders.of(this).get(FoodDetailViewModel.class);

        View root =  inflater.inflate(R.layout.fragment_food_detail, container, false);
        unbinder = ButterKnife.bind(this, root);
        initViews();
        foodDetailViewModel.getFoodModelMutableLiveData().observe(this, new Observer<FoodModel>() {
            @Override
            public void onChanged(FoodModel foodModel) {
                displayInfo(foodModel);
            }
        });
        return root;
    }

    private void initViews() {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        addonBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.DialogStyle);
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
        chip_group_addon = (ChipGroup)layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText)layout_addon_display.findViewById(R.id.edt_search);
        addonBottomSheetDialog.setContentView(layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                displayUserSelectedAddon();
                calculateTotalPrice();
            }
        });


    }

    private void displayUserSelectedAddon() {
        if (Common.selectedFood.getUserSelectedAddon() != null &&
        Common.selectedFood.getUserSelectedAddon().size() > 0) {
            chip_group_user_selected_addon.removeAllViews(); // clear all view already added
            for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon()) // add all avail addons to list
            {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+\u20B9")
                        .append(addonModel.getPrice()).append(")"));

                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {
                    //remove when deleted
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });

                chip_group_user_selected_addon.addView(chip);

            }
        }else
            chip_group_user_selected_addon.removeAllViews();
    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));
        food_price_up.setText(new StringBuilder(foodModel.getPrice().toString()));

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Common.selectedFood.getName());

        numberButton.setOnClickListener(new ElegantNumberButton.OnClickListener() {
            @Override // update price
            public void onClick(View view) {
                calculateTotalPrice();
            }
        });

        //size

        for(SizeModel sizeModel: Common.selectedFood.getSize()){

            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener((compoundButton, b) -> {

                if(b)
                    Common.selectedFood.setUserSelectedSize(sizeModel);
                calculateTotalPrice(); // Update price

            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f);

                radioButton.setLayoutParams(params);
                radioButton.setText(sizeModel.getName());
                radioButton.setTag(sizeModel.getPrice());

                rdi_group_size.addView(radioButton);


        }

        if (rdi_group_size.getChildCount() > 0){
            RadioButton radioButton = (RadioButton)rdi_group_size.getChildAt(0);
            radioButton.setChecked(true); // default first selected
        }

        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
            double totalPrice = Double.parseDouble(Common.selectedFood.getPrice().toString()), displayPrice = 0.0;
            //Addon
            if(Common.selectedFood.getUserSelectedAddon() != null && Common.selectedFood.getUserSelectedAddon().size()>0){
                for(AddonModel addonModel : Common.selectedFood.getUserSelectedAddon()){
                    totalPrice+= Double.parseDouble(addonModel.getPrice().toString());
                }
            }

            //Size
            if(Common.selectedFood.getUserSelectedSize() != null)
                totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());

            int num = Integer.parseInt(numberButton.getNumber());

            displayPrice = totalPrice * (num);
            displayPrice = Math.round(displayPrice * 100.0/100.0);

            food_price_up.setText(new StringBuilder("").append(Common.formatPrice(displayPrice/num)).toString());

            int count = Integer.parseInt(Common.currentUser.getCount());

            if (count >= 5 && count < 10) {
                discount = 0.95;
                displayPrice = discount * displayPrice;
                food_price_up.setText(new StringBuilder("")
                        .append(Common.formatPrice(displayPrice/num)
                                +"           - 5% Discount Applied").toString());
            }
                else if (count >= 10) {
                discount = 0.9;
                displayPrice = discount * displayPrice;
                food_price_up.setText(new StringBuilder("")
                        .append(Common.formatPrice(displayPrice/num)
                                +"           - 10% Discount Applied").toString());
            }
                else{
                food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());
            }

            food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();

        for(AddonModel addonModel: Common.selectedFood.getAddon()){
            if(addonModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase()))
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+\u20B9")
                .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener(((compoundButton, b) -> {
                    if(b){
                        if(Common.selectedFood.getUserSelectedAddon() == null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                }));

                chip_group_addon.addView(chip);

            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        //nothing
    }

    public void onStop(){
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
//        FragmentManager manager = getFragmentManager();
//        Log.d("back stack", String.valueOf(manager.getBackStackEntryCount()));
//        if(manager.getBackStackEntryCount() > 0)
//        {
//            manager.popBackStack();
//        }
//        manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

}

