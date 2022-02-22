package com.example.android.calenderview;

public class Event
{
    private final int id;
    private final String event;
    private final String time;
    private final int date;
    private final int month;
    private final int year;

    public Event(int id, String event, String time, int date, int month, int year) {
        this.id = id;
        this.event = event;
        this.time = time;
        this.date = date;
        this.month = month;
        this.year = year;
    }

    public int getId()
    {
        return id;
    }

    public String getEvent() {
        return event;
    }

    public String getTime() {
        return time;
    }

    public int getDate() {
        return date;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

}
