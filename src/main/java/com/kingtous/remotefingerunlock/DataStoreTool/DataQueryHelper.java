package com.kingtous.remotefingerunlock.DataStoreTool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataQueryHelper extends SQLiteOpenHelper{

    private final String CreateBook="create table data(" +
            "Type text,"+
            "Mac text PRIMARY KEY," +
            "User text," +
            "Passwd text," +
            "isDefault Integer)";


    public DataQueryHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
            db.execSQL(CreateBook);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
