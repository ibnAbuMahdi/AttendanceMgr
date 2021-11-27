package com.example.attendancemgr.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.LinkedList;
import java.util.List;

public class CourseViewModel extends AndroidViewModel {
    private CourseRepository mRepository;
    private LiveData<List<AgentCourse>> mAllAgentCourses;
    private LiveData<List<AttendanceCourse>> mAllAttendanceCourses;
    private LiveData<List<EnrolCourse>> mAllEnrolCourses;
    private MediatorLiveData<List<AgentCourse>> mAgentCourses = new MediatorLiveData<>();
    public CourseViewModel(@NonNull Application application) {
        super(application);
        /*db = CourseRoomDatabase.getDatabase(application);
        mCourseDao = db.courseDao();*/
        mRepository = new CourseRepository(application);
        this.mAllAgentCourses = mRepository.getAllAgentCourses();
        this.mAllAttendanceCourses = mRepository.getAllAttendanceCourses();
        this.mAllEnrolCourses = mRepository.getAllEnrolCourses();
    }
    public LiveData<List<AgentCourse>> getAllAgentCourses() {
        return mAllAgentCourses;
    }
    public LiveData<List<AttendanceCourse>> getAllAttendanceCourses() {
        return mAllAttendanceCourses;
    }
    public LiveData<List<EnrolCourse>> getAllEnrolCourses() {
        return mAllEnrolCourses;
    }
public AgentCourse[] getAnyCourse(){
        return mRepository.getAnyCourse();
}
    public void insert(AgentCourse course) {
        mRepository.insert(course);
    }
    public void insert(AttendanceCourse course) {
        mRepository.insert(course);
    }
    public void insert(EnrolCourse course) {
        mRepository.insert(course);
    }

    public void deleteAllAgentCourses() {
        mRepository.deleteAllAgentCourses();
    }
    public void deleteAllAttendanceCourses() {
        mRepository.deleteAllAttendanceCourses();
    }
    public void deleteAllEnrolCourses() {
        mRepository.deleteAllEnrolCourses();
    }

    public void delete(AgentCourse course) {
        mRepository.deleteAgentCourse(course);
    }
    public void delete(AttendanceCourse course) {
        mRepository.deleteAttendanceCourse(course);
    }
    public void delete(EnrolCourse course) {
        mRepository.deleteEnrolCourse(course);
    }

    public void update(AgentCourse course) {
        mRepository.update(course);
    }
    public void update(AttendanceCourse course) {
        mRepository.update(course);
    }
    public void update(EnrolCourse course) {
        mRepository.update(course);
    }

}
