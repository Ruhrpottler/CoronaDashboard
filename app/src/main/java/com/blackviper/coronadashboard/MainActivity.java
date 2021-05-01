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

import Model.CityDataModel;

public class MainActivity extends AppCompatActivity {

    Button btn_sendRequest;
    ListView lv;
    AutoCompleteTextView actv_city;
    final DataService dataService = new DataService(MainActivity.this); //oder doch in die onCreateMethode?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        lv = (ListView) findViewById(R.id.lv_responseView);

        //AutoComplete
        actv_city = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);
        String[] listOfEntries = new String[] {"Recklinghausen", "Bochum", "Dortmund"}; //TODO
        actv_city.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listOfEntries));

        btn_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userInputCityName = getCityInput();
                if(userInputCityName.isEmpty())
                {
                    showToastTextLong(getString(R.string.err_actv_cityName_empty));
                    return;
                }

                dataService.getDataForCity(userInputCityName, new DataService.CityDataModelResponseListener() {
                    @Override
                    public void onError(String message) {
                        showToastTextLong(message);
//                       Log.e("onError", message);
                    }

                    @Override
                    public void onResponse(CityDataModel cityDataModel) {
                        showToastTextShort("7-Tage-Inzidenzwert: " + cityDataModel.getCases7_per_100k_txt());
                    }
                });




//                dataService.getCityId(userInputCityName, new DataService.VolleyResponseListener() { //der Listener ist ein Interface und muss implementiert werden
//                    @Override
//                    public void onError(String message) {
//                        showToastTextLong(message);
//                        Log.e("onError", message);
//                    }
//
//                    /**
//                     * Callback-Methode, welche aufgerufen wird, wenn die Antwort angekommen ist.
//                     * @param cityId Das ist die Antwort vom Server, die wir weiterverwenden können
//                     */
//                    @Override
//                    public void onResponse(int cityId) { //
//                        showToastTextLong("Callback-Answer: City-ID = " + cityId);
//                    }
//                });
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
        {
            length = 1;
            Log.e("IllegalArgument", String.format("Länge '%d' für Toast.Length ungültig. Wird auf 1 (LONG) gesetzt.", length));
        }
        Toast.makeText(MainActivity.this, str, length).show();
    }

    private String getCityInput()
    {
        String input = "";
        input = actv_city.getText().toString();
        return input;
    }
}