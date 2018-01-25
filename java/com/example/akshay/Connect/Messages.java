package com.example.akshay.Connect;

/**
 * Created by Akshay on 12/16/2017.
 */

public class Messages {

    String message,type,from;
    Boolean seen;
    Long time;

    public Messages()
    {

    }

    public Messages(String message, String type, String from, Boolean seen, Long time) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.seen = seen;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }


}
