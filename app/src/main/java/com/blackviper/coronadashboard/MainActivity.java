package com.blackviper.coronadashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import Model.CityDataModel;

public class MainActivity extends AppCompatActivity {

    Button btn_sendRequest;
    ListView lv;
    TextView tvErgebnisse;
    AutoCompleteTextView actv_city;
    final DataService dataService = new DataService(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sendRequest = (Button) findViewById(R.id.btn_sendRequest);
        lv = (ListView) findViewById(R.id.lv_responseView);

        //AutoComplete
        actv_city = (AutoCompleteTextView) findViewById(R.id.actv_Landkreis);
        String[] listOfEntries = new String[] {"Recklinghausen", "Bochum", "Dortmund", "Herne", "Mülheim an der Ruhr"}; //TODO
        actv_city.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listOfEntries));

        tvErgebnisse = (TextView) findViewById(R.id.tvErgebnisse);

        btn_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userInputCityName = getCityInput();
                if(userInputCityName.isEmpty())
                {
                    showToastTextLong(getString(R.string.err_actv_cityName_empty));
                    return;
                }

                closeKeyboard();

                dataService.getCityDataByName(userInputCityName, new DataService.CityDataModelResponseListener() {
                    @Override
                    public void onError(String message) {
                        Log.e("ErrCallbackMainActivity", message);
                        showToastTextLong(message);
                    }

                    @Override
                    public void onResponse(CityDataModel cityDataModel) {
                        showToastTextShort("7-Tage-Inzidenzwert: " + cityDataModel.getCases7_per_100k_txt());
                        tvErgebnisse.setText(cityDataModel.toString());
                    }
                });

            }
        });
    }

    public void closeKeyboard()
    {
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
            //oder man returned direkt
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
}