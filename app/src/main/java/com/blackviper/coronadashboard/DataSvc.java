package com.blackviper.coronadashboard;

import android.content.Context;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.blackviper.coronadashboard.ResponseListener.ActvSetupResponseListener;
import com.blackviper.coronadashboard.ResponseListener.BaseDataListResponseListener;
import com.blackviper.coronadashboard.ResponseListener.CityListResponseListener;
import com.blackviper.coronadashboard.ResponseListener.CityResponseListener;
import com.blackviper.coronadashboard.ResponseListener.FirebaseResponseListener;
import com.blackviper.coronadashboard.ResponseListener.ObjectIdResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Database.FirebaseSvc;
import Database.SQLiteDatabaseHelper;
import Model.BaseData;
import Model.City;

/**
 * This class handles the requests and responses to the ArcGis/RKI API (COVID-19-Datenhub).
 */
public class DataSvc
{
    //Klassenattribute
    private final MainActivity activity;
    private final Context context;
    private final SQLiteDatabaseHelper dbHelper;
    private final FirebaseSvc firebaseSvc = FirebaseSvc.getFirebaseInstance();
    private final AlarmSvc alarmSvc;
    private int objectId;

    //Konstuktoren
    public DataSvc(MainActivity activity, Context context)
    {
        this.activity = activity;
        this.context = context;
        this.dbHelper = new SQLiteDatabaseHelper(context);
        this.alarmSvc = new AlarmSvc(context);
    }

    //Methoden

