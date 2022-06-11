package Database;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import Model.CityDataModel;

public class FirebaseSvc
{

    private DatabaseReference db;

    public FirebaseSvc()
    {
        db = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * DOKU: https://firebase.google.com/docs/database/android/read-and-write
     * @param dataModel Java-Objekt, welches in der DB gespeichert wird.
     * Für den Zugriff benötigt die Klasse einen Standard-Konstruktor und public getter
     */
    public void saveCityData(CityDataModel dataModel)
    {
        dataModel = encodeModel(dataModel);
        db.child("CityData").child(dataModel.getCityName()).setValue(dataModel);
        //DatabaseReference cityDataRef = db.child("CityData");
        //cityDataRef.child(dataModel.getCityName()).setValue(dataModel); //TODO objectId als primaryKey und die ref von CityData nutzen.
    }

    private static CityDataModel encodeModel(CityDataModel cityData) //TODO model clonen ?
    {
        if(cityData.getBaseData().getGen().contains("_") || cityData.getBaseData().getGen().contains("."))
        {
            cityData.getBaseData().setGen(encode(cityData.getBaseData().getGen()));
        }
        return cityData;
    }

    private static String encode(String str)
    {
        return str
                .replace("_", "__")
                .replace(".", "_P");
    }

    private static CityDataModel decodeModel(CityDataModel cityData)
    {
        if(cityData.getBaseData().getGen().contains("__") || cityData.getBaseData().getGen().contains("_P"))
        {
            cityData.getBaseData().setGen(decode(cityData.getBaseData().getGen()));
        }
        return cityData;
    }

    private static String decode(String str)
    {
        return str
                .replace("_P", ".")
                .replace("_", "__");
    }

//TODO remove

//    public void addTestData()
//    {
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("Vorname", "Jan");
//        map.put("Nachname", "Lappenküper");
//        map.put("Hochschule", "Fachhochschule Dortmund");
//
//        db.child("Personen").child("me").updateChildren(map);
//
//    }

}
