package com.example.androideatit.Adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androideatit.Common.Common;
import com.example.androideatit.Database.CartItem;
import com.example.androideatit.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import Model.Order;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static java.lang.Math.round;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.MyViewHolder> {

    private Context context;
    private List<Order> orderList;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;

    public MyOrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_order_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(orderList.get(position).getCartItemList().get(0).getFoodImage())
                .into(holder.img_order); //load default image in cart
        calendar.setTimeInMillis(orderList.get(position).getCreateDate());
        Date date = new Date(orderList.get(position).getCreateDate());
        holder.txt_order_date.setText(new StringBuilder(Common.getDateofWeek(calendar.get(Calendar.DAY_OF_WEEK)))
        .append(" ")
        .append(simpleDateFormat.format(date)));
        holder.txt_order_number.setText(new StringBuilder("Order Number: ")
        .append(orderList.get(position).getOrderNumber()));
        if(orderList.get(position).getFinalPayment()==orderList.get(position).getTotalPayment())
            holder.txt_order_price.setText(new StringBuilder("Price (excluding delivery): Rs. ").append(round(orderList.get(position).getTotalPayment())));
        else
            holder.txt_order_price.setText(new StringBuilder("Price (including delivery): Rs. ").append(round(orderList.get(position).getFinalPayment())));
        holder.txt_order_status.setText(new StringBuilder("Status: ").append(Common.convertStatusToText(orderList.get(position).getOrderStatus())));
        List<CartItem> cartItemList = orderList.get(position).getCartItemList();
        StringBuilder orderItems = new StringBuilder("Order Items: ");
        for(CartItem item : cartItemList)
        {
            orderItems.append(item.getFoodName()).append(", ");
        }
        orderItems.delete(orderItems.length()-2, orderItems.length());
//        Common.setSpanStringColor("Name: ", new String(orderItems), holder.txt_order_items, Color.parseColor("#02330c"));
        holder.txt_order_items.setText(orderItems);
        if(!orderList.get(position).getDeliveryNumber().equals(""))
        {
            holder.btnCallDelivery.setVisibility(View.VISIBLE);
        }
        holder.btnCallDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Call del", "calling");
                Dexter.withContext(context).withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent callIntent = new Intent(Intent.ACTION_DIAL); //use ACTION_CALL class
                                callIntent.setData(Uri.parse(new StringBuilder("tel:").append(orderList.get(position).getDeliveryNumber()).toString()));    //this is the phone number calling
                                try{
                                    context.startActivity(callIntent);  //call activity and make phone call
                                }
                                catch (android.content.ActivityNotFoundException ex){
                                    Toast.makeText(context,"yourActivity is not founded",Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(context,"Please grant call permission", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            }
                        }).check();


            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_order_price)
        TextView txt_order_price;
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_order_items)
        TextView txt_order_items;
        @BindView(R.id.img_order)
        ImageView img_order;
        @BindView(R.id.btnCallDelivery)
        Button btnCallDelivery;

        Unbinder unbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
