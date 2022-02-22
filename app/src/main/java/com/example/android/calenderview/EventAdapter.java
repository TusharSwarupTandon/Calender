package com.example.android.calenderview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EventAdapter extends ArrayAdapter<Event>
{
    Context mContext;
    public EventAdapter(AppCompatActivity context, ArrayList<Event> event)
    {
        super(context, 0 ,event);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItemView = convertView;
        if(listItemView == null)
        {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.event_list_view, parent, false);
        }

        Event currentEvent = getItem(position);

        TextView dateView = listItemView.findViewById(R.id.date);
        dateView.setText(String.valueOf(currentEvent.getDate()));

        TextView eventView = listItemView.findViewById(R.id.event_name);
        eventView.setText(currentEvent.getEvent());

        TextView timeView = listItemView.findViewById(R.id.event_time);
        timeView.setText(currentEvent.getTime());

        return listItemView;
    }
}
