package com.blackviper.coronadashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btn_sendRequest;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        lv = (ListView) findViewById(R.id.lv_responseView);


        btn_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

                //TODO objectId für jeden Landkreis herausfinden können und diese direkt in die URL klatschen, um mit dem Namen der Stadt die aktuellen Daten zu bekommen.
                //TODO Daten richtig darstellen (ListView etc)
                int objectId = 95;
                String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?" +
                        "where=OBJECTID%3D" + objectId + "&outFields=OBJECTID,BEZ,EWZ,death_rate,cases,deaths,cases_per_100k,cases_per_population,county," +
                        "last_update,cases7_per_100k,recovered,EWZ_BL,cases7_bl_per_100k,cases7_bl,death7_bl,cases7_lk," +
                        "death7_lk,cases7_per_100k_txt&returnGeometry=false&f=json";

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String userOutput = "7-Tage-Inzidenzwert: ";
                        try
                        {
                            //response.getJSONArray("features").getJSONObject(0).getJSONObject("attributes").getString("cases7_per_100k_txt"); //Gesamte Anfrage

                            JSONArray features = response.getJSONArray("features");
                            if(features == null) throw new JSONException("features is null");

                            JSONObject firstAndOnlyArrayObject = features.getJSONObject(0); //Object without name
                            if(firstAndOnlyArrayObject == null) throw new JSONException("firstAndOnlyArrayObject is null");

                            JSONObject attributes = firstAndOnlyArrayObject.getJSONObject("attributes");
                            if(attributes == null) throw new JSONException("attributes is null");

                            userOutput += attributes.getString("cases7_per_100k_txt");
                            //TODO Zahl abfreifen (welcher Datentyp)
                            //TODO Wenn ich ganz krass bin: Werte zwischenspeichern, um zu zeigen, wann 5 tage unter und wann 3 Tage über 100.
                            //TODO Damit kann man z.B. prüfen, wie viele Landkreise nun welche Regelungen (z.B. Ausgangssperren) haben.

                            showToastTextLong(userOutput);
                        } catch(JSONException e) {
                            e.printStackTrace(); //TODO Exception-Handling verbessern
                            Log.d("JSONException", e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("onErrorResponse", error.toString());
                        showToastTextShort("Es ist ein Fehler aufgetreten, siehe auf der Konsole nach!");
                    }
                });

                queue.add(request);
            }
        });

    }

    private void showToastTextShort(String str)
    {
        showToastText(str, Toast.LENGTH_SHORT); //0
    }

    private void showToastTextLong(String str)
    {
        showToastText(str, Toast.LENGTH_LONG); //1
    }

    private void showToastText(String str, int length)
    {
        if(length < 0 || length > 1)
            throw new IllegalArgumentException(String.format("Länge '%d' für Toast.Length ungültig.", length));
        Toast.makeText(MainActivity.this, str, length).show();
    }

}