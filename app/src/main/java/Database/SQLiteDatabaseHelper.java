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
    //Tabellen
    private static final String TABLE_CITY_BASE_DATA = "CITY_STAMMDATEN";
    //Spalten
    private static final String COLUMN_CITY_ID = "OBJECTID";
    private static final String COLUMN_CITY_BL_ID = "bl_id";
    private static final String COLUMN_CITY_BL = "bl";
    private static final String COLUMN_CITY_PRE = "BEZ";
    private static final String COLUMN_CITY_NAME = "GEN";
    private static final String COLUMN_CITY_EWZ = "EWZ";

    public SQLiteDatabaseHelper(@Nullable Context activityContext) //, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version
    {
        super(activityContext, "coronadata.db", null, 1);
    }

    /**
     * Wird aufgerufen, wenn die DB das erste Mal erstellt wird.
     * Hier müssen wir also die Tabellen erstellen usw.
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createTableStatement = "CREATE TABLE " + TABLE_CITY_BASE_DATA + " (" +
                "OBJECTID INTEGER NOT NULL PRIMARY KEY, " +
                "BL_ID INTEGER NOT NULL, " +
                "BL TEXT NOT NULL, " +
                "BEZ TEXT NOT NULL, " +
                "GEN TEXT NOT NULL, " +
                "EWZ INTEGER " +
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
     * @param model
     * @return Gibt an, ob die Ausführung erfolgreich war
     */
    public boolean insertOrUpdateCityBaseDataRow(BaseData model)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues(); //Wie Hashmaps, man kann jz Paare reinpacken (put)

        cv.put(COLUMN_CITY_ID, model.getObjectId());
        cv.put(COLUMN_CITY_BL_ID, model.getBl_id());
        cv.put(COLUMN_CITY_BL, model.getBl());
        cv.put(COLUMN_CITY_PRE, model.getBez());
        cv.put(COLUMN_CITY_NAME, model.getGen());
        cv.put(COLUMN_CITY_EWZ, model.getEwz());

        long success = -1;

        try
        {
            success = db.insertWithOnConflict(TABLE_CITY_BASE_DATA, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            if (success == -1)
                success = db.update(TABLE_CITY_BASE_DATA, cv, "OBJECTID = ?", new String[]{String.valueOf(model.getObjectId())});
        } catch(Exception e)
        {
            String msg = String.format("Fehler beim InsertOrUpdate (SQLite) von '%s': \n %s ", model.getCityName(), e.toString());
            Log.e("DbErr", msg);
        } finally
        {
            db.close();
        }
        return (success != -1);
    }
}
