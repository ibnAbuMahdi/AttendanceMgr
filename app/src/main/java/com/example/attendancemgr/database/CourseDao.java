package com.example.attendancemgr.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.LinkedList;
import java.util.List;
@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(AgentCourse agCourse);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(AttendanceCourse atCourse);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(EnrolCourse enCourse);

    @Query("DELETE FROM AgentCourse_table")
    void deleteAllAgentCourse();

    @Query("DELETE FROM AttendanceCourse_table")
    void deleteAllAttendanceCourse();

    @Query("DELETE FROM EnrolCourse_table")
    void deleteAllEnrolCourse();

    @Delete
    void deleteAgentCourse(AgentCourse agCourse);

    @Delete
    void deleteAttendanceCourse(AttendanceCourse atCourse);

    @Delete
    void deleteEnrolCourse(EnrolCourse enCourse);

    @Query("SELECT * from AgentCourse_table ORDER BY course ASC")
    LiveData<List<AgentCourse>> getAllAgentCourses();

    @Query("SELECT * from AttendanceCourse_table WHERE sub_status == 'unsubmitted' ORDER BY course ASC")
    LiveData<List<AttendanceCourse>> getAllAttendanceCourses();

    @Query("SELECT * from EnrolCourse_table WHERE status == 'unsubmitted' ORDER BY course ASC")
    LiveData<List<EnrolCourse>> getAllEnrolCourses();

    @Update
    void update(AgentCourse... courses);

    @Update()
    void update(AttendanceCourse... courses);

    @Update
    void update(EnrolCourse... courses);

    @Query("SELECT * from AgentCourse_table LIMIT 1")
    AgentCourse[] getAnyCourse();
}
