package com.blackviper.coronadashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    Button btn_sendRequest;
    ListView lv;
    AutoCompleteTextView actv_city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        lv = (ListView) findViewById(R.id.lv_responseView);
        actv_city = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);
        String[] listOfEntries = new String[] {"Recklinghausen", "Bochum", "Dortmund"}; //TODO
        actv_city.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listOfEntries));

        btn_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataService dataService = new DataService(MainActivity.this);
                dataService.getCityId(getCityInput(), new DataService.VolleyResponseListener() { //der Listener ist ein Interface und muss implementiert werden
                    @Override
                    public void onError(String message) {
                        showToastTextLong(message);
                        Log.d("onError", message);
                    }

                    /**
                     * Callback-Methode, welche aufgerufen wird, wenn die Antwort angekommen ist.
                     * @param cityId Das ist die Antwort vom Server, die wir weiterverwenden können
                     */
                    @Override
                    public void onResponse(int cityId) { //
                        showToastTextLong("Callback-Answer: City-ID = " + cityId);
                    }
                });
            }
        });
    }

    private void showToastTextShort(String str)
    {
        showToastText(str, Toast.LENGTH_SHORT);
    }

    private void showToastTextLong(String str)
    {
        showToastText(str, Toast.LENGTH_LONG);
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

    private String getCityInput()
    {
        String input = "";
        try {
            input = actv_city.getText().toString();
            if (input.isEmpty()) {
                throw new IllegalArgumentException("Geben Sie eine kreisfreie Stadt oder einen Landkreis an.");
                //TODO Exception Handling
            }
        } catch (IllegalArgumentException e) //Dem User anzeigen
        {
            showToastTextShort(e.getMessage());
            Log.d("OnClickMethod", e.toString());
        }
        return input;
    }

}