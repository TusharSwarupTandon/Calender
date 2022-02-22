package com.example.android.calenderview.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.calenderview.data.EventContract.EventEntry;

public class EventDbHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "calender_events.db";
    private static final int DATABASE_VERSION = 1;

    public EventDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String SQL_CREATE_EVENTS_TABLE = "CREATE TABLE "+ EventEntry.TABLE_NAME + "("
                + EventEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EventEntry.COLUMN_EVENT_NAME + " TEXT NOT NULL, "
                + EventEntry.COLUMN_EVENT_TIME + " TEXT, "
                + EventEntry.COLUMN_EVENT_DATE + " INTEGER NOT NULL DEFAULT 1, "
                + EventEntry.COLUMN_EVENT_MONTH + " INTEGER NOT NULL DEFAULT 1, "
                + EventEntry.COLUMN_EVENT_YEAR + " INTEGER NOT NULL DEFAULT 1970);";
        db.execSQL(SQL_CREATE_EVENTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

}
