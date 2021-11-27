package com.example.attendancemgr.ui.Departments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DeptsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DeptsViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
    public void setText(MutableLiveData<String> text){
        this.mText = text;
    }
}