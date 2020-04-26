package com.example.androideatit.EventBus;

public class CounterCartEvent {
    private boolean success;

    public CounterCartEvent(boolean sources){
        this.success = sources;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
