package com.blackviper.coronadashboard;

import android.content.Context;
import android.util.Log;
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

import Model.CityDataModel;
import Model.CityStammdatenModel;

/**
 * Diese Klasse stellt asynchrone Methoden (Callbacks) zur Verfügung, welche den Traffic mit den Anfragen an die API
 * managed und uns z.B. die ID für eine Stadt, ein Bundesland oder die Strings zurück gibt.
 */
public class DataService {

    //Klassenattribute
    Context activityContext;
    int cityId;

    private static final String STR_OBJECT_ID = "OBJECTID";
    private static final String STR_BL_ID = "BL_ID";
    private static final String STR_BEZ = "BEZ";
    private static final String STR_GEN = "GEN";
    private static final String STR_EWZ = "EWZ";

    //Konstuktoren
    public DataService(Context activityContext)
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
    public void getCityId(String cityName, CityIdResponseListener responseListener)
    {
        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/" +
                "RKI_Landkreisdaten/FeatureServer/0/query?where=GEN%20%3D%20'" + cityName + "'" +
                "&outFields=OBJECTID,GEN&returnGeometry=false&returnIdsOnly=true&outSR=&f=json";
        //int cityId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray objectIdsArray = response.getJSONArray("objectIds");
                    if (objectIdsArray == null)
                        throw new JSONException("'objectIds' is null");
                    if(objectIdsArray.length() == 0)
                        throw new IllegalArgumentException(String.format("'%s' konnte nicht gefunden werden.", cityName));
                    else if (objectIdsArray.length() > 1)
                        throw new IllegalArgumentException("Es wurden mehrere Ergebnisse gefunden.");

                    cityId = objectIdsArray.getInt(0);

                    if(cityId == 0)
                        throw new IllegalArgumentException(String.format("Für '%s' wurde die ungültige Objekt-ID %d gefunden.", cityName, cityId));
                    responseListener.onResponse(cityId); //ruft den Listener in der Activity auf --> callback
                } catch (JSONException e) {
                    e.printStackTrace(); //TODO Exception-Handling verbessern
                    Log.d("JSONException", e.toString());
                } catch(IllegalArgumentException userException)
                {
                    Toast.makeText(activityContext, "Fehler: " + userException.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
                "cases7_per_100k,cases7_per_100k_txt,cases7_lk,death7_lk,death7_lk,cases7_bl_per_100k,cases7_bl,death7_bl" +
                "&returnGeometry=false&f=json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray features = response.getJSONArray("features");
                    if (features == null) throw new JSONException("'features' is null");
                    JSONObject firstAndOnlyArrayObject = features.getJSONObject(0); //Object without name
                    if (firstAndOnlyArrayObject == null) throw new JSONException("'firstAndOnlyArrayObject' is null");
                    JSONObject attributes = firstAndOnlyArrayObject.getJSONObject("attributes");
                    if (attributes == null) throw new JSONException("'attributes' is null");

                    CityDataModel cityDataModel = createAndFillCityDataModel(attributes);
                    if(cityDataModel == null) responseListener.onError("cityDataModel ist null");

                    responseListener.onResponse(cityDataModel);
                } catch (Exception e) {
                    responseListener.onError(e.getMessage());
                    Log.e("errGetCityDataById", e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
        getCityId(cityName, new CityIdResponseListener() {
            @Override
            public void onError(String message) {
                Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show();
                Log.e("onErrCityIdListener", message);
            }

            @Override
            public void onResponse(int cityId) {
                getCityDataByCityId(cityId, new CityDataModelResponseListener() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show();
                        Log.e("onErrGetCityData", message);
                    }

                    @Override
                    public void onResponse(CityDataModel cityDataModel) {
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
        CityDataModel model = new CityDataModel(
                attributes.getInt(STR_OBJECT_ID),
                attributes.getString(STR_BEZ),
                attributes.getString(STR_GEN),
                attributes.getInt(STR_EWZ),
                attributes.getInt(STR_BL_ID),
                attributes.getString("BL"),
                attributes.getString("last_update"),
                attributes.getDouble("death_rate"),
                attributes.getInt("cases"),
                attributes.getInt("deaths"),
                attributes.getDouble("cases_per_100k"),
                attributes.getDouble("cases_per_population"),
                attributes.getDouble("cases7_per_100k"),
                attributes.getString("cases7_per_100k_txt"),
                attributes.getInt("cases7_lk"),
                attributes.getInt("death7_lk"),
                attributes.getDouble("cases7_bl_per_100k"),
                attributes.getInt("cases7_bl"),
                attributes.getInt("death7_bl")
        );
        return model;
    }

    public interface CityStammdatenResponseListener
    {
        void onError(String message);
        void onResponse(List<CityStammdatenModel> list);
    }

    /**
     *
     * @return Liste mit allen CityStammdatenModels für Deutschland
     */
    public void getAllCities(CityStammdatenResponseListener responseListener)
    {

        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/" +
                "query?where=1%3D1&outFields=OBJECTID,BL_ID,GEN,BEZ,EWZ&returnGeometry=false&outSR=&f=json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray features = response.getJSONArray("features");
                    if (features == null) throw new JSONException("'features' is null");

                    JSONObject iterator;
                    JSONObject attributes;

                    List<CityStammdatenModel> list = new ArrayList<CityStammdatenModel>();
                    for(int i = 0; i < features.length(); i++)
                    {
                        attributes = features.getJSONObject(i).getJSONObject("attributes");
                        list.add(fillAndGetCityStammdatenModel(attributes));
                    }

                    responseListener.onResponse(list); //ruft die implementierte Methode auf (MainActivity) --> callback
                } catch (Exception e) {
                    responseListener.onError(e.getMessage());
                    Log.d("errOnGetAllCities", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg = "Fehler bei der Verarbeitung der Server-Antwort: " + error.toString();
                Log.d("onErrorResponse", msg);
                responseListener.onError(msg);
            }
        });
        RequestSingleton.getInstance(activityContext).addToRequestQueue(request);

    }

    private CityStammdatenModel fillAndGetCityStammdatenModel(JSONObject attributes) throws JSONException
    {
        return new CityStammdatenModel(
                attributes.getInt(STR_OBJECT_ID),
                attributes.getInt(STR_BL_ID),
                attributes.getString(STR_BEZ),
                attributes.getString(STR_GEN),
                attributes.getInt(STR_EWZ)
        );

    }
}
