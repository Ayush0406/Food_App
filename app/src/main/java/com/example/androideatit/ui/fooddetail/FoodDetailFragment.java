package com.example.androideatit.ui.fooddetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.androideatit.Common.Common;
import com.example.androideatit.EventBus.FoodItemClick;
import com.example.androideatit.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import Model.AddonModel;
import Model.FoodModel;
import Model.SizeModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodDetailFragment extends Fragment implements TextWatcher {
    FoodDetailViewModel foodDetailViewModel;

    Unbinder unbinder;
    BottomSheetDialog addonBottomSheetDialog;

    ChipGroup chip_group_addon;
    EditText edt_search;

    @BindView(R.id.img_food)
    ImageView img_food;
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;
    @BindView(R.id.food_name)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.btnShowComment)
    Button btnShowComment;

    @BindView(R.id.rdi_group_size)
    RadioGroup rdi_group_size;
    @BindView(R.id.img_add_addon)
    ImageView img_add_on;
    @BindView(R.id.chip_group_user_selected_addon)
    ChipGroup chip_group_user_selected_addon;

    @OnClick(R.id.img_add_addon)
    void onAddonClick(){
        if(Common.selectedFood.getAddon() != null){
            displayAddonList(); //show all addon options
            addonBottomSheetDialog.show();
        }
    }

    private void displayAddonList() {
        if (Common.selectedFood.getAddon().size() > 0){
            chip_group_addon.clearCheck(); //clear check all views
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener(this);

            //add all views
            for(AddonModel addonModel: Common.selectedFood.getAddon()){

                    Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                    chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
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
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));

                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {
                    //remove when deleted
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                });

                chip_group_user_selected_addon.addView(chip);

            }
        }else if (Common.selectedFood.getUserSelectedAddon().size() == 0)
            chip_group_user_selected_addon.removeAllViews();
    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Common.selectedFood.getName());

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
            totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());


            displayPrice = totalPrice * (Integer.parseInt(numberButton.getNumber()));
            displayPrice = Math.round(displayPrice * 100.0/100.0);

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
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
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
}
