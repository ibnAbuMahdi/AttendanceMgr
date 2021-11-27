package com.example.attendancemgr.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {
    private String mData;



public LoggedInUser(String data){
    this.mData = data;
}
    public String getmData() {
        return mData;
    }


}