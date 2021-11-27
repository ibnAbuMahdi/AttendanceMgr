package com.example.attendancemgr.database;

import android.content.Context;
import android.os.AsyncTask;
import androidx.room.Database;
import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {AgentCourse.class, AttendanceCourse.class, EnrolCourse.class}, version = 3, exportSchema = false)
public abstract class CourseRoomDatabase extends RoomDatabase {

    public abstract CourseDao courseDao();

    private static CourseRoomDatabase INSTANCE;

    public static CourseRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CourseRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here.
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CourseRoomDatabase.class, "Attendance_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this practical.
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                }
            };



    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final CourseDao mDao;

        // Initial data set

        PopulateDbAsync(CourseRoomDatabase db) {
            mDao = db.courseDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // If we have no words, then create the initial list of words.

            return null;
        }
    }
}
