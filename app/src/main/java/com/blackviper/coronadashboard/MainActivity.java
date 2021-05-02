package com.blackviper.coronadashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import Database.DatabaseHelper;
import Model.CityDataModel;

public class MainActivity extends AppCompatActivity {

    Button btn_sendRequest;
    ListView lv;
    TextView tvErgebnisse;
    AutoCompleteTextView actv_city;

    final DataService dataService = new DataService(MainActivity.this);
    final DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        lv = (ListView) findViewById(R.id.lv_responseView);
        tvErgebnisse = (TextView) findViewById(R.id.tvErgebnisse);
        actv_city = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);

        setupActv_city();
        //TODO Die App hängt hierbei trotzdem, wenn die ganze Schleife durchlaufen wird. Hier muss man background-services nutzen (Intent oder was es ist)
        dataService.fillActvCity(actv_city, MainActivity.this, new DataService.ActvSetupResponseListener() {
            @Override
            public void onError(String message) {
                message = "Fehler beim initialisieren der Städte-Listeneinträge. " + message;
                showToastTextLong(message);
                Log.e("actvSetupFailed", message);
            }

            @Override
            public void onResponse(List<String> listOfEntries) {
                actv_city.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listOfEntries));
                showToastTextShort("AutoComplete done.");
            }
        });

        btn_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInputCityName = getCityInput();
                if(userInputCityName.isEmpty())
                {
                    showToastTextLong(getString(R.string.err_actv_cityName_empty));
                    return;
                }

                keyboardDown();

                dataService.getCityDataByName(userInputCityName, new DataService.CityDataModelResponseListener() {
                    @Override
                    public void onError(String message) {
                        Log.e("ErrGetCityDataByName", message);
                        showToastTextLong(message);
                    }

                    @Override
                    public void onResponse(CityDataModel cityDataModel) {
                        tvErgebnisse.setText(cityDataModel.toString());
                    }
                });

            }
        });
    }

    private void setupActv_city()
    {
        actv_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                keyboardDown(); //Methode onKeyboardDownListener dingen machen
            }
        });
    }

    public void keyboardDown()
    {
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            Log.w("IllegalArgument", String.format("Länge '%d' für Toast.Length ungültig. Wird auf 1 (LONG) gesetzt.", length));
        }
        Toast.makeText(MainActivity.this, str, length).show();
    }

    private String getCityInput()
    {
        String input = "";
        input = actv_city.getText().toString().trim();
        return input;
    }

    /**
     *
     * @param cityName
     * @return
     */
    private String filterPrefix(String cityName)
    {
        if(cityName.contains("München")) //Man könnte einmal die DB fragen ob es mehr als 1 Ergebnis gibt
            return cityName;

        if(cityName.startsWith("Kreisfreie Stadt"))
            cityName = cityName.replace("Kreisfreie Stadt", "");
        else if(cityName.startsWith("Landkreis"))
            cityName = cityName.replace("Landkreis", "");
        else if(cityName.startsWith("Stadtkreis"))
            cityName = cityName.replace("Landkreis", "");
        else if(cityName.startsWith("Kreis"))
            cityName = cityName.replace("Kreis", "");
        else if(cityName.startsWith("Bezirk"))
            cityName = cityName.replace("Bezirk", "");

        return cityName.trim();
    }
}