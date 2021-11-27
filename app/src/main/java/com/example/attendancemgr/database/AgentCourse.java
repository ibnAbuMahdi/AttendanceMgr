package com.example.attendancemgr.database;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AgentCourse_table")
public class AgentCourse {
    public enum Att_Status {
        open, closed
    }
    public enum Sub_Status {
        submitted, unsubmitted
    }
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "faculty")
    private String faculty;

    @NonNull
    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(@NonNull String faculty) {
        this.faculty = faculty;
    }

    @NonNull
    @ColumnInfo(name = "department")
    private String dept;

    @NonNull
    @ColumnInfo(name = "course")
    private String course;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ColumnInfo(name = "attendance_status")
    private Att_Status att_status;

    @ColumnInfo(name = "submission_status")
    private Sub_Status sub_status;

    @NonNull
    public String getDept() {
        return dept;
    }

    public void setDept(@NonNull String dept) {
        this.dept = dept;
    }

    public void setCourse(@NonNull String course) {
        this.course = course;
    }

    public AgentCourse(@NonNull String faculty, @NonNull String dept, @NonNull String course, Att_Status att_status, Sub_Status sub_status){
        this.faculty = faculty;
        this.dept = dept;
        this.course = course;
        this.att_status = att_status;
        this.sub_status = sub_status;

    }

    @NonNull
    public String getCourse() {
        return course;
    }



    public AgentCourse.Att_Status getAtt_status() {
        return att_status;
    }

    public void setAtt_status(AgentCourse.Att_Status att_status) {
        this.att_status = att_status;
    }

    public AgentCourse.Sub_Status getSub_status() {
        return sub_status;
    }

    public void setSub_status(AgentCourse.Sub_Status sub_status) {
        this.sub_status = sub_status;
    }
}
