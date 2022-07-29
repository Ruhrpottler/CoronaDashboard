package com.blackviper.coronadashboard.ResponseListener;

public interface ObjectIdResponseListener
{
    void onResponse(int objectId);

    void onError(String message);
}