    /**
     * Fragt die objectId der City über die API ab und speichert sie im Attribut
     *
     * @param cityName Vollständiger Name von Landkreis oder kreisfreie Stadt
     */
    public void findAndSetObjectIdByName(String cityName, ObjectIdResponseListener responseListener)
    {
        String url = UrlManager.getUrlFindObjectIdByCityName(cityName);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                disableOfflineMode();
                try
                {
                    JSONArray objectIDsArray = response.getJSONArray("objectIds");

                    if (objectIDsArray.length() == 0)
                    {
                        throw new IllegalArgumentException(String.format("'%s' konnte nicht gefunden werden.", cityName));
                    }
                    else if (objectIDsArray.length() > 1)
                    {
                        throw new IllegalArgumentException(String.format("Es wurden mehrere Ergebnisse für '%s' gefunden.", cityName));
                    }

                    objectId = objectIDsArray.getInt(0);

                    if (objectId == 0)
                    {
                        throw new IllegalArgumentException(String.format("Für '%s' wurde die ungültige Objekt-ID %d gefunden.", cityName, objectId));
                    }
                    responseListener.onResponse(objectId);
                } catch (JSONException e)
                {
                    e.printStackTrace();
                    Log.d("JSONException", e.getMessage());
                } catch (IllegalArgumentException userException)
                {
                    activity.uiUtility.showToastTextLong("Fehler: " + userException.getMessage());
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                String msg;
                if(error instanceof NoConnectionError)
                {
                    enableOfflineMode();
                    msg = "Verbindung zum Host gescheitert. Es wird versucht, die Daten offline zu finden.";
                    Log.d("DataSvc", msg);
                    firebaseSvc.getObjectIdByName(cityName, responseListener);
                }
                else
                {
                    disableOfflineMode();
                    msg = "Fehler bei der Verarbeitung der Server-Antwort";
                    Log.d("DataSvc", msg + ": " + error.toString());
                    responseListener.onError(msg + ".");
                }
                activity.uiUtility.showToastTextLong(msg);
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    public void getAllCityData(CityListResponseListener responseListener)
    {
        String url = UrlManager.getUrlGetAllCityData();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                List<City> list = new ArrayList<>();
                try
                {
                    list = JsonSvc.getCityListFromResponse(response);
                }
                catch(JSONException | IndexOutOfBoundsException e)
                {
                    String msg = "Loading city-data from all cities and extracting to list failed: "
                            + e.getMessage();
                    activity.uiUtility.showToastTextLong(msg);
                    responseListener.onError(msg);
                    return;
                }
                responseListener.onResponse(list);
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if(error instanceof NoConnectionError)
                {
                    enableOfflineMode();
                    responseListener.onError("Verbindung zum Host gescheitert.");
                }
                else
                {
                    disableOfflineMode();
                    responseListener.onError(error.getMessage());
                }
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * Fragt die Daten (Base und Corona) über das RKI ab (REST-Schnittstelle).
     * @param objectId eindeutige ID der Kommune
     * @param responseListener Rückgabe über Listener (callback)
     */
    public void getCityByObjectId(int objectId, CityResponseListener responseListener)
    {
        String url = UrlManager.getUrlGetCityByObjectId(objectId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                disableOfflineMode();
                try
                {
                    JSONArray features = JsonSvc.getJSONFeaturesFromResponse(response);
                    JSONObject attributes = JsonSvc.getJSONAttributesFromFeatures(features, 0);
                    City city = JsonSvc.createAndFillCity(attributes);
                    responseListener.onResponse(city);
                }
                catch (JSONException jsonException)
                {
                    String msg = "Error processing JSON-response.";
                    responseListener.onError(msg);
                    Log.e("DataSvc", msg + "\n" + jsonException.getMessage());
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if(error instanceof NoConnectionError) //Wenn kein Internet, auf die Firebase DB zugreifen.
                {
                    enableOfflineMode();
                    firebaseSvc.getCity(objectId, responseListener); //async
                    return;
                }
                else
                {
                    disableOfflineMode();
                }
                String msg = "Fehler bei der Verarbeitung der Server-Antwort. Host nicht erreichbar.";
                Log.d("DataSvc", msg + "\n" + error.toString());
                responseListener.onError(msg);
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

//    /**
//     * Fragt die Daten (Base und Corona) über das RKI ab (REST-Schnittstelle).
//     * @param objectId eindeutige ID der Kommune
//     * @param responseListener Rückgabe über Listener (callback)
//     */
//    public void getCityByObjectId(int objectId, CityResponseListener responseListener)
//    {
//        String url = UrlManager.getUrlGetCityByObjectId(objectId);
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
//        {
//            @Override
//            public void onResponse(JSONObject response)
//            {
//                disableOfflineMode();
//                try
//                {
//                    JSONArray features = JsonSvc.getJSONFeaturesFromResponse(response);
//                    JSONObject attributes = JsonSvc.getJSONAttributesFromFeatures(features, 0);
//                    City city = JsonSvc.createAndFillCity(attributes);
//                    responseListener.onResponse(city);
//                }
//                catch (JSONException jsonException)
//                {
//                    String msg = "Error processing JSON-response.";
//                    responseListener.onError(msg);
//                    Log.e("DataSvc", msg + "\n" + jsonException.getMessage());
//                }
//            }
//        }, new Response.ErrorListener()
//        {
//            @Override
//            public void onErrorResponse(VolleyError error)
//            {
//                if(error instanceof NoConnectionError) //Wenn kein Internet, auf die Firebase DB zugreifen.
//                {
//                    enableOfflineMode();
//                    firebaseSvc.getCity(objectId, responseListener); //async
//                    return;
//                }
//                else
//                {
//                    disableOfflineMode();
//                }
//                String msg = "Fehler bei der Verarbeitung der Server-Antwort. Host nicht erreichbar.";
//                Log.d("DataSvc", msg + "\n" + error.toString());
//                responseListener.onError(msg);
//            }
//        });
//        RequestSingleton.getInstance(context).addToRequestQueue(request);
//    }

    /**
     * Callback-Hell
     * Holt das City-Objekt anhand des Namen
     *
     * @param cityName              ohne Präfix
     * @param cityResponseListener Wird aufgerufen, wenn Response vom Server da ist
     */
    public void getCityDataByName(String cityName, CityResponseListener cityResponseListener)
    {
        findAndSetObjectIdByName(cityName, new ObjectIdResponseListener()
        {
            @Override
            public void onResponse(int objectId)
            {
                getCityByObjectId(objectId, new CityResponseListener()
                {
                    @Override
                    public void onResponse(City city)
                    {
                        cityResponseListener.onResponse(city);
                        alarmSvc.warnUser(city);
                    }

                    @Override
                    public void onError(String message)
                    {
                        activity.uiUtility.showToastTextLong(message);
                        Log.e("DataSvc", message);
                    }
                });
            }

            @Override
            public void onError(String message)
            {
                activity.uiUtility.showToastTextLong(message);
                Log.e("DataSvc", message);
            }
        });
    }

    /**
     * Get list with all Cities in Germany
     */
    public void getAllBaseData(BaseDataListResponseListener responseListener)
    {
        String url = UrlManager.getUrlGetAllBaseData();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                disableOfflineMode();
                try
                {
                    List<BaseData> list = JsonSvc.getBaseDataListFromResponse(response);
                    responseListener.onResponse(list);
                } catch (JSONException jsonExceptione)
                {
                    responseListener.onError(jsonExceptione.getMessage());
                    Log.d("DataSvc", jsonExceptione.toString());
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if(error instanceof NoConnectionError)
                {
                    enableOfflineMode();
                    firebaseSvc.getAllBaseData(responseListener);
                    return;
                }
                else
                {
                    disableOfflineMode();
                }

                String msg = "Fehler bei der Verarbeitung der Server-Antwort: " + error.toString();
                Log.e("onErrorResponse", msg);
                responseListener.onError(msg);
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);

        //TODO move to other point
        getAllCityData(new CityListResponseListener()
        {
            @Override
            public void onResponse(List<City> cities)
            {
                if(cities.isEmpty())
                {
                    String msg = "Cities is empty";
                    activity.uiUtility.showToastTextShort(msg);
                    throw new IllegalArgumentException(msg);
                }
                firebaseSvc.saveCityList(cities, new FirebaseResponseListener()
                {
                    @Override
                    public void onResponse()
                    {
                        activity.uiUtility.showToastTextShort("Daten aller Städte werden gespeichert.");
                        //Hinweis: Dass sie wirklich gespeichert wurden, ist nicht klar. Es wurde nur die
                        //Liste abgearbeitet und Firebase wurden die Tasks übergeben
                    }

                    @Override
                    public void onError(String message)
                    {
                        Log.e("DataSvc", "Saving all cities to the database failed: " + message);
                        activity.uiUtility.showToastTextShort("Fehler beim Speicher aller Städte-Daten.");
                    }
                });
            }

            @Override
            public void onError(String message)
            {
                Log.e("DataSvc", "Request loading all cities failed: " + message);
                activity.uiUtility.showToastTextShort("Fehler beim Speicher aller Städte-Daten.");
            }
        });

    }

    private List<String> fillCityNameList(List<BaseData> baseDataList)
    {
        List<String> cityNameList = new ArrayList<String>();
        for(BaseData baseData : baseDataList)
        {
            if (baseData == null)
            {
                continue;
            }
            cityNameList.add(baseData.getCityName());
        }
        return cityNameList;
    }

    public void fillActvCity(AutoCompleteTextView actv_city, Context activityContext, ActvSetupResponseListener responseListener)
    {
        getAllBaseData(new BaseDataListResponseListener()
        {
            @Override
            public void onError(String message)
            {
                Log.e("DataSvc", message);
                responseListener.onError(message);
            }

            @Override
            public void onResponse(List<BaseData> list)
            {
                List<String> cityNameList = fillCityNameList(list);
                firebaseSvc.saveBaseDataList(list);
                responseListener.onResponse(cityNameList);
            }
        });
    }

    private void enableOfflineMode()
    {
        activity.enableOfflineMode();
    }

    private void disableOfflineMode()
    {
        activity.disableOfflineMode();
    }
}
