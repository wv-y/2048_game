package com.example.a2048_game.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Helper extends SQLiteOpenHelper {

    public static final String DB_NAME = "scoredb.db"; //数据库名字

    public static final int DB_VERSION = 1;     //数据库版本
    public static final String sql = "CREATE TABLE  score " +
            "(score int," +
            "time TEXT)";

    public Helper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
