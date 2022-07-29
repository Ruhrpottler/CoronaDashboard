package com.blackviper.coronadashboard.ResponseListener;

import Model.City;

public interface CityResponseListener
{
    void onResponse(City city);

    void onError(String message);
}
