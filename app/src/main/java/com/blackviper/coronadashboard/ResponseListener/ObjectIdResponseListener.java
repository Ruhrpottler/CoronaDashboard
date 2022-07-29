package com.blackviper.coronadashboard.ResponseListener;

public interface ObjectIdResponseListener
{
    void onError(String message);

    void onResponse(int objectId);
}
