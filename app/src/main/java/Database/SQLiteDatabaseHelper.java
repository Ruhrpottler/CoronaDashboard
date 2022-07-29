package Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;

import Model.BaseData;
import Tools.Constants;

/**
 * Zum Speichern der City-Stammdaten auf dem Gerät, um die Vorschläge bei Eingabe der City-Namen zu ermöglichen.
 * Dokumentation: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 */
public class SQLiteDatabaseHelper extends SQLiteOpenHelper
{
    private static final String db_name = "basedata.db";

    //Tabellen
    private static final String TABLE_CITY_BASE_DATA = "CITY_STAMMDATEN";
    //Spalten
    private static final String COLUMN_OBJECT_ID = Constants.STR_OBJECT_ID;
    private static final String COLUMN_CITY_BL_ID = Constants.STR_BL_ID;
    private static final String COLUMN_CITY_BL = Constants.STR_BL;
    private static final String COLUMN_CITY_BEZ = Constants.STR_BEZ;
    private static final String COLUMN_CITY_NAME = Constants.STR_GEN;
    private static final String COLUMN_CITY_EWZ = Constants.STR_EWZ;

    public SQLiteDatabaseHelper(@Nullable Context activityContext)
    {
        super(activityContext, db_name, null, 1);
    }

    private void fillListOfEntriesAndSaveSQLite(List<BaseData> baseDataList, List<String> listOfEntries)
    {
        boolean success;
        int i = -1;
        for(BaseData dataElement : baseDataList)
        {
            i++;
            if(dataElement == null) //Beim Lesen aus Firebase kann es passieren, dass [0] in der ArrayList null ist. Auch weitere, warum?? //TODO
            {
                Log.i("SQLite", "Element i=" + i + " is missing.");
                continue;
            }
            listOfEntries.add(dataElement.getCityName());
            insertOrUpdateCityBaseDataRow(dataElement);
        }

        Log.d("SQLite", "The basedata of all cities was stored in the SQLite database.");
    }

    /**
     * Wird aufgerufen, wenn die DB das erste Mal erstellt wird.
     * Hier müssen wir also die Tabellen erstellen usw.
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createTableStatement = "CREATE TABLE " + TABLE_CITY_BASE_DATA + " (" + COLUMN_OBJECT_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                COLUMN_CITY_BL_ID + " INTEGER NOT NULL, " +
                COLUMN_CITY_BL + " TEXT NOT NULL, " +
                COLUMN_CITY_BEZ + " TEXT NOT NULL, " +
                COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                COLUMN_CITY_EWZ + " INTEGER " +
                ")";

        db.execSQL(createTableStatement);
        //TODO preparedStatemend nutzen (performance)
        //TODO Tabelle CITY_DATEN erstellen und immer speichern, falls man kein Netz hat
    }

    /**
     * Wird aufgerufen, wenn man etwas am DB-Schema ändert
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //TODO
    }

    /**
     * Auto-Inkrement Werte (meist PK) müssen dank der ContentValues nicht hinzugefügt werden.
     * @param baseData
     * @return Gibt an, ob die Ausführung erfolgreich war
     */
    public boolean insertOrUpdateCityBaseDataRow(BaseData baseData)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = fillContentValues(baseData);
        long success = -1;

        try
        {
            Log.d("SQLite", "Try inserting object '" + baseData.getObjectId() +  "' into table " + TABLE_CITY_BASE_DATA);
            success = db.insertWithOnConflict(TABLE_CITY_BASE_DATA, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            if (success == -1) //Failed
            {
                Log.d("SQLite", "Try updating table " + TABLE_CITY_BASE_DATA + " after insert failed");
                success = db.update(TABLE_CITY_BASE_DATA, cv, COLUMN_OBJECT_ID + " = ?", new String[]{String.valueOf(baseData.getObjectId())});
            }
        } catch(Exception e)
        {
            String msg = String.format("Fehler beim InsertOrUpdate (SQLite) von '%s': \n %s ", baseData.getCityName(), e.toString());
            Log.e("SQLite", msg);
        } finally
        {
            Log.d("SQLite", "Successfully inserted or updated data (into) table " + TABLE_CITY_BASE_DATA);
            db.close(); //TODO Die DB wird zu beginn über 400 mal geschlossen in 2 Sekunden, das frisst viel Zeit
        }
        return (success != -1);
    }

    private ContentValues fillContentValues(BaseData baseData)
    {
        ContentValues cv = new ContentValues(); //Map

        cv.put(COLUMN_OBJECT_ID, baseData.getObjectId());
        cv.put(COLUMN_CITY_BL_ID, baseData.getBl_id());
        cv.put(COLUMN_CITY_BL, baseData.getBl());
        cv.put(COLUMN_CITY_BEZ, baseData.getBez());
        cv.put(COLUMN_CITY_NAME, baseData.getGen());
        cv.put(COLUMN_CITY_EWZ, baseData.getEwz());

        return cv;
    }
}
