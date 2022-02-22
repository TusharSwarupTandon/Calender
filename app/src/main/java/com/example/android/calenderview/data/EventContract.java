package com.example.android.calenderview.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class EventContract
{

    public static final String CONTENT_AUTHORITY = "com.example.android.calenderview";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_EVENTS = "events";

    private EventContract() {}

    public static final class EventEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EVENTS);
        public final static String TABLE_NAME = "events";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_EVENT_NAME ="name";
        public final static String COLUMN_EVENT_TIME = "time";
        public final static String COLUMN_EVENT_DATE = "date";
        public final static String COLUMN_EVENT_MONTH = "month";
        public final static String COLUMN_EVENT_YEAR = "year";
    }

}
