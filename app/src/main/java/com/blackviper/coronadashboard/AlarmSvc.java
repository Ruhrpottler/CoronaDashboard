package com.blackviper.coronadashboard;

import android.content.Context;
import android.util.Log;

import java.util.Comparator;
import java.util.List;

import Comparator.LastUpdateComparator;
import Model.BaseData;
import Model.CoronaData;

/**
 * Warn the user in specific cases, for example via notification or toasts.
 */
public class AlarmSvc
{
    private final Context context;
    private NotificationSvc notificationSvc;

    private static final int THRESHOLD_INZIDENZ = 100; //Inzidenzwert, welcher für eine Warnung überschritten werden muss
    private static final int THRESHOLD_DAYS = 2; //Warn user if the Inzidenz-value is over the threshold more since or more than x days

    public AlarmSvc(Context context)
    {
        this.context = context;
        notificationSvc = new NotificationSvc(context);
    }

    //TODO Für eine Stadt von der DB alle Tage ziehen (List<CoronaData> und sortieren lassen)
    //TODO Dann die letzten Tage durchgehen und warnen
    public void warnUser(List<CoronaData> list, BaseData baseData)
    {
        if(list == null || list.isEmpty())
        {
            return;
        }
        Comparator<CoronaData> cmp = new LastUpdateComparator().reversed();
        int counter = 0;
        for(CoronaData data : list)
        {
            double inzidenzwert = data.getCases7_per_100k();
            if(inzidenzwert >= THRESHOLD_INZIDENZ)
            {
                counter++;
            }
        }
        if(counter > THRESHOLD_DAYS) //Wert seit x Tagen überschritten
        {
            sendWarningThresholdExceeded(counter, baseData);
        }
        else
        {
            Log.d("AlarmSvc", "No warning will be send to the user.");
        }
    }

    private void sendWarningThresholdExceeded(int sinceDays, BaseData baseData)
    {
        if(baseData == null || sinceDays <= 0)
        {
            return;
        }
        Log.d("AlarmSvc", "Send notification for city '" + baseData.getCityName() + "'.");
        String description = String.format("Der Inzidenzwert von %s %d wird bereits seit" +
                " mind.  %d Tagen überschritten.", THRESHOLD_INZIDENZ, baseData.getCityName(), sinceDays);
        notificationSvc.sendNotification("Inzidenzwert überschritten",
                description); //TODO Resoruces nutzen

    }
}
