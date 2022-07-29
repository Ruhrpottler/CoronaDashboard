package com.blackviper.coronadashboard.ResponseListener;

import java.util.List;

import Model.City;

public interface CityListResponseListener
{
    void onResponse(List<City> cities);

    void onError(String message);
}
