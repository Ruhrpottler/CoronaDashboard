package Database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

import Model.City;

public class FirebaseSvc
{
    private static final String PATH_CITY_DATA = "CoronaData"; //Name der Firebase-"Tabelle"
    private final DatabaseReference db; //root

    public FirebaseSvc()
    {
        db = FirebaseDatabase.getInstance().getReference();
    }

    //TODO hier oder in den aufrufenden Klassen?? Es muss eben auch asynchron sein, weil Cloud-Zugriff
    public interface CityResponseListener
    {
        void onError(String message);
        void onResponse(City city);
    }

    public City getCity(int objectId) //TODO Daten aus der DB holen (können) und nicht immer nur vom RKI ziehen (vor allem Offlinebetrieb!)
    {
        final City[] result = {null};
        //TODO Exception Handling (try catch?)

        //TODO Wie macht man es nun richtig? Query (Video 3) oder get()?
        //TODO see https://firebase.google.com/docs/reference/admin/java/reference/com/google/firebase/database/Query
        Query query = db.child(PATH_CITY_DATA).equalTo(Integer.toString(objectId)).limitToFirst(1);
        //query. //TODO ValueEventListener??

        //vorher: db.child(PATH_CITY_DATA).child(Integer.toString(objectId)).get().addOnCompleteListener
        db.child(PATH_CITY_DATA).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful())
                {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                result[0] =  (City) Objects.requireNonNull(task.getResult()).getValue(); //TODO 'Objects.requireNonNull' kann ggf. weg, wenn Ex-Handling gemacht wurde.
            }
        });
        return result[0];
    }

    /**
     * DOKU: https://firebase.google.com/docs/database/android/read-and-write
     * @param city Java-Objekt, welches in der DB gespeichert wird.
     * Für den Zugriff benötigt die Klasse einen Standard-Konstruktor und public getter
     */
    public void saveCityData(City city) //TODO mehrere Tage speichern
    {
        encodeModel(city);
        db.child(PATH_CITY_DATA).child(Integer.toString(city.getObjectId())).setValue(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("firebase", "City 'objectId " + city.getObjectId()
                + "' successfully stored to the firebase database.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("firebase", "Saving city ' +" + city.getObjectId()
                +"' failed", e);
            }
        });


        //DatabaseReference cityDataRef = db.child("CityData");
        //cityDataRef.child(dataModel.getCityName()).setValue(dataModel); //TODO objectId als primaryKey und die ref von CityData nutzen (stimmt das noch??)
    }

    //TODO Daten aus DB lesen (zumindest wenn offline)

    /**
     * @param cityData Model. Wird als Referenz übergeben => Kein return notwendig, da auf der
     *                 Referenz gearbeitet wird.
     */
    private static void encodeModel(@NonNull City cityData)
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
    private static void decodeModel(@NonNull City cityData)
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
