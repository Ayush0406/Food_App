package com.example.androideatit.Common;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import Model.CategoryModel;
import Model.FoodModel;
import Model.User;

public class Common {
    public static CategoryModel categorySelected;
    public static User currentUser;
    public static FoodModel selectedFood;
    public static String Uid;

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
            return finalPrice.replace(".", ",");
        }
        else
            return "0,00";
    }
}
