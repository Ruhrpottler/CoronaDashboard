package com.blackviper.coronadashboard;

import android.content.Context;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Database.FirebaseSvc;
import Database.SQLiteDatabaseHelper;
import Model.BaseData;
import Model.City;
import Model.CoronaData;
import Model.Data;

/**
 * Diese Klasse stellt asynchrone Methoden (Callbacks) zur Verfügung, welche den Traffic mit den Anfragen an die API
 * verwaltet und z.B. die ID für eine Stadt, ein Bundesland oder die Strings zurück gibt.
 */
public class DataSvc
{
    //Klassenattribute
    private final MainActivity activity;
    private final Context context;
    private final SQLiteDatabaseHelper dbHelper;
    private final FirebaseSvc firebaseSvc = FirebaseSvc.getFirebaseInstance();
    private int objectId;

    //Konstuktoren
    public DataSvc(MainActivity activity, Context context)
    {
        this.activity = activity;
        this.context = context;
        this.dbHelper = new SQLiteDatabaseHelper(context);
    }

    /**
     * Callback-Methoden, welche in der Activity implementiert werden müssen
     */
    public interface ObjectIdResponseListener
    {
        void onError(String message);

        void onResponse(int objectId);
    }

    //Methoden

    /**
     * Fragt die objectId der City über die API ab und speichert sie im Attribut
     *
     * @param cityName Vollständiger Name von Landkreis oder kreisfreie Stadt
     */
    public void findAndSetObjectIdByName(String cityName, ObjectIdResponseListener responseListener)
    {
        String url = UrlManager.getUrlSearchObjectIdByCityName(cityName);

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
        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    public interface CityResponseListener
    {
        void onResponse(City city);

        void onError(String message);
    }

    public interface CityListResponseListener
    {
        void onResponse(List<City> cities);

        void onError(String message);
    }

    public void getAllCityData(CityListResponseListener responseListener)
    {
        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?where=1%3D1&outFields=OBJECTID,BEZ,GEN,EWZ,BL_ID,BL,last_update,death_rate,cases,deaths,cases_per_100k,cases_per_population,cases7_per_100k,cases7_lk,death7_lk,death7_lk,cases7_bl_per_100k,cases7_bl,death7_bl&returnGeometry=false&outSR=&f=json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                List<City> list = new ArrayList<>();
                try
                {
                    list = getCityListFromResponse(response);
                }
                catch(JSONException jsonException)
                {
                    activity.uiUtility.showToastTextLong("JSON exception");
                }
                catch(IndexOutOfBoundsException outOfBoundsException)
                {
                    activity.uiUtility.showToastTextLong("IndexOutOfBoundsException");
                }
                responseListener.onResponse(list);
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //TODO
                if(error instanceof NoConnectionError)
                {
                    enableOfflineMode();
                    //firebaseSvc.getCity(objectId, responseListener); //async
                    return;
                }
                else
                {
                    disableOfflineMode();
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
        //TODO url
        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?"
                + "where=OBJECTID%3D" + objectId
                + "&outFields=OBJECTID,BEZ,GEN,EWZ,BL_ID,BL,last_update,death_rate,cases,deaths,cases_per_100k,cases_per_population,"
                + "cases7_per_100k,cases7_lk,death7_lk,death7_lk,cases7_bl_per_100k,cases7_bl,death7_bl&returnGeometry=false&f=json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                disableOfflineMode();
                try
                {
                    JSONArray features = getJSONFeaturesFromResponse(response);
                    JSONObject attributes = getJSONAttributesFromFeatures(features, 0);
                    City city = createAndFillCity(attributes);
                    responseListener.onResponse(city);
                }
                catch (JSONException jsonException)
                {
                    responseListener.onError(jsonException.getMessage());
                    Log.e("DataSvc", jsonException.toString());
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

                //TODO Exception Handling verbessern: Nicht zu viele Details geben. Nur sowas wie "Host unavailable".
                String msg = "Fehler bei der Verarbeitung der Server-Antwort: " + error.toString();
                Log.d("onErrorResponse", msg);
                responseListener.onError(msg);
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

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
            public void onError(String message)
            {
                activity.uiUtility.showToastTextLong(message);
                Log.e("DataSvc", message);
            }

            @Override
            public void onResponse(int objectId)
            {
                //objectId anhand vom City-name finden
                getCityByObjectId(objectId, new CityResponseListener()
                {
                    @Override
                    public void onResponse(City city)
                    {
                        cityResponseListener.onResponse(city);
                    }

                    @Override
                    public void onError(String message)
                    {
                        activity.uiUtility.showToastTextLong(message);
                        Log.e("DataSvc", message);
                    }
                });
            }
        });
    }

    public interface BaseDataResponseListener //Kann man evtl. weglassen und stattdessen den list-Listener nutzen
    {
        void onError(String message);

        void onResponse(BaseData baseData);
    }

    public interface BaseDataListResponseListener
    {
        void onError(String message);

        void onResponse(List<BaseData> list);
    }

    private JSONArray getJSONFeaturesFromResponse(JSONObject response) throws JSONException
    {
        return response.getJSONArray("features");
    }

    private JSONObject getJSONAttributesFromFeatures(JSONArray features, int index) throws JSONException
    {
        return features.getJSONObject(index).getJSONObject("attributes");
    }

    private List<City> getCityListFromResponse(JSONObject response) throws JSONException
    {
        List<City> list = new ArrayList<>();

        JSONArray features = getJSONFeaturesFromResponse(response);
        JSONObject attributes;
        City city;
        for(int i = 0; i < features.length(); i++)
        {
            attributes = getJSONAttributesFromFeatures(features, i);
            city = createAndFillCity(attributes);
            if(city != null)
            {
                list.add(city);
            }
        }
        return list;
    }

    private City createAndFillCity(JSONObject attributes) throws JSONException
    {
        BaseData baseData = BaseData.createDataFromJSONAttributes(attributes);
        CoronaData coronaData = CoronaData.createDataFromJSONAttributes(attributes);
        return new City(baseData, coronaData);
    }

    private List<BaseData> getBaseDataListFromResponse(JSONObject response) throws JSONException
    {
        return getListFromResponse(response, new BaseData());
    }

    private List<CoronaData> getCoronaDataListFromResponse(JSONObject response) throws JSONException
    {
        return getListFromResponse(response, new CoronaData());
    }

    private <T extends Data> List<T> getListFromResponse(JSONObject response, T t) throws JSONException
    {
        JSONArray features = getJSONFeaturesFromResponse(response);
        JSONObject attributes;
        List<T> list = new ArrayList<>();

        for (int i = 0; i < features.length(); i++)
        {
            attributes = getJSONAttributesFromFeatures(features, i);
            list.add((T) t.createDataFromJSONAttributesGeneric(attributes));
        }

        return list;
    }

    /**
     * Get list with all Cities in Germany
     */
    public void getAllBaseData(BaseDataListResponseListener responseListener)
    {
        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/"
                + "query?where=1%3D1&outFields=OBJECTID,BL_ID, BL, GEN,BEZ,EWZ&returnGeometry=false&outSR=&f=json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                disableOfflineMode();
                try
                {
                    List<BaseData> list = getBaseDataListFromResponse(response);
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

        //Test //TODO move to other point
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
                        activity.uiUtility.showToastTextShort("Daten aller Städte wurden gespeichert.");
                        //Hinweis: Dass sie wirklich gespeichert wurden, ist nicht klar. Es wurde nur die
                        //Liste abgearbeitet und Firebase wurden die Tasks übergeben
                    }

                    @Override
                    public void onError(String message)
                    {
                        activity.uiUtility.showToastTextShort("Fehler beim Speicher aller Städte-Daten.");
                    }
                });
            }

            @Override
            public void onError(String message)
            {
                //TODO
            }
        });

    }

    public interface FirebaseResponseListener //TODO rename or outsource to firebaseSvc
    {
        void onResponse();

        void onError(String message);
    }

    public interface ActvSetupResponseListener
    {
        void onResponse(List<String> listOfEntries);

        void onError(String message);
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

    //TODO auslagern in SQLite, aber doppelte for-Schleife verhindern / Methodennamen ändern
    private void fillListOfEntriesAndSaveSQLite(List<BaseData> baseDataList, List<String> listOfEntries)
    {
        boolean success;
        int i = -1;
        for(BaseData dataElement : baseDataList)
        {
            i++;
            if(dataElement == null) //Beim Lesen aus Firebase kann es passieren, dass [0] in der ArrayList null ist. Auch weitere, warum?? //TODO
            {
                Log.i("Counter", "Es fehlt Element i=" + i);
                continue;
            }
            listOfEntries.add(dataElement.getCityName());
            dbHelper.insertOrUpdateCityBaseDataRow(dataElement);
        }

        Log.d("DataSvc", "The basedata of all german cities was stored in the SQLite database.");
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
