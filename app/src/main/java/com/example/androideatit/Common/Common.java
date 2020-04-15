package com.example.androideatit.Common;

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

}
