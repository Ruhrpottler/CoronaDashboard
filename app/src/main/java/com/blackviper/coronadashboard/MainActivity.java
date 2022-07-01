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

import Database.SQLiteDatabaseHelper;
import Database.FirebaseSvc;
import Model.City;
import Model.CoronaData;

public class MainActivity extends AppCompatActivity {

    Button btn_sendRequest;
    ListView lv;
    TextView tvErgebnisse;
    AutoCompleteTextView actv_city;

    final DataSvc dataSvc = new DataSvc(MainActivity.this);
    final SQLiteDatabaseHelper dbHelper = new SQLiteDatabaseHelper(MainActivity.this);
    final FirebaseSvc firebaseSvc = new FirebaseSvc();
    static NotificationSvc notificationSvc; //TODO non static machen?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notificationSvc = new NotificationSvc(this); //TODO init in try-catch packen, damit die App auch funktioniert, wenn es hierbei Probleme gibt

        btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        lv = (ListView) findViewById(R.id.lv_responseView);
        tvErgebnisse = (TextView) findViewById(R.id.tvErgebnisse);
        actv_city = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);

        setupActv_city();
        //TODO Die App hängt hierbei trotzdem, wenn die ganze Schleife durchlaufen wird. Hier muss man background-services nutzen (Intent oder was es ist)
        dataSvc.fillActvCity(actv_city, MainActivity.this, new DataSvc.ActvSetupResponseListener() {
            @Override
            public void onError(String message) {
                message = "Fehler beim Initialisieren der Städte-Listeneinträge. " + message;
                showToastTextLong(message);
                Log.e("actvSetupFailed", message);
            }

            @Override
            public void onResponse(List<String> listOfEntries) {
                actv_city.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listOfEntries));
                showToastTextShort("AutoComplete done.");
            }
        });

        btn_sendRequest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String userInputCityName = getCityInput();
                if(userInputCityName.isEmpty())
                {
                    showToastTextLong(getString(R.string.err_actv_cityName_empty));
                    return;
                }

                pushKeyboardDown();

                dataSvc.getCityDataByName(userInputCityName, new DataSvc.CityResponseListener()
                {
                    @Override
                    public void onError(String message)
                    {
                        Log.e("ErrGetCityDataByName", message);
                        showToastTextLong(message);
                    }

                    @Override
                    public void onResponse(City city)
                    {
                        tvErgebnisse.setText(city.toString());
                        firebaseSvc.saveCityData(city);
                    }
                });

            }
        });
    }

    private void setupActv_city()
    {
        actv_city.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                pushKeyboardDown(); //Methode onKeyboardDownListener dingen machen
            }
        });
    }

    //TODO In Tools auslagern

    public void pushKeyboardDown()
    {
        View view = this.getCurrentFocus();
        if (view == null)
        {
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
        if(length == Toast.LENGTH_SHORT || length == Toast.LENGTH_LONG)
        {
            length = Toast.LENGTH_LONG;
            Log.i("IllegalArgument", String.format("'%d' is an illegal length for the toast display time.", length));
        }
        Toast.makeText(MainActivity.this, str, length).show();
    }

    private String getCityInput()
    {
        return actv_city.getText().toString().trim();
    }
}