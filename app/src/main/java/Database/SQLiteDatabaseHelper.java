package Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import Model.BaseData;

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
    private static final String COLUMN_OBJECT_ID = "OBJECTID";
    private static final String COLUMN_CITY_BL_ID = "BL_ID";
    private static final String COLUMN_CITY_BL = "BL";
    private static final String COLUMN_CITY_BEZ = "BEZ";
    private static final String COLUMN_CITY_NAME = "GEN";
    private static final String COLUMN_CITY_EWZ = "EWZ";

    public SQLiteDatabaseHelper(@Nullable Context activityContext) //, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version
    {
        super(activityContext, db_name, null, 1);
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
            db.close();
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
