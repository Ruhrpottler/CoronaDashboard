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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import Database.FirebaseSvc;
import Database.SQLiteDatabaseHelper;
import Model.City;
import Model.CoronaData;
import Tools.UiUtility;

public class MainActivity extends AppCompatActivity
{
    private Button btn_sendRequest;
    private TextView tvErgebnisse;
    private AutoCompleteTextView actv_city;
    private LineChart lineChart;

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
        btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        tvErgebnisse = (TextView) findViewById(R.id.tvErgebnisse);
        actv_city = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);
        lineChart = (LineChart) findViewById(R.id.lineChart);
        lineChart.setNoDataText("");

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

        setupRequestBtn();
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

    private void setupRequestBtn()
    {
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

                CityResponseListener apiListener = new CityResponseListener()
                {
                    //via API
                    @Override
                    public void onResponse(City city)
                    {
                        //alarmSvc.warnUser(city); //TODO kann weg, falls ich nur über mehrere Tage warne

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
                        fillLineChart(city);
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

    private void fillLineChart(City city)
    {
        if(city == null || city.getBaseData() == null)
        {
            return;
        }
        ArrayList<Entry> entryList = createEntryList(city);
        if(entryList.isEmpty())
        {
            return;
        }
        LineDataSet dataSet = new LineDataSet(entryList, city.getBaseData().getCityName());
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.invalidate();
    }

    private ArrayList<Entry> createEntryList(City city)
    {
        List<CoronaData> coronaList = city.getCoronaDataList();
        ArrayList<Entry> result = new ArrayList<>();
        if(coronaList != null && coronaList.size() >= 2)
        {
            for (CoronaData coronaData : coronaList)
            {
                if (coronaData == null)
                {
                    continue;
                }
                result.add(createEntry(coronaData));
            }
        }
        return result;
    }

    private Entry createEntry(CoronaData coronaData)
    {
        return new Entry(0, 10); //TODO Daten vom Objekt entnehmen
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

    protected boolean isOfflineModeEnabled() //TODO notification when switch
    {
        return (findViewById(R.id.ic_offline).getVisibility() == View.VISIBLE);
    }

}