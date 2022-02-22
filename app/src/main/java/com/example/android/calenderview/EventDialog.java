package com.example.android.calenderview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.android.calenderview.data.EventContract.EventEntry;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDialog extends AppCompatDialogFragment
{

    private EditText mEventName;
    private TextView mEventTime;
    private EventDialogListener listener;
    String eventId = "";
    private String mEventType;
    private final String mUpdate = "Update";
    private final String mInsert = "Insert";


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_event, null);

        mEventName = view.findViewById(R.id.event_details);
        mEventTime = view.findViewById(R.id.alarm_time);
        ImageButton mTimePicker = view.findViewById(R.id.time_picker);

        mTimePicker.setOnClickListener(v -> {
            TimePickerDialog dpd = new TimePickerDialog(getContext(), (timePicker, hourOfDay, minute) -> {

                Time time = new Time(hourOfDay, minute, 0);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
                String s = simpleDateFormat.format(time);
                mEventTime.setText(s);
            }, 8, 0, false);

            dpd.show(); });

        Bundle bundle = getArguments();
        String mTitle;
        try
        {
            mTitle = "Update Event";
            eventId = bundle.getString("Id","");
            String[] projection =
                    {EventEntry.COLUMN_EVENT_NAME,
                            EventEntry.COLUMN_EVENT_TIME};

            String selection = EventEntry._ID+ " =? ";
            String[] selectionArgs = new String[]{eventId};
            try(Cursor cursor = getContext().getContentResolver().query(EventEntry.CONTENT_URI, projection, selection, selectionArgs, null)) {
                while (cursor.moveToNext())
                {
                    mEventName.setText(cursor.getString(cursor.getColumnIndex(EventEntry.COLUMN_EVENT_NAME)));
                    mEventTime.setText(cursor.getString(cursor.getColumnIndex(EventEntry.COLUMN_EVENT_TIME)));
                }
            }
            mEventType = mUpdate;
        }
        catch (NullPointerException ignored)
        {
            mTitle = "Add Event";
            mEventType = mInsert;
        }

        builder.setView(view)
                .setTitle("Event")
                .setPositiveButton(mTitle, (dialog, which) -> {
                    String time = mEventTime.getText().toString();
                    String event = mEventName.getText().toString().trim();
                    if(mEventType.equals(mUpdate))
                    {
                        try {
                            listener.applyData(time, event, eventId);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(mEventType.equals(mInsert))
                    {
                        try {
                            listener.applyData(time, event);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        try
        {
            listener = (EventDialogListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString()+"must implement EventDialogListener");
        }
    }

    public interface EventDialogListener
    {
        void applyData(String time, String event, String ID) throws ParseException;
        void applyData(String time, String event) throws ParseException;
    }
}
