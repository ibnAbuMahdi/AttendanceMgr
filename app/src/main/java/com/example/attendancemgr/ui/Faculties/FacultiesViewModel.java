package com.example.attendancemgr.ui.Faculties;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FacultiesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FacultiesViewModel() {
        mText = new MutableLiveData<>();
       // mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
    public void setText(MutableLiveData<String> text){
        this.mText = text;
    }
}