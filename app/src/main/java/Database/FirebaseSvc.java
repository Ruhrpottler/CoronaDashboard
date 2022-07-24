package Database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.blackviper.coronadashboard.DataSvc;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.BaseData;
import Model.City;

public class FirebaseSvc
{
    private static FirebaseSvc firebaseInstance; //Singleton
    private static final String PATH_CITY_DATA = "CoronaData"; //Name der Firebase-"Tabelle"
    private final DatabaseReference db; //root
    private final DatabaseReference coronaDataPath;

    public static FirebaseSvc getFirebaseInstance() //Singleton
    {
        if(firebaseInstance == null)
        {
            firebaseInstance = new FirebaseSvc();
        }
        return firebaseInstance;
    }

    private FirebaseSvc()
    {
        FirebaseDatabase instance = FirebaseDatabase.getInstance(); //TODO variable umbenennen weil doppeldeutig
        instance.setPersistenceEnabled(true); // Daten offline speichern, auch bei Neustart etc, see https://firebase.google.com/docs/database/android/offline-capabilities
        db = instance.getReference();
        coronaDataPath = instance.getReference(PATH_CITY_DATA);
    }

    public void getCityIdByName(String cityName, DataSvc.CityIdResponseListener responseListener)
    {
        //TODO query: In Firebase die Stammdaten durchsuchen
        int cityId = 65; //TODO hardcoded, remove!

        responseListener.onResponse(cityId);
        return;

        //responseListener.onError(msg);

    }

    public void getCity(int objectId, @NonNull DataSvc.CityResponseListener responseListener)
    {
        String objectIdStr = Integer.toString(objectId);
        //TODO Exception Handling (try catch?)

        coronaDataPath.child(objectIdStr).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if (task.isSuccessful() && task.getResult().exists())
                {
                    DataSnapshot dataSnapshot = task.getResult();
                    City city = dataSnapshot.getValue(City.class);
                    if(city != null)
                    {
                        responseListener.onResponse(city);
                        return;
                    }
                }
                String msg = "Daten von City " + objectIdStr + " konnten nicht aus der Firebase-Datenbank gelesen werden.";
                Log.e("Firebase", msg);
                responseListener.onError(msg);
            }
        });
    }

    /**
     * DOKU: https://firebase.google.com/docs/database/android/read-and-write
     * @param city Java-Objekt, welches in der DB gespeichert wird.
     * Für den Zugriff benötigt die Klasse einen Standard-Konstruktor und public getter
     */
    public void saveCityData(@NonNull City city) //TODO mehrere Tage speichern
    {
        String objectIdStr = Integer.toString(city.getObjectId());
        encodeCity(city);
        db.child(PATH_CITY_DATA).child(objectIdStr).setValue(city)
                .addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                Log.d("firebase", "City 'objectId " + objectIdStr
                + "' successfully stored to the firebase database.");
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.e("firebase", "Saving city ' +" + objectIdStr
                +"' failed", e);
            }
        });
    }

    public void saveBaseData(@NonNull BaseData baseData)
    {
        String objectIdStr = Integer.toString(baseData.getObjectId());
        encodeBaseData(baseData);
        db.child("BaseData").child(objectIdStr).setValue(baseData)
                .addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                Log.d("firebase", "baseData 'objectId " + objectIdStr
                        + "' successfully stored to the firebase database.");
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.e("firebase", "Saving baseData ' +" + objectIdStr
                        +"' failed", e);
            }
        });
    }

    public void saveBaseDataList(@NonNull List<BaseData> baseDataList)
    {
        HashMap<String, BaseData> map = new HashMap<>();
        for(BaseData baseData : baseDataList)
        {
            if(baseData == null)
            {
                continue;
            }
            encodeBaseData(baseData);
            map.put(Integer.toString(baseData.getObjectId()), baseData);
        }

        db.child("BaseDataHashMap").setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                Log.d("firebase", "baseDataList successfully stored to the firebase database.");
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.e("firebase", "Saving baseDataList failed.", e);
            }
        });
    }

//    public void saveAllBaseData(List<BaseData> baseDataList, List<String> listOfEntries)
//    {
//        for(BaseData baseData : baseDataList)
//        {
//            saveBaseData(baseData);
//            listOfEntries.add(baseData.getCityName());
//
//            Log.d("DataSvc", "The basedata of all german cities was stored in the SQLite database.");
//        }
//    }

    public void getAllBaseData(DataSvc.BaseDataResponseListener responseListener)
    {
        db.child("BaseDataHashMap").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if (task.isSuccessful() && task.getResult().exists())
                {
                    ArrayList<BaseData> baseDataList = null;
                    try
                    {
                        DataSnapshot dataSnapshot = task.getResult();
                        GenericTypeIndicator<ArrayList<BaseData>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<BaseData>>() {};
                        baseDataList = dataSnapshot.getValue(genericTypeIndicator); //TODO dann kann man die Daten doch auch so speichern oder?
                    } catch (Exception e)
                    {
                      Log.e("Firebase", e.getMessage());
                    }

                    if(baseDataList != null && !baseDataList.isEmpty())
                    {
                        responseListener.onResponse(baseDataList);
                        return;
                    }

                    String msg = "Stammdaten konnten nicht aus der Firebase-Datenbank gelesen werden";
                    responseListener.onError(msg);
                }
            }
        });
    }



    /**
     * @param cityData Model. Wird als Referenz übergeben => Kein return notwendig, da auf der
     *                 Referenz gearbeitet wird.
     */
    private static void encodeCity(@NonNull City cityData)
    {
        encodeBaseData(cityData.getBaseData());
    }

    private static void encodeBaseData(@NonNull BaseData baseData)
    {
        if(baseData.getGen().contains("_") || baseData.getGen().contains("."))
        {
            baseData.setGen(encodeString(baseData.getGen()));
        }
    }

    @NonNull
    private static String encodeString(@NonNull String str)
    {
        return str
                .replace("_", "__")
                .replace(".", "_P");
    }

    /**
     * @param cityData Model. Wird als Referenz übergeben => Kein return notwendig, da auf der
     *                 Referenz gearbeitet wird.
     */
    private static void decodeCity(@NonNull City cityData)
    {
        decodeBaseData(cityData.getBaseData());
    }

    private static void decodeBaseData(@NonNull BaseData baseData)
    {
        if(baseData.getGen().contains("__") || baseData.getGen().contains("_P"))
        {
            baseData.setGen(decodeString(baseData.getGen()));
        }
    }

    @NonNull
    private static String decodeString(@NonNull String str)
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
