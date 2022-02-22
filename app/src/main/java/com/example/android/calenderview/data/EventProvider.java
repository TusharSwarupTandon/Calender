package com.example.android.calenderview.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.calenderview.data.EventContract.EventEntry;

public class EventProvider extends ContentProvider
{
    public static final String LOG_TAG = EventProvider.class.getSimpleName();
    private EventDbHelper mDbHelper;

    private static final int EVENTS = 100;
    private static final int EVENTS_ID = 101;

    private static  final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sUriMatcher.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_EVENTS, EVENTS);
        sUriMatcher.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_EVENTS+"#", EVENTS_ID);
    }

    @Override
    public boolean onCreate()
    {
        mDbHelper = new EventDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor = null;
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case EVENTS:
                cursor = database.query(EventEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case EVENTS_ID:
                selection = EventEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(EventEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }
    @Override
    public Uri insert(Uri uri, ContentValues contentValues)
    {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case EVENTS:
                return insertEvent(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertEvent(Uri uri, ContentValues values)
    {
        String name = values.getAsString(EventEntry.COLUMN_EVENT_NAME);
        if (name == null)
        {
            throw new IllegalArgumentException("Event requires a name");
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(EventEntry.TABLE_NAME, null, values);

        if(id == -1)
        {
            Log.e("Insert Data:","Failed to insert row for" + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EVENTS:
                return updateEvent(uri, contentValues, selection, selectionArgs);
            case EVENTS_ID:
                selection = EventEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateEvent(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateEvent(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        if (values.containsKey(EventEntry.COLUMN_EVENT_NAME))
        {
            String name = values.getAsString(EventEntry.COLUMN_EVENT_NAME);
            if (name == null)
            {
                throw new IllegalArgumentException("Event requires a name");
            }
        }
        if (values.size() == 0)
        {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(EventEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EVENTS:
                return database.delete(EventEntry.TABLE_NAME, selection, selectionArgs);
            case EVENTS_ID:
                selection = EventEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.delete(EventEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }
}
