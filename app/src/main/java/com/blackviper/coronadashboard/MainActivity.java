package com.blackviper.coronadashboard;

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

import androidx.appcompat.app.AppCompatActivity;

import com.blackviper.coronadashboard.ResponseListener.ActvSetupResponseListener;
import com.blackviper.coronadashboard.ResponseListener.CityResponseListener;

import java.util.List;

import Database.FirebaseSvc;
import Database.SQLiteDatabaseHelper;
import Model.City;
import Tools.UiUtility;

public class MainActivity extends AppCompatActivity
{
    private AutoCompleteTextView actv_city;

    final UiUtility uiUtility = new UiUtility(MainActivity.this);
    private final DataSvc dataSvc = new DataSvc(this, MainActivity.this);
    private final SQLiteDatabaseHelper dbHelper = new SQLiteDatabaseHelper(MainActivity.this);
    private final FirebaseSvc firebaseSvc = FirebaseSvc.getFirebaseInstance(); //TODO könnte man auch aus dem DataSvc ziehen
    private NotificationSvc notificationSvc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notificationSvc = new NotificationSvc(this);

        Button btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        ListView lv = (ListView) findViewById(R.id.lv_responseView);
        TextView tvErgebnisse = (TextView) findViewById(R.id.tvErgebnisse);
        actv_city = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);

        setupActv_city();
        dataSvc.fillActvCity(actv_city, MainActivity.this, new ActvSetupResponseListener() {
            @Override
            public void onError(String message) {
                message = "Fehler beim Initialisieren der Städte-Listeneinträge. " + message;
                uiUtility.showToastTextLong(message);
                Log.e("actvSetupFailed", message);
            }

            @Override
            public void onResponse(List<String> listOfEntries) {
                //fill ACTV
                actv_city.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listOfEntries));
                uiUtility.showToastTextShort("AutoComplete done.");
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
                    uiUtility.showToastTextLong(getString(R.string.err_actv_cityName_empty));
                    return;
                }

                pushKeyboardDown();

                dataSvc.getCityDataByName(userInputCityName, new CityResponseListener()
                {
                    @Override
                    public void onError(String message)
                    {
                        Log.e("ErrGetCityDataByName", message);
                        uiUtility.showToastTextLong(message);
                    }

                    @Override
                    public void onResponse(City city)
                    {
                        tvErgebnisse.setText(city.toString());
                        if(!isOfflineModeEnabled())
                        {
                            firebaseSvc.saveCityData(city);
                        }

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
                pushKeyboardDown();
            }
        });
    }

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

    private String getCityInput()
    {
        return actv_city.getText().toString().trim();
    }

    public void enableOfflineMode()
    {
        setOfflineMode(View.VISIBLE);
    }

    public void disableOfflineMode()
    {
        setOfflineMode(View.INVISIBLE);
    }

    private void setOfflineMode(int visibility)
    {
        findViewById(R.id.ic_offline).setVisibility(visibility);
    }

    private boolean isOfflineModeEnabled() //TODO notification when switch
    {
        return (findViewById(R.id.ic_offline).getVisibility() == View.VISIBLE);
    }

}