package ru.alfabank.news_viewer.DataBase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by pavel on 21.01.2017.
 */

public class DbTable {
    public static final String TABLE_ITEMS_NAME = "items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ITEM_TITLE = "title";
    public static final String COLUMN_ITEM_LINK = "link";

    public static final String[] ITEMS_PROJECTION = new String[] {
            COLUMN_ID, COLUMN_ITEM_TITLE, COLUMN_ITEM_LINK
    };
    private static final String TABLE_ITEMS_CREATE = "create table "
            + TABLE_ITEMS_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_ITEM_TITLE + " text, "
            + COLUMN_ITEM_LINK + " text"
            + ");";
    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_ITEMS_CREATE);
    }
}
