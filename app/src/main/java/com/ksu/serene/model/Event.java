package com.ksu.serene.model;

public class Event {
    private String summary;
    private String startTime;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;

    public Event(String name, String date, String time) {
        this.summary = name;
        startTime = date;
        this.time = time;

    }

    public Event(){

    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
