package geocaching.pasi.geonote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Pasi on 09/03/2016.
 */
public class DBHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "CacheDB.db";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CACHES_TABLE = "CREATE TABLE caches ( " +
                "name TEXT PRIMARY KEY , " +
                "gc TEXT, "+
                "latitude FLOAT , "+
                "longitude FLOAT , "+
                "difficulty FLOAT , "+
                "terrain FLOAT , " +
                "size TEXT , " +
                "note TEXT ," +
                "type TEXT ," +
                "winter TEXT)";
        db.execSQL(CREATE_CACHES_TABLE);

    }


    // Table name
    private static final String TABLE_CACHES = "caches";

    // Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_GC = "gc";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_DIFFICULTY  = "difficulty";
    private static final String KEY_TERRAIN = "terrain";
    private static final String KEY_SIZE = "size";
    private static final String KEY_NOTE = "note";
    private static final String KEY_TYPE = "type";
    private static final String KEY_WINTER = "winter";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS books");
        // create fresh books table
        this.onCreate(db);
    }

    public void addCache(Cache cache){
        //Get writeable db
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, cache.getName());
        values.put(KEY_GC, cache.getGc());
        values.put(KEY_LATITUDE, cache.getCoordinates().latitude);
        values.put(KEY_LONGITUDE, cache.getCoordinates().longitude);
        values.put(KEY_DIFFICULTY, cache.getDifficulty());
        values.put(KEY_TERRAIN, cache.getTerrain());
        values.put(KEY_SIZE, cache.getSizeString());
        values.put(KEY_NOTE, cache.getNote());
        values.put(KEY_TYPE, cache.getTypeString());
        values.put(KEY_WINTER, cache.getWinterString());
        db.insert(TABLE_CACHES, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        db.close();
    }

    public ArrayList<Cache> getAllCaches(){
        ArrayList<Cache> list = new  ArrayList<Cache>();
        //Build query string
        String query = "SELECT  * FROM " + TABLE_CACHES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Cache cache = null;
        if(cursor.moveToFirst()){
            do{
                cache = new Cache();
                cache.setName(cursor.getString(0));
                cache.setGc(cursor.getString(1));
                try {
                    cache.setCoordinates(new LatLng(Double.parseDouble(cursor.getString(2)), Double.parseDouble(cursor.getString(3))));
                }
                catch(IllegalArgumentException ex){}
                cache.setDifficulty(Double.parseDouble(cursor.getString(4)));
                cache.setTerrain(Double.parseDouble(cursor.getString(5)));
                cache.setSize(cursor.getString(6));
                cache.setNote(cursor.getString(7));
                cache.setType(cursor.getString(8));
                cache.setWinter(cursor.getString(9));
                list.add(cache);

            }while(cursor.moveToNext());
        }

        return list;
    }

    public void removeCache(Cache cache){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CACHES, KEY_NAME + " = ?", new String[] {(cache.getName())});
        db.close();
    }

    public void updateCache(Cache cache, String oldName){
        Log.v("GeoNote","DBHelper.updateCache: oldName === " + oldName);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, cache.getName());
        values.put(KEY_GC, cache.getGc());
        values.put(KEY_LATITUDE, cache.getCoordinates().latitude);
        values.put(KEY_LONGITUDE, cache.getCoordinates().longitude);
        values.put(KEY_DIFFICULTY, cache.getDifficulty());
        values.put(KEY_TERRAIN, cache.getTerrain());
        values.put(KEY_SIZE, cache.getSizeString());
        values.put(KEY_NOTE, cache.getNote());
        values.put(KEY_TYPE, cache.getTypeString());
        values.put(KEY_WINTER, cache.getWinterString());
        db.update(TABLE_CACHES, values, KEY_NAME + " = ?", new String[]{(oldName)});
        db.close();
    }
}
