package com.blackviper.coronadashboard;

import android.content.Context;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Database.DatabaseHelper;
import Model.CityDataModel;
import Model.CityBaseDataModel;

/**
 * Diese Klasse stellt asynchrone Methoden (Callbacks) zur Verfügung, welche den Traffic mit den Anfragen an die API
 * verwaltet und z.B. die ID für eine Stadt, ein Bundesland oder die Strings zurück gibt.
 */
public class DataSvc
{

    //Klassenattribute
    Context activityContext;
    int cityId;

    private static final String STR_OBJECT_ID = "OBJECTID";
    private static final String STR_BL_ID = "BL_ID";
    private static final String STR_BEZ = "BEZ";
    private static final String STR_GEN = "GEN";
    private static final String STR_EWZ = "EWZ";

    //Konstuktoren
    public DataSvc(Context activityContext)
    {
        this.activityContext = activityContext;
    }

    /** Callback-Methoden, welche in der Activity implementiert werden müssen
     */
    public interface CityIdResponseListener
    {
        void onError(String message);
        void onResponse(int cityId);
    }

    //Methoden
    /**
     * @param cityName Landkreis oder kreisfreie Stadt
     * @return objectId der city
     */
    public void getCityIdByName(String cityName, CityIdResponseListener responseListener)
    {
        String[] inputArray = seperateBezAndGen(cityName);
        String bez = inputArray[0];
        String gen = inputArray[1];

        String whereKlausel;
        if(bez.isEmpty()) {
            whereKlausel = "GEN%20%3D%20'" + gen + "'";
        }
        else {
            whereKlausel = "BEZ%20%3D%20'" + bez + "'%20AND%20GEN%20%3D%20'" + gen + "'";
        }

        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/" +
                "RKI_Landkreisdaten/FeatureServer/0/query?where=" + whereKlausel +
                "&outFields=OBJECTID&returnGeometry=false&returnIdsOnly=true&outSR=&f=json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    JSONArray objectIdsArray = response.getJSONArray("objectIds");
                    if (objectIdsArray == null) {
                        throw new JSONException("'objectIds' is null");
                    }
                    if(objectIdsArray.length() == 0) {
                        throw new IllegalArgumentException(String.format("'%s' konnte nicht gefunden werden.", cityName));
                    }
                    else if (objectIdsArray.length() > 1) {
                        throw new IllegalArgumentException(String.format("Es wurden mehrere Ergebnisse für '%s' gefunden.", cityName));
                    }

                    cityId = objectIdsArray.getInt(0);

                    if(cityId == 0) {
                        throw new IllegalArgumentException(String.format("Für '%s' wurde die ungültige Objekt-ID %d gefunden.", cityName, cityId));
                    }
                    responseListener.onResponse(cityId); //ruft den Listener in der Activity auf --> callback
                } catch (JSONException e)
                {
                    e.printStackTrace(); //TODO Exception-Handling verbessern
                    Log.d("JSONException", e.toString());
                } catch(IllegalArgumentException userException)
                {
                    Toast.makeText(activityContext, "Fehler: " + userException.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                String msg = "Fehler bei der Verarbeitung der Server-Antwort: " + error.toString();
                Log.d("onErrorResponse", msg);
                responseListener.onError(msg);
            }
        });
        RequestSingleton.getInstance(activityContext).addToRequestQueue(request);
    }

    public interface CityDataModelResponseListener
    {
        void onError(String message);
        void onResponse(CityDataModel cityDataModel);
    }

    public void getCityDataByCityId(int cityId, CityDataModelResponseListener responseListener)
    {
        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?" +
                "where=OBJECTID%3D" + cityId + "&outFields=OBJECTID,BEZ,GEN,EWZ,BL_ID,BL,last_update,death_rate,cases,deaths,cases_per_100k,cases_per_population," +
                "cases7_per_100k,cases7_lk,death7_lk,death7_lk,cases7_bl_per_100k,cases7_bl,death7_bl" +
                "&returnGeometry=false&f=json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    JSONArray features = response.getJSONArray("features");
                    if (features == null)
                    {
                        throw new JSONException("'features' is null");
                    }

                    JSONObject firstAndOnlyArrayObject = features.getJSONObject(0); //Object without name
                    if (firstAndOnlyArrayObject == null)
                    {
                        throw new JSONException("'firstAndOnlyArrayObject' is null");
                    }

                    JSONObject attributes = firstAndOnlyArrayObject.getJSONObject("attributes");
                    if (attributes == null)
                    {
                        throw new JSONException("'attributes' is null");
                    }

                    CityDataModel cityDataModel = createAndFillCityDataModel(attributes);
                    if(cityDataModel == null)
                    {
                        responseListener.onError("cityDataModel ist null");
                    }

                    responseListener.onResponse(cityDataModel);
                } catch (Exception e)
                {
                    responseListener.onError(e.getMessage());
                    Log.e("errGetCityDataById", e.toString());
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                String msg = "Fehler bei der Verarbeitung der Server-Antwort: " + error.toString();
                Log.d("onErrorResponse", msg);
                responseListener.onError(msg);
            }
        });
        RequestSingleton.getInstance(activityContext).addToRequestQueue(request);
    }

    /** Callback-Hell
     * @param cityName ohne Präfix
     * @param modelResponseListener Wird aufgerufen, wenn Response vom Server da ist
     */
    public void getCityDataByName(String cityName, CityDataModelResponseListener modelResponseListener)
    {
        getCityIdByName(cityName, new CityIdResponseListener()
        {
            @Override
            public void onError(String message)
            {
                Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show();
                Log.e("onErrCityIdListener", message);
            }

            @Override
            public void onResponse(int cityId)
            {
                getCityDataByCityId(cityId, new CityDataModelResponseListener()
                {
                    @Override
                    public void onError(String message)
                    {
                        Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show();
                        Log.e("onErrGetCityData", message);
                    }

                    @Override
                    public void onResponse(CityDataModel cityDataModel)
                    {
                        modelResponseListener.onResponse(cityDataModel);
                    }
                });
            }
        });
    }

    /** Achtung: case-sensitive!
     *
     * @param attributes features --> first obj of the array --> attributes
     * @return CityDataModel, für das alle setter ausgeführt wurden
     * @throws JSONException Evtl. liefert der Server für die Felder keine Antwort, dann fliegt die Exception
     */
    private CityDataModel createAndFillCityDataModel(JSONObject attributes) throws JSONException
    {
        CityBaseDataModel cityBaseData = new CityBaseDataModel(
                attributes.getInt(STR_OBJECT_ID),
                attributes.getInt(STR_BL_ID),
                attributes.getString(STR_BEZ),
                attributes.getString(STR_GEN),
                attributes.getInt(STR_EWZ)
        );

        if(cityBaseData == null)
        {
            return null; //TODO Error Handling
        }

        CityDataModel cityData = new CityDataModel(
//                attributes.getInt(STR_OBJECT_ID),
//                attributes.getString(STR_BEZ),
//                attributes.getString(STR_GEN),
//                attributes.getInt(STR_EWZ),
//                attributes.getInt(STR_BL_ID),
                cityBaseData,
                attributes.getString("BL"),
                attributes.getString("last_update"),
                attributes.getDouble("death_rate"),
                attributes.getInt("cases"),
                attributes.getInt("deaths"),
                attributes.getDouble("cases_per_100k"),
                attributes.getDouble("cases_per_population"),
                attributes.getDouble("cases7_per_100k"),
                attributes.getInt("cases7_lk"),
                attributes.getInt("death7_lk"),
                attributes.getDouble("cases7_bl_per_100k"),
                attributes.getInt("cases7_bl"),
                attributes.getInt("death7_bl")
        );
        return cityData;
    }

    private String[] seperateBezAndGen(String cityName)
    {
        cityName = cityName.toLowerCase();
        String[] cityNameArray;// = new String[2];
        if(cityName.startsWith("kreisfreie stadt"))
            cityNameArray = seperateString("kreisfreie stadt", cityName);
        else if(cityName.startsWith("landkreis"))
            cityNameArray = seperateString("landkreis", cityName);
        else if(cityName.startsWith("stadtkreis"))
            cityNameArray = seperateString("stadtkreis", cityName);
        else if(cityName.startsWith("kreis"))
            cityNameArray = seperateString("kreis", cityName);
        else if(cityName.startsWith("bezirk"))
            cityNameArray = seperateString("bezirk", cityName);
        else
            cityNameArray = new String[]{"", cityName};

        return cityNameArray;
    }

    private String[] seperateString(String firstPart, String fullString)
    {
        return new String[]{firstPart, fullString.replace(firstPart, "").trim()};
    }

    public interface CityBaseDataResponseListener
    {
        void onError(String message);
        void onResponse(List<CityBaseDataModel> list);
    }

    /**
     * Get list with all Cities in Germany
     */
    public void getAllCities(CityBaseDataResponseListener responseListener)
    {

        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/" +
                "query?where=1%3D1&outFields=OBJECTID,BL_ID,GEN,BEZ,EWZ&returnGeometry=false&outSR=&f=json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    JSONArray features = response.getJSONArray("features");
                    if (features == null)
                    {
                        throw new JSONException("'features' is null");
                    }

                    JSONObject iterator;
                    JSONObject attributes;

                    List<CityBaseDataModel> list = new ArrayList<CityBaseDataModel>();
                    for(int i = 0; i < features.length(); i++)
                    {
                        attributes = features.getJSONObject(i).getJSONObject("attributes");
                        list.add(fillAndGetCityBaseDataModel(attributes));
                    }

                    responseListener.onResponse(list); //ruft die implementierte Methode auf (MainActivity) --> callback
                } catch (Exception e)
                {
                    responseListener.onError(e.getMessage());
                    Log.d("errOnGetAllCities", e.toString());
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                String msg = "Fehler bei der Verarbeitung der Server-Antwort: " + error.toString();
                Log.d("onErrorResponse", msg);
                responseListener.onError(msg);
            }
        });
        RequestSingleton.getInstance(activityContext).addToRequestQueue(request);

    }

    private CityBaseDataModel fillAndGetCityBaseDataModel(JSONObject attributes) throws JSONException
    {
        return new CityBaseDataModel(
                attributes.getInt(STR_OBJECT_ID),
                attributes.getInt(STR_BL_ID),
                attributes.getString(STR_BEZ),
                attributes.getString(STR_GEN),
                attributes.getInt(STR_EWZ)
        );
    }

    public interface ActvSetupResponseListener
    {
        void onError(String message);
        void onResponse(List<String> listOfEntries);
    }

    public void fillActvCity(AutoCompleteTextView actv_city, Context activityContext, ActvSetupResponseListener responseListener)
    {
        DatabaseHelper dbHelper = new DatabaseHelper(activityContext); //Können sich zwei DbHelper in die Quere kommen? Kann man ein Singleton aus dem Helper machen?
        List<String> listOfEntries= new ArrayList<String>();

        getAllCities(new CityBaseDataResponseListener()
        {
            @Override
            public void onError(String message)
            {
                Log.e("ErrGetAllCities", message);
                responseListener.onError(message);
            }

            @Override
            public void onResponse(List<CityBaseDataModel> list)
            {
                boolean success;
                for(int i = 0; i < list.size(); i++)
                {
                    listOfEntries.add(list.get(i).getCityName());
                    success = dbHelper.insertOrUpdateCityBaseDataRow(list.get(i));
                    if(!success)
                        Log.e("ErrWhileInsertOrUpdate", String.format("Fehler beim Insert/Update des Tupels '%s'. No success.", list.get(i).toString()));
                }
                responseListener.onResponse(listOfEntries);
            }
        });
    }
}
