package com.blackviper.coronadashboard.ResponseListener;

public interface FirebaseResponseListener
{
    void onResponse();

    void onError(String message);
}
