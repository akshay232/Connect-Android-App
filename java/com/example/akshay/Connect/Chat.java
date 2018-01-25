package com.example.akshay.Connect;

/**
 * Created by Akshay on 12/22/2017.
 */

public class Chat {
    String seen;
    long timestamp;

    public Chat() {
    }

    public Chat(String seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
