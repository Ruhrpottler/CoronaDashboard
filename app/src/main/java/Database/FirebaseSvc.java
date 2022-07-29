package Database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.blackviper.coronadashboard.ResponseListener.BaseDataListResponseListener;
import com.blackviper.coronadashboard.ResponseListener.CityResponseListener;
import com.blackviper.coronadashboard.ResponseListener.FirebaseResponseListener;
import com.blackviper.coronadashboard.ResponseListener.ObjectIdResponseListener;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Comparator.LastUpdateComparator;
import Model.BaseData;
import Model.City;
import Model.CoronaData;
import Tools.DateFormatTool;

public class FirebaseSvc
{
    private static final String LOG_TAG = FirebaseSvc.class.getName();

    private static final String PATH_CITY_DATA = "CoronaDataMitDatum"; //Name des Pfades //TODO rename
    private static final String PATH_BASE_DATA = "BaseData";

    private static FirebaseSvc firebaseInstance;
    private final DatabaseReference db; //root
    private final DatabaseReference cityDataRef;
    private final DatabaseReference baseDataRef;

    /**
     * Singleton. Get the instance if it exists. Otherwise, creates a new instance.
     */
    public static synchronized FirebaseSvc getFirebaseInstance()
    {
        if(firebaseInstance == null)
        {
            firebaseInstance = new FirebaseSvc();
        }
        return firebaseInstance;
    }

    private FirebaseSvc()
    {
        FirebaseDatabase dbInstance = FirebaseDatabase.getInstance();
        dbInstance.setPersistenceEnabled(true); // Store data local, even beyond an android restart
        db = dbInstance.getReference();
        cityDataRef = dbInstance.getReference(PATH_CITY_DATA);
        baseDataRef = dbInstance.getReference(PATH_BASE_DATA);
    }

    //TODO Alle Listener aus den Methoden auslagern (mehrere machen)

    //Read data

    /**
     * Find objectId with the database.
     * @param cityName BEZ + GEN, z.B. "Kreis Recklinghausen"
     * @param responseListener
     */
    public void getObjectIdByName(String cityName, @NonNull ObjectIdResponseListener responseListener)
    {
        String cityNameEncoded = encodeString(cityName);
        Query query = baseDataRef.orderByChild("cityName").equalTo(cityNameEncoded).limitToFirst(1); //TODO Pfad umstellen?!

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
                        if (baseData == null || baseData.getCityName() == null || !baseData.getCityName().equals(cityNameEncoded))
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
                responseListener.onError(buildMsgGetId(cityNameEncoded));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                responseListener.onError(buildMsgGetId(cityNameEncoded) + "\n" + error.getMessage());
            }
        });
    }

    public void getCity(int objectId, @NonNull CityResponseListener responseListener)
    {
        String objectIdStr = Integer.toString(objectId);
        Task<DataSnapshot> taskBaseData = cityDataRef
                .child(objectIdStr)
                .child(PATH_BASE_DATA)
                .get();
        Task<DataSnapshot> taskCoronaData = cityDataRef
                .child(objectIdStr)
                .child("CoronaData")
                .orderByKey()
                .limitToFirst(1)
                .get();

        Task<Void> syncedTasks = Tasks.whenAll(taskBaseData, taskCoronaData)
                .addOnSuccessListener(new OnSuccessListener<Void>()
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
        }).addOnFailureListener(new OnFailureListener()
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
        List<CoronaData> coronaDataList = null;
        if (taskBaseData != null && taskBaseData.isSuccessful()
                && taskBaseData.getResult() != null && taskBaseData.getResult().exists())
        {
            DataSnapshot dataSnapshot = taskBaseData.getResult();
            baseData = dataSnapshot.getValue(BaseData.class);
        }

        if (taskCoronaData != null && taskCoronaData.isSuccessful()
                && taskCoronaData.getResult() != null && taskCoronaData.getResult().exists())
        {
            DataSnapshot dataSnapshot = taskCoronaData.getResult();
            GenericTypeIndicator<HashMap<String, CoronaData>> genericTypeIndicator =
                    new GenericTypeIndicator<HashMap<String, CoronaData>>() {};
            HashMap<String, CoronaData> map = dataSnapshot.getValue(genericTypeIndicator);
            Comparator<CoronaData> cmp = new LastUpdateComparator().reversed();
            coronaDataList = getCoronaDataListFromMap(map, cmp);
        }

        City city;
        if(baseData != null && coronaDataList != null)
        {
            city = new City(baseData, coronaDataList);
            decodeCity(city);
        }
        else
        {
            city = null;
        }
        return city;
    }

