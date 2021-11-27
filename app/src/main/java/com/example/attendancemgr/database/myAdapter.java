package com.example.attendancemgr.database;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

public class myAdapter extends ArrayAdapter<String> {
   private List<String> mAgentCourses;
   private Context mContext;
   private int mResource;
    public myAdapter(@NonNull Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.mAgentCourses = objects;
        this.mContext = context;
        this.mResource = resource;
    }
    public void setAgentCourses(List<String> courses) {
        mAgentCourses = courses;
        notifyDataSetChanged();
    }

}
