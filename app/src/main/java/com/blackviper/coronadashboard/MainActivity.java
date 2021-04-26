package com.blackviper.coronadashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
                //Toast.makeText(MainActivity.this, "Button wurde gedrückt.", Toast.LENGTH_SHORT).show();
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String url ="https://www.metaweather.com/api/location/search/?query=london"; //TODO hardcoded

                JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) { //Wir erwarten als Antwort vom Server ein JSONArray (aus JSON-Objekten)
                        String cityId = "";
                        try {
                            JSONObject jObject = response.getJSONObject(0);
                            cityId = jObject.getString("woeid");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showToastTextLong("Error: Keine oder fehlerhafte Daten erhalten.");
                        }
                        showToastTextLong("City-ID = " + cityId); //response.toString()
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToastTextShort("Es ist ein Fehler aufgetreten.");
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