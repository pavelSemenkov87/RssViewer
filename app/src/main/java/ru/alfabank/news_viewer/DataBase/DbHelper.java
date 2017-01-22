package ru.alfabank.news_viewer.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context paramContext) {
        super(paramContext, "rssDB", null, 1);
    }

    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
        DbTable.onCreate(paramSQLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}