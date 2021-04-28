package com.blackviper.coronadashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btn_sendRequest;
    ListView lv;
    AutoCompleteTextView actv_landkreis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        lv = (ListView) findViewById(R.id.lv_responseView);
        actv_landkreis = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);
        String[] listOfEntries = new String[] {"Recklinghausen", "Bochum", "Dortmund"};
        actv_landkreis.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listOfEntries));

        //TODO asynchrone Anfrage (Callback)
        btn_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO objectId für jeden Landkreis herausfinden können und diese direkt in die URL klatschen, um mit dem Namen der Stadt die aktuellen Daten zu bekommen.
                //TODO Daten richtig darstellen (ListView etc)
                //TODO Prüfen, ob auch die richtigen Werte angezeigt werden (ids nochmal vergleichen oder Name)
                try {
                    String input = actv_landkreis.getText().toString();
                    if (input.isEmpty()) {
                        //showToastTextShort("Geben Sie eine kreisfreie Stadt oder einen Landkreis an.");
                        throw new IllegalArgumentException("Geben Sie eine kreisfreie Stadt oder einen Landkreis an.");
                        //TODO Exception Handling
                    }

                    showToastTextShort(input);

                    int objectId = 95; //TODO hardcoded
                    String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?" +
                            "where=OBJECTID%3D" + objectId + "&outFields=OBJECTID,BEZ,EWZ,death_rate,cases,deaths,cases_per_100k,cases_per_population,county," +
                            "last_update,cases7_per_100k,recovered,EWZ_BL,cases7_bl_per_100k,cases7_bl,death7_bl,cases7_lk," +
                            "death7_lk,cases7_per_100k_txt&returnGeometry=false&f=json";

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String userOutput = "7-Tage-Inzidenzwert: ";
                            double cases7_per_100k;
                            try {
                                JSONArray features = response.getJSONArray("features");
                                if (features == null) throw new JSONException("'features' is null");
                                JSONObject firstAndOnlyArrayObject = features.getJSONObject(0); //Object without name
                                if (firstAndOnlyArrayObject == null) throw new JSONException("'firstAndOnlyArrayObject' is null");
                                JSONObject attributes = firstAndOnlyArrayObject.getJSONObject("attributes");
                                if (attributes == null) throw new JSONException("'attributes' is null");
                                cases7_per_100k = attributes.getDouble("cases7_per_100k");
                                cases7_per_100k = Math.round(cases7_per_100k);

                                userOutput += attributes.getString("cases7_per_100k_txt");
                                //TODO Wenn ich ganz krass bin: Werte zwischenspeichern, um zu zeigen, wann 5 tage unter und wann 3 Tage über 100.
                                //TODO Damit kann man z.B. prüfen, wie viele Landkreise nun welche Regelungen (z.B. Ausgangssperren) haben.

                                //showToastTextLong(userOutput);
                                cases7_per_100k = roundDouble(cases7_per_100k, 1);
                                showToastTextLong(String.format("7-Tage Inzidenzwert: %.1f", cases7_per_100k));
                            } catch (JSONException e) {
                                e.printStackTrace(); //TODO Exception-Handling verbessern
                                Log.d("JSONException", e.toString());
                            }

                        }
                    }, new Response.ErrorListener() { //kann man mit Lambda replacen
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("onErrorResponse", error.toString());
                            showToastTextShort("Es ist ein Fehler aufgetreten, siehe auf der Konsole nach!");
                        }
                    });
                    RequestSingleton.getInstance(MainActivity.this).addToRequestQueue(request); //TODO prüfen, ob der ApplicationContext gezogen wird
                } catch (IllegalArgumentException e) //Dem User anzeigen
                {
                    String msg = e.getMessage();
                    showToastTextShort(msg);
                    Log.d("OnClickMethod", e.toString());
                } catch (Exception e)
                {
                    String msg = "Fehler: " + e.getMessage();
                    showToastTextShort(msg);
                    Log.d("OnClickMethod", msg);
                }
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

    /** Rundet "normal (ab 5 auf, vorher ab)
     * BigDecimal Round Modus Doku: https://docs.oracle.com/javase/7/docs/api/java/math/RoundingMode.html
     * @param value
     * @param stelle Diese Zahl gibt an, wie viele Zahlen (abgesehen von 0) hinter dem Komma stehen bleiben.
     *               Stelle 2 heißt also, es wird auf die 3. Stelle geschaut und anhand dessen entschieden.
     *               Bsp: 3.4537 wird zu 3.45
     */
    private double roundDouble(double value, int stelle) //TODO static oder nicht? In Eclipse eig schon
    {
        if (stelle < 0) throw new IllegalArgumentException(String.format("Runden auf die Stelle '%d' ist nicht möglich.", stelle));

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(stelle, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}