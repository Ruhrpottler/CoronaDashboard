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
    private final int ID_BTN_RELOAD = R.id.btn_reload;
    private final int ID_IC_OFFLINE = R.id.ic_offline;
    private AutoCompleteTextView actv;

    final UiUtility uiUtility = new UiUtility(MainActivity.this);
    private final DataSvc dataSvc = new DataSvc(this, MainActivity.this);
    private final SQLiteDatabaseHelper dbHelper = new SQLiteDatabaseHelper(MainActivity.this);
    private final FirebaseSvc firebaseSvc = FirebaseSvc.getFirebaseInstance(); //TODO könnte man auch aus dem DataSvc ziehen

    private AlarmSvc alarmSvc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmSvc = new AlarmSvc(this);

        setContentView(R.layout.activity_main);
        Button btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        TextView tvErgebnisse = (TextView) findViewById(R.id.tvErgebnisse);
        actv = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);

        setupActv();
        fillActv();
        setupBtnSendRequest();
        setupBtnReload();
    }

    private void setupActv()
    {
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                pushKeyboardDown();
            }
        });
    }

    private void fillActv()
    {
        dataSvc.fillActvCity(actv, MainActivity.this, new ActvSetupResponseListener() {
            @Override
            public void onError(String message) {
                message = "Fehler beim Initialisieren der Städte-Listeneinträge. " + message;
                uiUtility.showToastTextLong(message);
                Log.e("actvSetupFailed", message);
            }

            @Override
            public void onResponse(List<String> listOfEntries) {
                //fill ACTV
                actv.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listOfEntries));
                uiUtility.showToastTextShort("AutoComplete done.");
            }
        });
    }

    private void setupBtnSendRequest()
    {
        Button button = (Button) findViewById(R.id.btn_sendRequest);
        button.setOnClickListener(new View.OnClickListener()
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

                CityResponseListener apiListener = new CityResponseListener()
                {
                    //via API
                    @Override
                    public void onResponse(City city)
                    {
                        //alarmSvc.warnUser(city); //TODO kann weg, falls ich nur über mehrere Tage warne
                        TextView tvErgebnisse = (TextView) findViewById(R.id.tvErgebnisse);
                        tvErgebnisse.setText(city.toString()); //print the newest data
                        if(!isOfflineModeEnabled())
                        {
                            firebaseSvc.saveCityData(city);
                        }
                    }

                    @Override
                    public void onError(String message)
                    {
                        Log.e("MainActivity", message);
                        uiUtility.showToastTextLong(message);
                    }
                };

                //Will be called when the city was loaded from the database
                CityResponseListener firebaseListener = new CityResponseListener()
                {
                    @Override
                    public void onResponse(City city)
                    {
                        alarmSvc.warnUser(city);
                        //TODO Später einen Graphen zeigen
                    }

                    @Override
                    public void onError(String message)
                    {
                        //TODO msg vmtl. anpassen
                        Log.e("MainActivity", message);
                        uiUtility.showToastTextLong(message);
                    }
                };

                dataSvc.getCityByName(userInputCityName, apiListener, firebaseListener);
            }
        });
    }

    private void setupBtnReload()
    {
        Button btn_reload = (Button) findViewById(ID_BTN_RELOAD);
        btn_reload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(isOfflineModeEnabled())
                {
                    fillActv();
                }
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
        return actv.getText().toString().trim();
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
        findViewById(ID_IC_OFFLINE).setVisibility(visibility);
        findViewById(ID_BTN_RELOAD).setVisibility(visibility);
    }

    protected boolean isOfflineModeEnabled()
    {
        return (findViewById(ID_IC_OFFLINE).getVisibility() == View.VISIBLE);
    }

}