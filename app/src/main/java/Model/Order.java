package Model;

import com.example.androideatit.Database.CartItem;

import java.util.List;

public class Order {
    private String userId, userName, userPhone, shippingAddress, comment, transactionID;
    private double lat, lng, totalPayment, finalPayment;
    private boolean cod;
    private int discount;

    private List<CartItem> cartItemList;

    public Order() {

    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setTotalPayment(double totalPayment) {
        this.totalPayment = totalPayment;
    }

    public void setFinalPayment(double finalPayment) {
        this.finalPayment = finalPayment;
    }

    public void setCod(boolean cod) {
        this.cod = cod;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public void setCartItemList(List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
    }
}
