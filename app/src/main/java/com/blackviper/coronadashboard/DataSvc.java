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
import Tools.FormatTool;

/**
 * Diese Klasse stellt asynchrone Methoden (Callbacks) zur Verfügung, welche den Traffic mit den Anfragen an die API
 * verwaltet und z.B. die ID für eine Stadt, ein Bundesland oder die Strings zurück gibt.
 */
public class DataSvc
{
    //Klassenattribute
    private final MainActivity activity;
    private final Context context;
    private final FormatTool formatTool;
    private final SQLiteDatabaseHelper dbHelper;
    private final FirebaseSvc firebaseSvc = FirebaseSvc.getFirebaseInstance();
    private int objectId;

    //Konstuktoren
    public DataSvc(MainActivity activity, Context context)
    {
        this.activity = activity;
        this.context = context;
        this.formatTool = new FormatTool(context);
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
        String[] inputArray = formatTool.seperateBezAndGen(cityName);
        String bez = inputArray[0];
        String gen = inputArray[1];

        String whereCondition;
        if (bez.isEmpty())
        {
            whereCondition = "GEN%20%3D%20'" + gen + "'";
        }
        else
        {
            whereCondition = "BEZ%20%3D%20'" + bez + "'%20AND%20GEN%20%3D%20'" + gen + "'";
        }

        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/" + "RKI_Landkreisdaten/FeatureServer/0/query?where=" + whereCondition + "&outFields=OBJECTID&returnGeometry=false&returnIdsOnly=true&outSR=&f=json";

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
                    responseListener.onResponse(objectId); //ruft den Listener in der Activity auf (callback)
                } catch (JSONException e)
                {
                    e.printStackTrace(); //TODO Exception-Handling verbessern
                    Log.d("JSONException", e.toString());
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
        void onError(String message);

        void onResponse(City city);
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
                    JSONArray features = response.getJSONArray("features");

                    JSONObject firstAndOnlyArrayObject = features.getJSONObject(0); //Object without name
                    if (firstAndOnlyArrayObject == null)
                    {
                        throw new JSONException("'firstAndOnlyArrayObject' is null");
                    }
                    JSONObject attributes = firstAndOnlyArrayObject.getJSONObject("attributes");
                    City city = createAndFillCity(attributes);
                    responseListener.onResponse(city);
                }
                catch (Exception e)
                {
                    responseListener.onError(e.getMessage());
                    Log.e("DataSvc", e.toString());
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if(error instanceof NoConnectionError) //TODO Wenn kein Internet, auf die Firebase DB zugreifen.
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

    private City createAndFillCity(JSONObject attributes) throws JSONException
    {
        BaseData baseData = createAndFillCityBaseDataModel(attributes);
        CoronaData coronaData = createAndFillCityDataModel(attributes);
        return new City(baseData, coronaData);
    }

    private BaseData createAndFillCityBaseDataModel(JSONObject attributes) throws JSONException
    {
        return new BaseData(
                attributes.getInt(context.getString(R.string.STR_OBJECT_ID)),
                attributes.getInt(context.getString(R.string.STR_BL_ID)),
                attributes.getString(context.getString(R.string.STR_BL)),
                attributes.getString(context.getString(R.string.STR_BEZ)),
                attributes.getString(context.getString(R.string.STR_GEN)),
                attributes.getInt(context.getString(R.string.STR_EWZ))
        );
    }

    /**
     * Achtung: case-sensitive!
     *
     * @param attributes features --> first obj of the array --> attributes
     * @return CityDataModel, für das alle setter ausgeführt wurden
     * @throws JSONException Evtl. liefert der Server für die Felder keine Antwort, dann fliegt die Exception
     */
    private CoronaData createAndFillCityDataModel(JSONObject attributes) throws JSONException
    {
        return new CoronaData(
                attributes.getInt(context.getString(R.string.STR_OBJECT_ID)),
                attributes.getString(context.getString(R.string.STR_LAST_UPDATE)),
                attributes.getDouble(context.getString(R.string.STR_DEATH_RATE)),
                attributes.getInt(context.getString(R.string.STR_CASES)),
                attributes.getInt(context.getString(R.string.STR_DEATHS)),
                attributes.getDouble(context.getString(R.string.STR_CASES_PER_100k)),
                attributes.getDouble(context.getString(R.string.STR_CASES_PER_POPULATION)),
                attributes.getDouble(context.getString(R.string.STR_7_TAGE_INZIDENZWERT)),
                attributes.getInt(context.getString(R.string.STR_CASES_7)),
                attributes.getInt(context.getString(R.string.STR_DEATH_7)),
                attributes.getDouble(context.getString(R.string.STR_BL_7_TAGE_INZIDENZWERT)),
                attributes.getInt(context.getString(R.string.STR_CASES_7_BL)),
                attributes.getInt(context.getString(R.string.STR_DEATH_7_BL)));
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

    /**
     * Get list with all Cities in Germany
     */
    public void getAllCities(BaseDataListResponseListener responseListener)
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
                    JSONArray features = response.getJSONArray("features");

                    JSONObject iterator;
                    JSONObject attributes;

                    List<BaseData> list = new ArrayList<BaseData>();
                    for (int i = 0; i < features.length(); i++)
                    {
                        attributes = features.getJSONObject(i).getJSONObject("attributes");
                        list.add(createAndFillCityBaseDataModel(attributes));
                    }

                    responseListener.onResponse(list); //ruft die implementierte Methode auf (MainActivity) --> callback
                } catch (Exception e)
                {
                    responseListener.onError(e.getMessage());
                    Log.d("DataSvc", e.toString());
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

    }

    public interface ActvSetupResponseListener
    {
        void onError(String message);

        void onResponse(List<String> listOfEntries);
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
        getAllCities(new BaseDataListResponseListener()
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
