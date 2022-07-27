package Database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.blackviper.coronadashboard.DataSvc;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import Model.BaseData;
import Model.City;
import Model.CoronaData;
import Tools.DateFormatTool;

public class FirebaseSvc
{
    private static final String LOG_TAG = FirebaseSvc.class.getName();

    private static FirebaseSvc firebaseInstance; //Singleton

    private static final String PATH_CITY_DATA = "CoronaData"; //Name der Firebase-"Tabelle"
    private static final String PATH_BASE_DATA = "BaseData";
    private static final String PATH_MIT_DATUM = "CoronaDataMitDatum";

    private final DatabaseReference db; //root
    private final DatabaseReference coronaDataRef;
    private final DatabaseReference baseDataRef;

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
        coronaDataRef = instance.getReference(PATH_CITY_DATA);
        baseDataRef = instance.getReference(PATH_BASE_DATA);
    }

    //TODO Alle Listener aus den Methoden auslagern (mehrere machen)

    //Read data

    /**
     *
     * @param cityName BEZ + GEN, z.B. "Kreis Recklinghausen"
     * @param responseListener
     */
    public void getObjectIdByName(String cityName, @NonNull DataSvc.ObjectIdResponseListener responseListener)
    {
        Query query = baseDataRef.orderByChild("cityName").equalTo(cityName).limitToFirst(1); //TODO Pfad umstellen?!
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                GenericTypeIndicator<HashMap<String, BaseData>> indicator =
                        new GenericTypeIndicator<HashMap<String, BaseData>>() {};
                try
                {
                    HashMap<String, BaseData> map = snapshot.getValue(indicator);
                    if (map != null && map.keySet().size() == 1) //Obsolet, weil limitToFirst?
                    {
                        String key = map.keySet().toArray()[0].toString();
                        BaseData baseData = map.get(key);
                        if (baseData == null || baseData.getCityName() == null || !baseData.getCityName().equals(cityName))
                        {
                            responseListener.onError(buildMsgGetId(cityName) + ": Attribute 'cityName' is null or empty or " +
                                    "does not equal the cityName what was queried for.");
                            return;
                        }
                        int objectId = baseData.getObjectId();
                        Log.i(LOG_TAG, "The objectId of '" + cityName + "' is '" + objectId + "'.");
                        responseListener.onResponse(objectId);
                        return;
                    }
                }
                catch(DatabaseException e)
                {
                    onCancelled(DatabaseError.fromException(e));
                    return;
                }
                responseListener.onError(buildMsgGetId(cityName));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                responseListener.onError(buildMsgGetId(cityName) + "\n" + error.getMessage());
            }
        });
    }

    public void getCity(int objectId, @NonNull DataSvc.CityResponseListener responseListener)
    {
        String objectIdStr = Integer.toString(objectId);
        Task<DataSnapshot> taskBaseData = db.child(PATH_MIT_DATUM).child(objectIdStr).child(PATH_BASE_DATA).get();

        //Task<DataSnapshot> taskCoronaData = db.child(PATH_MIT_DATUM).child(objectIdStr).orderByChild("CoronaData").limitToFirst(1).get();
        //Path p = db.child(PATH_MIT_DATUM).child(objectIdStr).child("CoronaData").orderByKey().limitToLast(1).get();

        Task<DataSnapshot> taskCoronaData =
                db.child(PATH_MIT_DATUM).child(objectIdStr).child("CoronaData").orderByKey().limitToLast(1).get();

        //Task<DataSnapshot> taskCoronaData = db.child(PATH_MIT_DATUM).child(objectIdStr).child("CoronaData").child("220727").get(); //TODO hardcoded
        Task<Void> syncedTasks = Tasks.whenAll(taskBaseData, taskCoronaData);
        syncedTasks.addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                City city = getCityFromTasks(taskBaseData, taskCoronaData);
                if(city != null)
                {
                    responseListener.onResponse(city);
                }
                else
                {
                    responseListener.onError("Daten von City " + objectIdStr
                            + " konnten nicht aus der lokalen Firebase-Datenbank gelesen werden.");
                }
            }
        });
        syncedTasks.addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                responseListener.onError("Daten von City " + objectIdStr
                        + " konnten nicht aus der lokalen Firebase-Datenbank gelesen werden.");
            }
        });
    }

    private City getCityFromTasks(Task<DataSnapshot> taskBaseData, Task<DataSnapshot> taskCoronaData)
    {
        BaseData baseData = null;
        CoronaData coronaData = null;
        if (taskBaseData != null && taskBaseData.isSuccessful()
                && taskBaseData.getResult() != null && taskBaseData.getResult().exists())
        {
            DataSnapshot dataSnapshot = taskBaseData.getResult();
            baseData = dataSnapshot.getValue(BaseData.class);
        }

        if (taskCoronaData != null && taskCoronaData.isSuccessful()
                && taskCoronaData.getResult() != null && taskCoronaData.getResult().exists())
        {
//            DataSnapshot dataSnapshot = taskCoronaData.getResult();
//            coronaData = dataSnapshot.getValue(CoronaData.class);
            DataSnapshot dataSnapshot = taskCoronaData.getResult();
            GenericTypeIndicator<Map<String, CoronaData>> genericTypeIndicator =
                    new GenericTypeIndicator<Map<String, CoronaData>>() {};
            Map<String, CoronaData> map = dataSnapshot.getValue(genericTypeIndicator);

//            Optional<String> firstKey = map.keySet().stream().findFirst();
//            String key;
//            if (!firstKey.isPresent()) {
//                 return null;
//            }
//            key = firstKey.get();
            String key = "220727";
            coronaData = map.get(key); //TODO hardcoded
        }

        City city;
        if(baseData != null && coronaData != null)
        {
            city = new City(baseData, coronaData);
        }
        else
        {
            city = null;
        }
        return city;
    }

    public void getAllBaseData(@NonNull DataSvc.BaseDataListResponseListener responseListener)
    {
        baseDataRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists())
                {
                    ArrayList<BaseData> baseDataList = null;
                    try
                    {
                        DataSnapshot dataSnapshot = task.getResult();
                        GenericTypeIndicator<ArrayList<BaseData>> genericTypeIndicator =
                                new GenericTypeIndicator<ArrayList<BaseData>>() {};
                        baseDataList = dataSnapshot.getValue(genericTypeIndicator);
                    } catch (Exception e)
                    {
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    if(baseDataList != null && !baseDataList.isEmpty())
                    {
                        responseListener.onResponse(baseDataList);
                        return;
                    }
                }
                responseListener.onError("Stammdaten konnten nicht aus der Firebase-Datenbank gelesen werden");
            }
        });
    }

    //Save data
    //Objects which will be stored need a default constructor and public getters

    public void saveCityData(City city)
    {
        if(city == null)
        {
            Log.i(LOG_TAG, "city is null, abord saving");
            return;
        }

        saveBaseData(city.getBaseData());
        saveCoronaData(city.getCoronaData());
    }

    public void saveCoronaData(CoronaData coronaData)
    {
        if(coronaData == null)
        {
            return;
        }

        String objectIdStr = Integer.toString(coronaData.getObjectId());
        String key = DateFormatTool.germanToSort(coronaData.getLast_update());

        db.child(PATH_MIT_DATUM).child(objectIdStr).child("CoronaData").child(key).setValue(coronaData).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                Log.d(LOG_TAG, "CoronaData with id '" + objectIdStr + "' successfully saved."); //TODO add date to log
            }
        });
    }

    public void saveBaseData(BaseData baseData)
    {

        if(baseData == null)
        {
            return;
        }

        String objectIdStr = Integer.toString(baseData.getObjectId());
        db.child(PATH_MIT_DATUM).child(objectIdStr).child("BaseData").setValue(baseData).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Log.d(LOG_TAG, String.format("BaseData with objectId '%s' successfully " +
                        "stored to the firebase database.", objectIdStr));
            }
        });
    }

    /**
     * Saves the data in the path where the list of baseData will be stored when the
     * app starts.
     * @param baseData
     */
    public void saveBaseDataInMapPath(BaseData baseData)
    {
        if(baseData == null)
        {
            return;
        }

        int objectId = baseData.getObjectId();

        baseDataRef.child(Integer.toString(objectId)).setValue(baseData).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                Log.d(LOG_TAG, String.format("BaseData with objectId '%s' successfully " +
                        "stored to the firebase database.", objectId));
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.e(LOG_TAG, String.format("BaseData with objectId '%s' could not be" +
                        " stored to the firebase database.", objectId));
            }
        });
    }

    /**
     * Overwrites the existing data in the path!
     * @param list
     */
    public void saveBaseDataList(List<BaseData> list)
    {
        if(list == null || list.isEmpty())
        {
            return;
        }
        HashMap<String, BaseData> map = new HashMap<>();
        for(BaseData baseData : list)
        {
            if(baseData == null)
            {
                continue;
            }
            encodeBaseData(baseData);
            map.put(Integer.toString(baseData.getObjectId()), baseData);
        }

        baseDataRef.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                Log.d(LOG_TAG, "baseDataList successfully stored to the firebase database.");
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.e(LOG_TAG, "Saving baseDataList failed.", e);
            }
        });
    }

    //Other

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

    private String buildMsgGetId(String cityName) //TODO wenn res nutzen
    {
        return "Beim Versuch, die objectId für '" + cityName
                + "' herauszufinden, ist ein Fehler aufgetreten";
    }
}