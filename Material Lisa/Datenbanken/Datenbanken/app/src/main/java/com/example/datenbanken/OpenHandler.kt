package com.example.datenbanken

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class OpenHandler (context : Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Name und Version der Datenbank
        private val DATABASE_NAME = "mood.db"
        private val DATABASE_VERSION = 1
        // Konstanten für die Stimmungen
        val MOOD_FINE = 1
        val MOOD_OK = 2
        val MOOD_BAD = 3

        val MOOD_MOOD = "mood"
        val MOOD_TIME = "timeMillis"
    }

    // Name und Attribute der Tabelle "mood"
    private val _ID = "_id"
    private val TABLE_NAME_MOOD = "mood"

    private val TAG = OpenHandler::class.java.simpleName

    // Tabelle mood anlegen
    private val TABLE_MOOD_CREATE = ("CREATE TABLE "
            + TABLE_NAME_MOOD + " (" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MOOD_TIME
            + " INTEGER, " + MOOD_MOOD + " INTEGER);")

    // Tabelle mood löschen
    private val TABLE_MOOD_DROP = "DROP TABLE IF EXISTS $TABLE_NAME_MOOD"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_MOOD_CREATE)
    }

    override fun onUpgrade(
        db: SQLiteDatabase, oldVersion: Int,
        newVersion: Int
    ) {
        Log.w(
            TAG, "Upgrade der Datenbank von Version "
                    + oldVersion + " zu "
                    + newVersion + "; alle Daten werden gelöscht"
        )
        db.execSQL(TABLE_MOOD_DROP)
        onCreate(db)
    }

    fun insert(mood: Int, timeMillis: Long) {
        var rowId: Long = -1
        try {
            // Datenbank öffnen
            val db: SQLiteDatabase = getWritableDatabase()
            Log.d(TAG, "Pfad: " + db.path)
            // die zu speichernden Werte
            val values = ContentValues()
            values.put(MOOD_MOOD, mood)
            //values.put(MOOD_MOOD, mood + "); DROP TABLE 'mood';");
            values.put(MOOD_TIME, timeMillis)
            // in die Tabelle mood einfügen
            rowId = db.insert(TABLE_NAME_MOOD, null, values)
            /*
            db.execSQL("INSERT INTO 'mood' (timeMillis, mood)" + "VALUES (" + timeMillis + ", " + mood + ")");
            String [] columns = new String[] {"max(_ID)"};
            Cursor c = db.query( "mood", columns, null, null, null, null, null);
            c.moveToFirst();
            rowId = c.getInt(0);
            */
        } catch (e: SQLiteException) {
            Log.e(TAG, "insert()", e)
        } finally {
            Log.d(TAG, "insert(): rowId=$rowId")
        }
    }

    fun query(): Cursor? {
        val db: SQLiteDatabase = getReadableDatabase()
        return db.query(
            TABLE_NAME_MOOD,
            null, null, null,
            null, null,
            "$MOOD_TIME DESC"
        )
    }

    fun update(id: Long, smiley: Int) {
        val db: SQLiteDatabase = getWritableDatabase()
        val values = ContentValues()
        values.put(MOOD_MOOD, smiley)
        val numUpdated = db.update(
            TABLE_NAME_MOOD,
            values, "$_ID = ?", arrayOf(java.lang.Long.toString(id))
        )
        Log.d(TAG, "update(): id=$id -> $numUpdated")
    }

    fun delete(id: Long) {
        val db: SQLiteDatabase = getWritableDatabase()
        val numDeleted =
            db.delete(TABLE_NAME_MOOD, "$_ID = ?", arrayOf(java.lang.Long.toString(id)))
        Log.d(TAG, "delete(): id=$id -> $numDeleted")
    }
}