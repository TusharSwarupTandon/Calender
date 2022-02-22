package com.example.android.calenderview;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.calenderview.data.EventContract.EventEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EventDialog.EventDialogListener
{
    private long currentDate;
    private EventAdapter mAdapter;
    private int alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute;

    private final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView mEventsList = findViewById(R.id.list);
        mAdapter = new EventAdapter(this, new ArrayList<>());
        mEventsList.setAdapter(mAdapter);

        FloatingActionButton mAddButton = findViewById(R.id.button_addEvent);
        mEventsList = findViewById(R.id.list);
        CalendarView mCalender = findViewById(R.id.calenderView);
        currentDate = mCalender.getDate();
        String[] dmy = df.format(new Date(currentDate)).split("-");
        displayDailyEvents(Integer.parseInt(dmy[0]),Integer.parseInt(dmy[1]),Integer.parseInt(dmy[2]));
        mCalender.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                String s = dayOfMonth+"-"+(month+1)+"-"+year;
                try
                {
                    Date d = df.parse(s);
                    assert d != null;
                    currentDate = d.getTime();
                }
                catch(ParseException e)
                {
                    e.printStackTrace();
                }
                displayDailyEvents(dayOfMonth, month+1, year);
        });
        mAddButton.setOnClickListener(v -> {
            EventDialog eventDialog = new EventDialog();
            eventDialog.show(getSupportFragmentManager(), "Event Dialog");
        });

        mEventsList.setOnItemLongClickListener((arg0, arg1, position, id) ->
        {
            Event currentEvent = mAdapter.getItem(position);
            AlertDialog diaBox = AskOption(currentEvent);
            diaBox.show();

            return true;
        });

        mEventsList.setOnItemClickListener((parent, view, position, id) ->
        {
            EventDialog eventDialog = new EventDialog();
            Event currentEvent = mAdapter.getItem(position);
            int ID = currentEvent.getId();
            Bundle bundle = new Bundle();
            bundle.putString("Id",String.valueOf(ID));
            eventDialog.setArguments(bundle);
            eventDialog.show(getSupportFragmentManager(), "Edit Event");
        });
    }


    @Override
    public void applyData(String time, String event, String ID) throws ParseException {
        String dateString = df.format(new Date(currentDate));
        String[] dmy = dateString.split("-");
        int date = Integer.parseInt(dmy[0]);
        int month = Integer.parseInt(dmy[1]);
        int year = Integer.parseInt(dmy[2]);
        updateEvent(event, time, ID, date, month, year);
    }

    @Override
    public void applyData(String time, String event) throws ParseException {
        String dateString = df.format(new Date(currentDate));
        String[] dmy = dateString.split("-");
        int date = Integer.parseInt(dmy[0]);
        int month = Integer.parseInt(dmy[1]);
        int year = Integer.parseInt(dmy[2]);
        insertEvent(event, time, date, month, year);
    }

    private void updateEvent(String name, String time, String ID, int date, int month, int year) throws ParseException
    {
        if(name.equals(""))
        {
            Toast.makeText(this, getString(R.string.empty_event), Toast.LENGTH_SHORT).show();
            return;
        }
        String selection = EventEntry._ID+ " =? ";
        String[] selectionArgs = new String[]{ID};
        String[] projection =
                {EventEntry.COLUMN_EVENT_NAME,
                        EventEntry.COLUMN_EVENT_TIME};
        String tm ="";
        try(Cursor cursor = MainActivity.this.getContentResolver().query(EventEntry.CONTENT_URI, projection, selection, selectionArgs, null)) {
            while (cursor.moveToNext())
            {
                tm = cursor.getString(cursor.getColumnIndex(EventEntry.COLUMN_EVENT_TIME));
            }
        }
        ContentValues values = new ContentValues();
        values.put(EventEntry.COLUMN_EVENT_NAME, name);
        values.put(EventEntry.COLUMN_EVENT_TIME, time);

        int rowsUpdated = getContentResolver().update(EventEntry.CONTENT_URI, values, selection, selectionArgs);
        if (rowsUpdated == 0)
        {
            Toast.makeText(this, getString(R.string.editor_insert_event_failed), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, getString(R.string.editor_insert_event_successful), Toast.LENGTH_SHORT).show();
            DateFormat df = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
            DateFormat frmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date t = df.parse(tm);
            String time24 = frmt.format(t);
            cancelAlarm(Integer.parseInt(ID)+Integer.parseInt(time24.substring(0,2))+Integer.parseInt(time24.substring(3,5)));
            alarmYear = year;
            alarmMonth = month;
            alarmDay = date;

            Date t1 = df.parse(time);
            String time241 = frmt.format(t1);

            alarmHour = Integer.parseInt(time241.substring(0,2));
            alarmMinute = Integer.parseInt(time241.substring(3,5));
            Calendar calendar = Calendar.getInstance();
            calendar.set(alarmYear, alarmMonth-1, alarmDay,alarmHour, alarmMinute);
            setAlarm(calendar, name, time241, Integer.parseInt(ID)+alarmHour+alarmMinute);
            displayDailyEvents(date, month, year);
        }
    }

    private void insertEvent(String name, String time, int date, int month, int year) throws ParseException {

        if(name.equals(""))
        {
            Toast.makeText(this, getString(R.string.empty_event), Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(EventEntry.COLUMN_EVENT_NAME, name);
        values.put(EventEntry.COLUMN_EVENT_TIME, time);
        values.put(EventEntry.COLUMN_EVENT_DATE, date);
        values.put(EventEntry.COLUMN_EVENT_MONTH, month);
        values.put(EventEntry.COLUMN_EVENT_YEAR, year);

        Uri newUri = getContentResolver().insert(EventEntry.CONTENT_URI, values);
        if (newUri == null)
        {
            Toast.makeText(this, getString(R.string.editor_insert_event_failed), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, getString(R.string.editor_insert_event_successful), Toast.LENGTH_SHORT).show();

            alarmYear = year;
            alarmMonth = month;
            alarmDay = date;
            DateFormat df = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
            DateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date t = df.parse(time);
            String time24 = format.format(t);

            alarmHour = Integer.parseInt(time24.substring(0,2));
            alarmMinute = Integer.parseInt(time24.substring(3,5));
            Calendar calendar = Calendar.getInstance();
            calendar.set(alarmYear, alarmMonth-1, alarmDay,alarmHour, alarmMinute);
            setAlarm(calendar, name, time24, (int)ContentUris.parseId(newUri)+alarmHour+alarmMinute);
            displayDailyEvents(date, month, year);
        }

    }

    private void setAlarm(Calendar calendar, String event, String time, int RequestCode)
    {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("event",event);
        intent.putExtra("time", time);
        intent.putExtra("id", RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), RequestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void cancelAlarm(int RequestCode)
    {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), RequestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void displayDailyEvents(int date, int month, int year)
    {
        List<Event> events = new ArrayList<>();

        String[] projection =
                {EventEntry._ID,
                        EventEntry.COLUMN_EVENT_NAME,
                        EventEntry.COLUMN_EVENT_TIME,
                        EventEntry.COLUMN_EVENT_DATE,
                        EventEntry.COLUMN_EVENT_MONTH,
                        EventEntry.COLUMN_EVENT_YEAR};

        String selection = EventEntry.COLUMN_EVENT_DATE+ "=? and " + EventEntry.COLUMN_EVENT_MONTH + "=? and " +EventEntry.COLUMN_EVENT_YEAR + "=?";
        String[] selectionArgs = new String[]{String.valueOf(date), String.valueOf(month), String.valueOf(year)};

        try (Cursor cursor = getContentResolver().query(EventEntry.CONTENT_URI, projection, selection, selectionArgs, null)) {
            int idColumnIndex = cursor.getColumnIndex(EventEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_NAME);
            int timeColumnIndex = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_TIME);
            int dateColumnIndex = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_DATE);
            int monthColumnIndex = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_MONTH);
            int yearColumnIndex = cursor.getColumnIndex(EventEntry.COLUMN_EVENT_YEAR);

            while (cursor.moveToNext())
            {
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentTime = cursor.getString(timeColumnIndex);
                int currentDate = cursor.getInt(dateColumnIndex);
                int currentMonth = cursor.getInt(monthColumnIndex);
                int currentYear = cursor.getInt(yearColumnIndex);
                events.add(new Event(currentID, currentName, currentTime, currentDate, currentMonth, currentYear));
            }
            mAdapter.clear();
            mAdapter.addAll(events);
        }
    }

    private AlertDialog AskOption(Event event)
    {

        return new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Do you want to delete the event")
                .setIcon(R.drawable.ic_delete)

                .setPositiveButton("Delete", (dialog, whichButton) ->
                {
                    int event_id = event.getId();
                    int date = event.getDate();
                    int month = event.getMonth();
                    int year = event.getYear();
                    String time = event.getTime();

                    DateFormat df = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
                    DateFormat frmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date t = null;
                    try
                    {
                        t = df.parse(time);
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    String time24 = frmt.format(t);
                    cancelAlarm(event_id+Integer.parseInt(time24.substring(0,2))+Integer.parseInt(time24.substring(3,5)));

                    String selection = EventEntry._ID + " =?";
                    String[] selectionArgs = {String.valueOf(event_id)};
                    int rowsDeleted = getContentResolver().delete(EventEntry.CONTENT_URI, selection, selectionArgs);

                    if(rowsDeleted == 0)
                    {
                        Toast.makeText(getApplicationContext(), R.string.editor_delete_event_failed,Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), R.string.editor_delete_event_successful, Toast.LENGTH_SHORT).show();
                        cancelAlarm(event_id);
                        displayDailyEvents(date, month, year);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();
    }
}