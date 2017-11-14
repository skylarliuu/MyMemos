package com.skylar.mymemos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Skylar on 2017/3/21.
 */

public class MemoDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_MEMO = "create table Memo ("
            + "_id integer primary key autoincrement,"
            + "content text not null,"
            + "groupName text not null,"
            + "widgetId integer,"
            + "widgetType integer,"
            + "modifiedDate integer,"
            + "createDate integer,"
            + "alarmDate integer,"
            + "stick integer,"
            + "fontSize integer,"
            + "alarmEnable boolean)";

    public static final String CREATE_GROUP = "create table GroupTable ("
//            + "groupId integer  autoincrement,"
            + "groupName text primary key)";

    public MemoDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_MEMO);
        db.execSQL(CREATE_GROUP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
