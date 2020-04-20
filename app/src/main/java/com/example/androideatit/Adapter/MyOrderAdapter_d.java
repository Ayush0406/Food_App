package com.example.androideatit.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androideatit.Common.Common;
import com.example.androideatit.R;

import java.text.SimpleDateFormat;
import java.util.List;

import Model.OrderModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter_d extends RecyclerView.Adapter<MyOrderAdapter_d.MyViewHolder> {

    Context context;
    List<OrderModel> orderModelList;
    SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter_d(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm::ss");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_item_d, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(orderModelList.get(position).getCartItemList().get(0).getFoodImage()).into(holder.img_food_image);
        holder.txt_order_number.setText(orderModelList.get(position).getKey());
        Common.setSpanStringColor("Order Date: ", simpleDateFormat.format(orderModelList.get(position).getCreateDate()), holder.txt_time, Color.parseColor("#333639"));
        Common.setSpanStringColor("Order status", Common.convertStatusToString_d(orderModelList.get(position).getOrderStatus()), holder.txt_order_status, Color.parseColor("#00579A"));
        Common.setSpanStringColor("Name: ", orderModelList.get(position).getUserName(), holder.txt_name, Color.parseColor("#00574B"));
        Common.setSpanStringColor("Number of Items: ", orderModelList.get(position).getCartItemList()==null?"0":String.valueOf(orderModelList.get(position).getCartItemList().size()), holder.txt_num_item, Color.parseColor("#4B647D"));

    }

    @Override
    public int getItemCount()
    {
        return orderModelList.size();
    }

    public OrderModel getItemAtPosition(int pos) {
        return orderModelList.get(pos);
    }

    public void removeItem(int pos) {
        orderModelList.remove(pos);
    }

    public void updateItemStatusAtPosition(int pos, int i) {
        orderModelList.get(pos).setOrderStatus(i);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.img_food_image_delivery_d)
        ImageView img_food_image;
        @BindView(R.id.txt_name_d)
        TextView txt_name;
        @BindView(R.id.txt_time_d)
        TextView txt_time;
        @BindView(R.id.txt_order_number_d)
        TextView txt_order_number;
        @BindView(R.id.txt_order_status_d)
        TextView txt_order_status;
        @BindView(R.id.txt_num_item_d)
        TextView txt_num_item;

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
