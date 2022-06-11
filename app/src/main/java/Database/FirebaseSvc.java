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

    private final DatabaseReference db; //root

    public FirebaseSvc()
    {
        db = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * DOKU: https://firebase.google.com/docs/database/android/read-and-write
     * @param dataModel Java-Objekt, welches in der DB gespeichert wird.
     * Für den Zugriff benötigt die Klasse einen Standard-Konstruktor und public getter
     */
    public void saveCityData(CityDataModel dataModel) //TODO mehrere Tage speichern
    {
        encodeModel(dataModel);
        db.child("CityData").child(dataModel.getCityName()).setValue(dataModel);

        //DatabaseReference cityDataRef = db.child("CityData");
        //cityDataRef.child(dataModel.getCityName()).setValue(dataModel); //TODO objectId als primaryKey und die ref von CityData nutzen.
    }

    //TODO Daten aus DB lesen (zumindest wenn offline)

    /**
     * @param cityData Model. Wird als Referenz übergeben => Kein return notwendig, da auf der
     *                 Referenz gearbeitet wird.
     */
    private static void encodeModel(@NonNull CityDataModel cityData)
    {
        if(cityData.getBaseData().getGen().contains("_") || cityData.getBaseData().getGen().contains("."))
        {
            cityData.getBaseData().setGen(encode(cityData.getBaseData().getGen()));
        }
    }

    @NonNull
    private static String encode(@NonNull String str)
    {
        return str
                .replace("_", "__")
                .replace(".", "_P");
    }

    /**
     * @param cityData Model. Wird als Referenz übergeben => Kein return notwendig, da auf der
     *                 Referenz gearbeitet wird.
     */
    private static void decodeModel(@NonNull CityDataModel cityData)
    {
        if(cityData.getBaseData().getGen().contains("__") || cityData.getBaseData().getGen().contains("_P"))
        {
            cityData.getBaseData().setGen(decode(cityData.getBaseData().getGen()));
        }
    }

    @NonNull
    private static String decode(@NonNull String str)
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