//    private CoronaData getFirstCoronaDataFromMap(Map<String, CoronaData> map, Comparator<CoronaData> cmp)
//    {
//        List<CoronaData> list = getCoronaDataListFromMap(map, cmp);
//        if(list == null || list.isEmpty())
//        {
//            return null;
//        }
//        return list.get(0);
//    }

    private List<CoronaData> getCoronaDataListFromMap(Map<String, CoronaData> map, Comparator<CoronaData> cmp)
    {
        List<CoronaData> list = new ArrayList<CoronaData>();
        if(map == null || map.isEmpty())
        {
            return list;
        }

        list.addAll(map.values());
        list.sort(cmp);
        return list;
    }

    public void getAllBaseData(@NonNull BaseDataListResponseListener responseListener)
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
                        for(BaseData element : baseDataList)
                        {
                            if(element == null) //First object will be null because list starts with objectId=1
                            {
                                continue;
                            }
                            decodeBaseData(element);
                        }
                        responseListener.onResponse(baseDataList);
                        return;
                    }
                }
                responseListener.onError("Stammdaten konnten nicht aus der Firebase-Datenbank gelesen werden.");
            }
        });
    }

    //Save data
    //Objects which will be stored need a default constructor and public getters

    public void saveCityData(City city) //TODO responseListener
    {
        if(city == null)
        {
            Log.i(LOG_TAG, "city is null, abord saving");
            return;
        }
        saveBaseData(city.getBaseData()); //TODO eig. nicht mehr notwendig

        List<CoronaData> coronaDataList = city.getCoronaDataList();
        if(coronaDataList != null && !coronaDataList.isEmpty())
        {
            //saveCoronaData(city.getCoronaDataList()); //Es wird vorerst nicht notwendig sein, mehrere zu speichern, weil ich diese
            //immer nur von der DB selbst und nicht der API beziehen kann.
            saveCoronaData(city.getNewestCoronaData());
        }

    }

    /**
     * Iterates over the list and saves each element with a own task.
     */
    public void saveCoronaData(List<CoronaData> list)
    {
        if(list == null)
        {
            return;
        }
        for(CoronaData data : list)
        {
            saveCoronaData(data);
        }
    }

    /**
     * Adds the element to the firebase database. It will not overwrite other elements from another day.
     * @param coronaData
     */
    public void saveCoronaData(CoronaData coronaData)
    {
        if(coronaData == null)
        {
            return;
        }

        String objectIdStr = Integer.toString(coronaData.getObjectId());
        String key = DateFormatTool.germanToSort(coronaData.getLast_update());

        cityDataRef.child(objectIdStr).child("CoronaData").child(key).setValue(coronaData).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                Log.d(LOG_TAG, "CoronaData with id '" + objectIdStr + "' successfully saved."); //TODO add date to log
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.e(LOG_TAG, "CoronaData with id '" + objectIdStr + "' could not be stored:\n"
                        + e.getMessage());
            }
        });
    }

    public void saveCityList(List<City> cities, FirebaseResponseListener responseListener)
    {
        if(cities == null || cities.isEmpty())
        {
            return;
        }

        for(City city : cities)
        {
            saveCityData(city);
        }
        responseListener.onResponse();
    }

    public void saveBaseData(BaseData baseData)
    {

        if(baseData == null)
        {
            return;
        }
        encodeBaseData(baseData);
        String objectIdStr = Integer.toString(baseData.getObjectId());
        cityDataRef.child(objectIdStr).child("BaseData").setValue(baseData).addOnCompleteListener(new OnCompleteListener<Void>()
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
            if(baseData == null) //First object will be null because list starts with objectId=1
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

    // Encode / Decode special character

    /** Encode data which will be stored to the firebase database.
     * @param city Changes will be done by reference => no return nedded
     */
    private static void encodeCity(City city)
    {
        if(city != null && city.getBaseData() != null)
        {
            encodeBaseData(city.getBaseData());
        }
    }

    /** Encode data which will be stored to the firebase database.
     * @param baseData Changes will be done by reference => no return nedded
     */
    private static void encodeBaseData(BaseData baseData)
    {
        if(baseData != null && (baseData.getGen().contains("_") || baseData.getGen().contains(".")))
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

    /** Decode Data which was loaded from the database
     * @param city Changes will be done by reference => no return nedded
     */
    private static void decodeCity(City city)
    {
        if(city != null && city.getBaseData() != null)
        {
            decodeBaseData(city.getBaseData());
        }
    }

    /** Decode Data which was loaded from the database
     * @param baseData Es wird auf der Referenz gearbeitet => Kein return notwendig
     */
    private static void decodeBaseData(BaseData baseData)
    {
        if(baseData != null && (baseData.getGen().contains("__") || baseData.getGen().contains("_P")))
        {
            baseData.setGen(decodeString(baseData.getGen()));
        }
    }

    private static String decodeString(String str)
    {
        return str
                .replace("_P", ".")
                .replace("_", "__");
    }

    // Other

    private String buildMsgGetId(String cityName)
    {
        return "Beim Versuch, die objectId für '" + cityName
                + "' herauszufinden, ist ein Fehler aufgetreten";
    }
}