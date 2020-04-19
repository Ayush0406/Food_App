package com.example.androideatit.Common;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import Model.AddonModel;
import Model.CategoryModel;
import Model.FoodModel;
import Model.SizeModel;
import Model.User;

public class Common {
    public static CategoryModel categorySelected;
    public static User currentUser;
    public static FoodModel selectedFood;
    public static String Uid;
    public static final String ORDER_REF = "Orders";

    public static void setUid(String uid) {
        Uid = uid;
    }

    public static String getUid() {
        return Uid;
    }

    public static String formatPrice(double price) {
        if (price != 0){
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice  = new StringBuilder(df.format(price)).toString();
            return finalPrice;//.replace(".", ",");
        }
        else
            return "0.00";
    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        Double result = 0.0;
        if(userSelectedSize == null &&userSelectedAddon == null)
            return 0.0;

        else if (userSelectedSize == null){
            for(AddonModel addonModel : userSelectedAddon)
                result+=addonModel.getPrice();
            return result;
        }

        else if (userSelectedAddon == null){
            return userSelectedSize.getPrice()*1.0;
        }

        else{
            result = userSelectedSize.getPrice()*1.0;
            for(AddonModel addonModel : userSelectedAddon)
                result+=addonModel.getPrice();
            return result;

        }


    }

//    public static void setSpanStringColor(String order_date_, String format, TextView txt_time, int color)
    public static void setSpanStringColor(String welcome, String name, TextView textView, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus)
        {
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Error";
        }
    }
}
