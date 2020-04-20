package com.example.androideatit.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.strictmode.CleartextNetworkViolation;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.androideatit.R;
import com.example.androideatit.services.MyFCMServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import Model.AddonModel;
import Model.CategoryModel;
import Model.DeliveryPerson;
import Model.FoodModel;
import Model.SizeModel;
import Model.TokenModel;
import Model.User;

public class Common {
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "content";
    private static final String TOKEN_REF = "Tokens";
    public static CategoryModel categorySelected;
    public static User currentUser;
    public static FoodModel selectedFood;
    public static String Uid;
    public static DeliveryPerson currentDelivery;


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

    public static String convertStatusToString_d(int orderStatus) {
        switch (orderStatus)
        {
            case 0:
                return "Pending";
            case 1:
                return "Accepted";
            case 2:
                return "Fulfilled";
            case -1:
                return "Cancelled";
            default:
                return "Error";
        }
    }

    public static void setSpanString(String welcome, String name, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldspan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldspan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "oop_eats_pilani";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "FasterChef", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("FasterChef");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title).setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_restaurant_menu_black_24dp));
        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    public static void updateToken(Context context, String newToken) {
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.getUid())
                .setValue(new TokenModel(Common.currentUser.getPhone(), newToken))
                .addOnFailureListener(e -> {
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
