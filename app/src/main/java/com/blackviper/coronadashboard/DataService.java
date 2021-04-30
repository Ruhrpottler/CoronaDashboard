package com.blackviper.coronadashboard;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.cronet.CronetHttpStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Diese Klasse stellt asynchrone Methoden zur Verfügung, welche den Traffic mit den Anfragen an die API
 * managed und uns z.B. die ID für eine Stadt, ein Bundesland oder die Strings zurück gibt.
 */
public class DataService {

    //Klassenattribute
    Context activityContext;
    int cityId;

    //Konstuktoren
    public DataService(Context activityContext)
    {
        this.activityContext = activityContext;
    }

    /** Callback-Methoden, welche in der Activity implementiert werden müssen
     */
    public interface VolleyResponseListener
    {
        void onError(String message);
        //void onResponse(Object response);
        void onResponse(int cityId);
    }

    //Methoden
    /**
     *
     * @param cityName Landkreis oder kreisfreie Stadt
     * @return objectId der city
     */
    public void getCityId(String cityName, VolleyResponseListener responseListener)
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

                    cityId = objectIdsArray.getInt(0); //Richtig??

                    if(cityId == 0)
                        throw new IllegalArgumentException(String.format("Für '%s' wurde die ungültige Objekt-ID %d gefunden.", cityName, cityId));
                    responseListener.onResponse(cityId); //ruft die implementierte Methode auf (MainActivity) --> callback
                } catch (JSONException e) {
                    e.printStackTrace(); //TODO Exception-Handling verbessern
                    Log.d("JSONException", e.toString());
                } catch(IllegalArgumentException userException)
                {
                    Toast.makeText(activityContext, userException.getMessage(), Toast.LENGTH_LONG).show();
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

    public void getDataForCity(String cityName) //TODO unfertig
    {
        VolleyResponseListener responseListener = new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show();
                Log.e("onError", message);
            }

            @Override
            public void onResponse(int responsedCityId) {
                cityId = responsedCityId;
            }
        };

        getCityId(cityName, responseListener);

        //TODO Video anschauen: Warten bis die Antwort kommt udn dann nächste Anfrage absenden

        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?" +
                "where=OBJECTID%3D" + cityId + "&outFields=OBJECTID,BEZ,EWZ,death_rate,cases,deaths,cases_per_100k,cases_per_population,county," +
                "last_update,cases7_per_100k,recovered,EWZ_BL,cases7_bl_per_100k,cases7_bl,death7_bl,cases7_lk," +
                "death7_lk,cases7_per_100k_txt&returnGeometry=false&f=json";

        //double inzidenzwert; //TODO

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
                    //inzidenzwert = attributes.getDouble("cases7_per_100k");

                    if(cityId == 0)
                        throw new IllegalArgumentException(String.format("Für '%s' wurde die ungültige Objekt-ID %d gefunden.", cityName, cityId));
                    responseListener.onResponse(cityId); //ruft die implementierte Methode auf (MainActivity) --> callback
                } catch (JSONException e) {
                    e.printStackTrace(); //TODO Exception-Handling verbessern
                    Log.d("JSONException", e.toString());
                } catch(IllegalArgumentException userException)
                {
                    Toast.makeText(activityContext, userException.getMessage(), Toast.LENGTH_LONG).show();
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

    public void getAllCities()
    {
        //TODO Alle Städte/LKs für AutoComplete herausfinden, geeigneten Rückgabe-Datentypen finden (String-Array? Liste?)
        //TODO Performance beachten, vlt sogar mal messen!
    }
}
