package com.example.akshay.Connect;

/**
 * Created by Akshay on 12/17/2017.
 */

public class Request {

    String req_type;
    String req_uid;

    public Request() {

    }

    public Request(String req_type, String req_uid) {
        this.req_type = req_type;
        this.req_uid = req_uid;
    }

    public String getReq_uid() {
        return req_uid;
    }
    public void setReq_uid(String req_uid) {
        this.req_uid = req_uid;
    }
    public String getReq_type() {
        return req_type;
    }

    public void setReq_type(String req_type) {
        this.req_type = req_type;
    }
}
