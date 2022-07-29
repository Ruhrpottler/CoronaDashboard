package com.blackviper.coronadashboard.ResponseListener;

import java.util.List;

public interface ActvSetupResponseListener
{
    void onResponse(List<String> listOfEntries);

    void onError(String message);
}
