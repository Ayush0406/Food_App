package com.example.androideatit.Common;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import Model.AddonModel;
import Model.CategoryModel;
import Model.FoodModel;
import Model.SizeModel;
import Model.User;

public class Common {
    public static final String ORDER_REF = "Orders";
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

    public static String createOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis()) //current time in milliseconds
                .append(Math.abs(new Random().nextInt())) //random number to block same order at same time
                .toString();
    }

    public static String getDateofWeek(int i) {
        switch (i)
        {
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            case 7:
                return "Sunday";
            default:
                return "unknown";
        }
    }

    public static String convertStatusToText(int orderStatus) {
        switch (orderStatus)
        {
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case 3:
                return "Cancelled";
            default:
                return "unknown";
        }
    }
}
