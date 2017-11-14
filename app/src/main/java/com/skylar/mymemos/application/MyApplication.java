package com.skylar.mymemos.application;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.skylar.mymemos.db.MemoDBManager;
import com.skylar.mymemos.db.MemoDatabaseHelper;

/**
 * Created by Skylar on 2017/3/21.
 */

public class MyApplication extends Application {
    public static Context applicationContext;
    private static MyApplication instance;
    private static SQLiteDatabase database;
    private static MemoDBManager dbManager;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;

        database = new MemoDatabaseHelper(this,"Memo.db",null,1).getWritableDatabase();
        dbManager = new MemoDBManager(database);
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public static SQLiteDatabase getDB() {
        return database;
    }

    public static MemoDBManager getDBManager(){
        return dbManager;
    }
}
