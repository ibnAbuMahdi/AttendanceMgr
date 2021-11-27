package com.example.attendancemgr.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CourseRepository {
    private CourseDao mCourseDao;
    private LiveData<List<AgentCourse>> mAllAgentCourses;
    private LiveData<List<EnrolCourse>> mAllEnrolCourses;
    private LiveData<List<AttendanceCourse>> mAllAttendanceCourses;

    CourseRepository(Application application) {
        CourseRoomDatabase db = CourseRoomDatabase.getDatabase(application);
        mCourseDao = db.courseDao();
        mAllAgentCourses = mCourseDao.getAllAgentCourses();
        mAllAttendanceCourses = mCourseDao.getAllAttendanceCourses();
        mAllEnrolCourses = mCourseDao.getAllEnrolCourses();
    }
    LiveData<List<AgentCourse>> getAllAgentCourses() {
        return mAllAgentCourses;
    }
    LiveData<List<EnrolCourse>> getAllEnrolCourses() {
        return mAllEnrolCourses;
    }
    LiveData<List<AttendanceCourse>> getAllAttendanceCourses() {
        return mAllAttendanceCourses;
    }

    public void insert(AgentCourse course) {
        new insertAsyncTask(mCourseDao).execute(course);
    }
    public void insert(AttendanceCourse course) {
        new insertAsyncTask(mCourseDao).execute(course);
    }
    public void insert(EnrolCourse course) {
        new insertAsyncTask(mCourseDao).execute(course);
    }

    public void update(AgentCourse course)  {
        new updateCourseAsyncTask(mCourseDao).execute(course);
    }
    public void update(AttendanceCourse course)  {
        new updateCourseAsyncTask(mCourseDao).execute(course);
    }
    public void update(EnrolCourse course)  {
        new updateCourseAsyncTask(mCourseDao).execute(course);
    }

    public void deleteAllAgentCourses()  {
        new deleteAllAgentCoursesAsyncTask(mCourseDao).execute();
    }
    public void deleteAllAttendanceCourses()  {
        new deleteAllAttendanceCoursesAsyncTask(mCourseDao).execute();
    }
    public void deleteAllEnrolCourses()  {
        new deleteAllEnrolCoursesAsyncTask(mCourseDao).execute();
    }

    public void deleteAgentCourse(AgentCourse course) {
        new deleteCourseAsyncTask(mCourseDao).execute(course);
    }
    public void deleteAttendanceCourse(AttendanceCourse course) {
        new deleteCourseAsyncTask(mCourseDao).execute(course);
    }
    public void deleteEnrolCourse(EnrolCourse course) {
        new deleteCourseAsyncTask(mCourseDao).execute(course);
    }
    public AgentCourse[] getAnyCourse(){
       return mCourseDao.getAnyCourse();
    }
    private static class insertAsyncTask extends AsyncTask<Object, Void, Void> {

        private CourseDao mAsyncTaskDao;

        insertAsyncTask(CourseDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Object... params) {
            if(AgentCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.insert((AgentCourse) params[0]);
            }else if(EnrolCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.insert((EnrolCourse) params[0]);
            }else if(AttendanceCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.insert((AttendanceCourse) params[0]);
            }

            return null;
        }
    }

    private static class deleteAllAgentCoursesAsyncTask extends AsyncTask<Void, Void, Void> {
        private CourseDao mAsyncTaskDao;

        deleteAllAgentCoursesAsyncTask(CourseDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAllAgentCourse();
            return null;
        }
    }
    private static class deleteAllEnrolCoursesAsyncTask extends AsyncTask<Void, Void, Void> {
        private CourseDao mAsyncTaskDao;

        deleteAllEnrolCoursesAsyncTask(CourseDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAllEnrolCourse();
            return null;
        }
    }
    private static class deleteAllAttendanceCoursesAsyncTask extends AsyncTask<Void, Void, Void> {
        private CourseDao mAsyncTaskDao;

        deleteAllAttendanceCoursesAsyncTask(CourseDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAllAttendanceCourse();
            return null;
        }
    }

    private static class deleteCourseAsyncTask extends AsyncTask<Object, Void, Void> {
        private CourseDao mAsyncTaskDao;

        private deleteCourseAsyncTask(CourseDao Dao) {
            this.mAsyncTaskDao = Dao;
        }

        @Override
        protected Void doInBackground(final Object... params) {
            if(AgentCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.deleteAgentCourse((AgentCourse) params[0]);
            }else if(EnrolCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.deleteEnrolCourse((EnrolCourse) params[0]);
            }else if(AttendanceCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.deleteAttendanceCourse((AttendanceCourse) params[0]);
            }
            return null;
        }
    }

    private static class updateCourseAsyncTask extends AsyncTask<Object, Void, Void> {
        private CourseDao mAsyncTaskDao;

        updateCourseAsyncTask(CourseDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Object... params) {
            if(AgentCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.update((AgentCourse) params[0]);
            }else if(EnrolCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.update((EnrolCourse) params[0]);
            }else if(AttendanceCourse.class.getName().equals(params[0].getClass().getName())){
                mAsyncTaskDao.update((AttendanceCourse) params[0]);
            }
            return null;
        }
    }
}
