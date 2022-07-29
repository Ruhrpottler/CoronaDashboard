package com.blackviper.coronadashboard.ResponseListener;

import java.util.List;

import Model.BaseData;

public interface BaseDataListResponseListener
{
    void onError(String message);

    void onResponse(List<BaseData> list);
}
