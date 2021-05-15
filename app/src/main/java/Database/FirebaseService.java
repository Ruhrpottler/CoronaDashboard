package Database;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FirebaseService {

    private DatabaseReference db;

    public FirebaseService()
    {
        db = FirebaseDatabase.getInstance().getReference();
    }

    public void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        try{
            db.child("users").child(userId).setValue(user);
        } catch(Exception ex)
        {
            String msg = ex.getMessage();
        }

    }

    public void addTestData()
    {
        FirebaseDatabase.getInstance().getReference("message").setValue("Message vom Studio");

        HashMap<String, Object> map = new HashMap<>();
        map.put("Vorname", "Jan");
        map.put("Nachname", "Lappenküper");
        map.put("Hochschule", "Fachhochschule Dortmund");

        db.child("users").child("abc").updateChildren(map);

    }

}
