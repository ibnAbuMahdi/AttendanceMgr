package com.example.attendancemgr.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "AttendanceCourse_table")
public class AttendanceCourse {
    public enum  Sub_Status {
        submitted, unsubmitted
    }
    public enum  Cap_Status {
        captured, uncaptured
    }
    @NonNull
    @ColumnInfo(name = "course")
    private String course;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "sub_status")
    private Sub_Status sub_status;

    @ColumnInfo(name = "cap_status")
    private Cap_Status cap_status;

    @ColumnInfo(name = "admission_number")
    private String Adm_No;

    @ColumnInfo(name = "fingerprint")
    private byte[] FP;

    @ColumnInfo(name = "date")
    private int Date;

    @ColumnInfo(name = "period")
    private int period;
    public AttendanceCourse(@NonNull String course, String Adm_No, byte[] FP, Sub_Status sub_status, Cap_Status cap_status, int Date, int period){
        this.course = course;
        this.sub_status = sub_status;
        this.Adm_No = Adm_No;
        this.FP = FP;
        this.cap_status = cap_status;
        this.Date = Date;
        this.period = period;
    }

    @Ignore
public AttendanceCourse(int id, @NonNull String course, String admNo, byte[] fp, Sub_Status status, Cap_Status cap_status, int Date, int period){
    this.course = course;
    this.sub_status = status;
    this.Adm_No = admNo;
    this.FP = fp;
    this.id = id;
    this.cap_status = cap_status;
    this.Date = Date;
    this.period = period;

}
    @NonNull
    public String getCourse() {
        return course;
    }

    public String getAdmNo() {
        return Adm_No;
    }

    public void setAdmNo(String AdmNo) {
        this.Adm_No = AdmNo;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getFP() {
        return FP;
    }

    public Cap_Status getCap_status() {
        return cap_status;
    }

    public void setCap_status(Cap_Status cap_status) {
        this.cap_status = cap_status;
    }

    public void setFP(byte[] fp) {
        FP = fp;
    }

    public Sub_Status getSub_Status() {
        return sub_status;
    }

    public void setSub_Status(Sub_Status sub_status) {
        this.sub_status = sub_status;
    }

    public Sub_Status getSub_status() {
        return sub_status;
    }

    public String getAdm_No() {
        return Adm_No;
    }
    public void setSub_status(Sub_Status sub_status) {
        this.sub_status = sub_status;
    }

    public void setAdm_No(String adm_No) {
        Adm_No = adm_No;
    }

    public void setCourse(@NonNull String course) {
        this.course = course;
    }

    public int getDate() {
        return Date;
    }

    public void setDate(int date) {
        Date = date;
    }
}
