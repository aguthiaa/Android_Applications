package com.peter.socialnetworkingapp;

public class Messages {

    public String message,date,time,from,type;

    public Messages(){

    }

    public Messages(String message, String date, String time, String from, String type) {
        this.message = message;
        this.date = date;
        this.time = time;
        this.from = from;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
