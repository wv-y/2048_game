package com.example.a2048_game.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class Operator {
    private Helper helper;
    private Context context;

    public Operator(Context context, Helper helper) {
        this.context = context;
        this.helper = helper;
    }

    //插入新的成绩
    public void insertScore(String score,String time ){
        String sql = "insert into score(score,time)values(?,?)";
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql, new String[]{score ,time});
    }

    //查找最高的分数
    public String getBestScore(){
        String str;
        String sql ="select * from score order by score DESC";
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,new String[]{});
        cursor.moveToFirst();
        str = cursor.getString(cursor.getColumnIndex("score"));
        return str;
    }
}
