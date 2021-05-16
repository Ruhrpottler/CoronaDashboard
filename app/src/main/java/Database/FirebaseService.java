package Database;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import Model.CityDataModel;

public class FirebaseService {

    private DatabaseReference db;

    public FirebaseService()
    {
        db = FirebaseDatabase.getInstance().getReference();
    }

    public void saveCityData(CityDataModel dataModel)
    {
        db.child("CityData").child(dataModel.getCityName()).setValue(dataModel);
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
