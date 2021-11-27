package com.example.attendancemgr.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {

    private String mData;

    //... other data fields that may be accessible to the UI
    LoggedInUserView(String data) {

        this.mData = data;
    }


    public String getmData() {
        return mData;
    }

}
