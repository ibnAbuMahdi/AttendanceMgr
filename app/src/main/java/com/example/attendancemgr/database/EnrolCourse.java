package com.example.attendancemgr.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "EnrolCourse_table")
public class EnrolCourse {
   public enum Sub_Status {
        submitted, unsubmitted
    }
    @NonNull
    @ColumnInfo(name = "course")
    private String course;

   @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "ID_Image")
    private byte[] IDImg;

    @ColumnInfo(name = "admission_number")
    private String Adm_No;

    @ColumnInfo(name = "status")
    private Sub_Status sub_status;

    @ColumnInfo(name = "fingerprint")
    private byte[] FP;

    public String getAdm_No() {
        return Adm_No;
    }

    public EnrolCourse(@NonNull String course, String Adm_No, byte[] FP, Sub_Status sub_status, byte[] IDImg){
        this.course = course;
        this.IDImg = IDImg;
        this.Adm_No = Adm_No;
        this.FP = FP;
        this.sub_status = sub_status;
    }

    @NonNull
    public String getCourse() {
        return course;
    }


    public byte[] getIDImg() {
        return IDImg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setIDImg(byte[] IDImg) {
        this.IDImg = IDImg;
    }

    public String getAdmNo() {
        return Adm_No;
    }

    public void setAdmNo(String AdmNo) {
        this.Adm_No = AdmNo;
    }

    public byte[] getFP() {
        return FP;
    }
    public void setFP(byte[] fp) {
        FP = fp;
    }

    public Sub_Status getSub_status() {
        return sub_status;
    }

    public void setSub_status(Sub_Status sub_status) {
        this.sub_status = sub_status;
    }
}
