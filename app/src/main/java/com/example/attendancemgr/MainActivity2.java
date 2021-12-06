package com.example.attendancemgr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.attendancemgr.database.AgentCourse;
import com.example.attendancemgr.database.AttendanceCourse;
import com.example.attendancemgr.database.CourseViewModel;
import com.example.attendancemgr.database.EnrolCourse;
import com.example.attendancemgr.ui.Faculties.FacultiesFragment;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static com.example.attendancemgr.ui.login.LoginActivity.ID_FILE;
import static com.example.attendancemgr.ui.login.LoginActivity.USERNAME;

public class MainActivity2 extends AppCompatActivity implements FacultiesFragment.OnFragmentInteractionListener {
    public static final String  ATT_COURSE = "Course";
    public static final String  ATT_TUTOR_FP = "Authenticate tutor";
    public static final String ATD_TUTOR_FP = "Tutor FPs";
    public static final String STUDENT_MATCH = "Authenticate student";
    public static final String  ATT_STDs_FPs = "Students FPs";
    public static final String  ATT_STDs_NOs = "Students NOs";
    public static final String  ATT_STDs_IDs = "Attendance IDs";

    private DrawerLayout drawer;
    private Set<String[]> orgData = new HashSet<>();
    private List<AgentCourse> allCourses;
    private NavigationView navigationView;
    private CourseViewModel mCourseViewModel;
    private boolean isSubmitting, Submitted;
    private ImageView headerImageView;
    Toolbar toolbar;
    public static final String AVAILABLE_JOBS = "Available";
    public static final String COMPLETED_JOBS = "Completed";
    public static final String JOBS_FILE = "Jobs file";
    public static final String NO_DATA = "No_Data";
    private List<AttendanceCourse> attendanceCourses;
    private List<EnrolCourse> enrolCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        headerImageView = findViewById(R.id.imageView);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        mCourseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        LiveData<List<AgentCourse>> allCoursesLiveData = mCourseViewModel.getAllAgentCourses();
        LiveData<List<AttendanceCourse>> attLiveData = mCourseViewModel.getAllAttendanceCourses();
        LiveData<List<EnrolCourse>> enrolLiveData = mCourseViewModel.getAllEnrolCourses();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        /*Bundle b = new Bundle();
        b.putString("name", "isa");
        navController.setGraph(R.navigation.mobile_navigation, b);*/
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);
        NavigationUI.setupWithNavController(navigationView, navController);


        Intent loginIntent = getIntent();
        if (loginIntent.hasExtra("data")) {
            if (loginIntent.getExtras().get("data") != null) {
                Object[] availsObject = (Object[]) loginIntent.getExtras().get("data");
                for (Object obj : availsObject) {
                    orgData.add((String[]) obj);
                }

                /*if (availJobs.size()>0){
                    saveData(availJobs, AVAILABLE_JOBS);
                }*/
            }
        }

        enrolLiveData.observe(this, enrolCourses1 -> {
            enrolCourses = enrolCourses1;
            updateBarImage();
        });

        attLiveData.observe(this, attendanceCourses1 -> {
            if (attendanceCourses1 != null) {
                attendanceCourses = attendanceCourses1;
                updateBarImage();
            }
        });

        allCoursesLiveData.observe(this, agentCourses -> allCourses = agentCourses);

    }

    private void updateBarImage() {
            if (isRecordSubmitted()) {
                navigationView.getHeaderView(0).setBackgroundColor(getResources().getColor(R.color.myGreen, null));
                Submitted = true;
            } else {
                navigationView.getHeaderView(0).setBackgroundColor(Color.RED);
                Submitted = false;
            }

    }
    private boolean isRecordSubmitted() {
        int count = 0;
        if (attendanceCourses != null) {
            for (AttendanceCourse course : attendanceCourses) {
                if (course.getSub_Status() == AttendanceCourse.Sub_Status.unsubmitted &&
                        course.getCap_status().equals(AttendanceCourse.Cap_Status.captured) &&
                        !course.getAdmNo().equals("null")) count++;
            }
        }
        if (enrolCourses != null) {
            for (EnrolCourse course : enrolCourses) {
                if (course.getSub_status() == EnrolCourse.Sub_Status.unsubmitted) count++;
            }
        }

        return count<1;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() != null){
        switch (item.getTitle().toString()){
            case "Submit Record":
                if (!isWifiCxd()){
                    Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
                } else if (!isSubmitting){
                    submitRecord();
                }
                break;
            case "Register Tutor":
              if (!isSubmitting) {
                  Intent i = new Intent(this, RegisterTutor.class);
                  startActivity(i);
              }
                break;
            case "Logout":
                if (!Submitted){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                    builder.setMessage("Submit record?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                                if (isWifiCxd()){
                                    submitRecord();
                                    dialog.cancel();
                                }
                            }
                    );

                    builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                    AlertDialog dialog = builder.create();

                    dialog.show();
                } else {
                    finish();
                }
                break;
        }
        return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, drawer)
                || super.onSupportNavigateUp();
    }

    public void saveData(Set<String> data, String Name) {
        SharedPreferences sharedPref = getSharedPreferences(JOBS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(Name, data);
        editor.apply();
    }

    private String getData(String Name){
        SharedPreferences preferences = this.getSharedPreferences(JOBS_FILE, MODE_PRIVATE);
        return preferences.getString(Name, NO_DATA);
    }
    public boolean isWifiCxd() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo!=null && networkInfo.isConnected();

    }
    void  updateAgentCourseSub() throws JSONException {
        Set<AttendanceCourse> att_crs = new HashSet<>(attendanceCourses);
        int attCount = 0;
        for (AgentCourse agCourse: allCourses) {
            for (AttendanceCourse course:att_crs) {
                if (course.getCourse().equals(agCourse.getCourse()) && course.getCap_status().equals(AttendanceCourse.Cap_Status.captured)) {
                    agCourse.setSub_status(AgentCourse.Sub_Status.submitted);
                    mCourseViewModel.update(agCourse);
                    att_crs.remove(course);
                    attCount++;
                    break;
                }
            }
        }
        Set<String> enrol_crs = new HashSet<>();
        for (EnrolCourse crs : enrolCourses) {
            JSONArray crs_arr = new JSONArray(crs.getCourse());
            for (int i=0; i<crs_arr.length(); i++) {
                enrol_crs.add(crs_arr.getString(i));
            }
        }
        for (AgentCourse agCourse: allCourses) {
            for (String course:enrol_crs) {
                if (course.equals(agCourse.getCourse())) {
                    agCourse.setSub_status(AgentCourse.Sub_Status.submitted);
                    mCourseViewModel.update(agCourse);
                    enrol_crs.remove(course);
                    break;
                }
            }
        }
        if (enrol_crs.size()==0 && attCount>0) updateBarImage();
    }
    public String removeSpace(String course){
        String[] arr = course.split(" ");
        StringJoiner joiner = new StringJoiner("_");
        for (String sub : arr) {
            joiner.add(sub);
        }
        return joiner.toString();
    }
    private JSONArray prepareRecordJsonArray() throws JSONException {
        JSONArray attArray = new JSONArray();

        for (EnrolCourse course:enrolCourses) {
            if (enrolCourses.size() == 0){
                break;
            }else{
                if (course.getFP() != null){
                    // for new courses
                    attArray.put(new JSONObject().put("enrol_course", new JSONArray(course.getCourse()))
                            .put("AdmNo", course.getAdmNo())
                            .put("fp", Base64.encodeToString(course.getFP(), Base64.DEFAULT))
                            .put("IDImg", Base64.encodeToString(course.getIDImg(), Base64.DEFAULT))
                    );
                } else {
                    //for added courses
                    attArray.put(new JSONObject().put("enrol_course", new JSONArray(course.getCourse()))
                            .put("AdmNo", course.getAdmNo()));
                }

            }

        }
        for (AttendanceCourse course:attendanceCourses) {
            if (attendanceCourses.size() == 0){
                break;
            }else if(course.getCap_status().equals(AttendanceCourse.Cap_Status.captured) && course.getAdmNo() != null) {

                attArray.put(new JSONObject().put("att_course", removeSpace(course.getCourse()))
                        .put("AdmNo", course.getAdmNo())
                        .put("date", course.getDate()).put("period", course.getPeriod()));
            }
        }
        attArray.put(new JSONObject().put("uname", getUsername()));
        attArray.put(new JSONObject().put("code", getCode()));
        return attArray;
    }

    private String getCode() {
            SharedPreferences preferences = this.getSharedPreferences("code file", MODE_PRIVATE);
            return preferences.getString("code", NO_DATA);
    }

    private String getUsername(){
        SharedPreferences preferences = this.getSharedPreferences(ID_FILE, MODE_PRIVATE);
        return preferences.getString(USERNAME, NO_DATA);
    }
    void submitRecord(){
         class SubmitRecordAsynTask extends AsyncTask<Void, Void, Boolean> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                toolbar.setSubtitle("Loading...");
                isSubmitting = true;
            }

            @Override
            protected void onPostExecute(Boolean o) {
                super.onPostExecute(o);
                if (o) {
                    try {
                        updateAgentCourseSub();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (AttendanceCourse course : attendanceCourses) {
                        if (course.getCap_status().equals(AttendanceCourse.Cap_Status.captured)) {
                            course.setSub_Status(AttendanceCourse.Sub_Status.submitted);
                            mCourseViewModel.update(course);
                        }
                    }
                    for (EnrolCourse course : enrolCourses) {
                        course.setSub_status(EnrolCourse.Sub_Status.submitted);
                        mCourseViewModel.update(course);
                    }
                    Toast.makeText(MainActivity2.this, "Record successfully submitted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity2.this, "Error occurred, try again later.", Toast.LENGTH_SHORT).show();
                }
                isSubmitting = false;
                toolbar.setSubtitle("");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                String urlString = "http://www.gstcbunza2012.org.ng/fas/c_register.php";

                try {
                    URL url = new URL(urlString);
                    try {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setRequestProperty("Content-type", "application/json");
                        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpURLConnection.setReadTimeout(15000);
                        httpURLConnection.setConnectTimeout(15000);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.connect();
                        JSONArray array = prepareRecordJsonArray();
                        JSONObject obj = new JSONObject().put("root", array);
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));

                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                        StringBuilder result = new StringBuilder();
                        String line;

                        while ((line = bufferedReader.readLine()) != null) {
                            result.append(line);
                        }
                        String rslt = result.toString().trim();
                        if (rslt.equals("Data successfully submitted")) {
                            return true;
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        }

        SubmitRecordAsynTask submit;
        submit = new SubmitRecordAsynTask();
        submit.execute();

    }

    @Override
    public void onBackPressed(boolean backPressed) {
        if (backPressed){
            if (!Submitted){
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                builder.setMessage("Submit record?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                            if (isWifiCxd()){
                                submitRecord();
                                dialog.cancel();
                            }
                        }
                );

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.cancel();
                    finish();
                });

                AlertDialog dialog = builder.create();

                dialog.show();
            } else {
                finish();
            }
        }
    }

    private void deleteCourses(){
        WorkManager manager = WorkManager.getInstance(getApplicationContext());

    }

    public class DeleteCoursesWorker extends Worker{
        Calendar cal = Calendar.getInstance();

        public DeleteCoursesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            if (cal.get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY) {
                mCourseViewModel.deleteAllAgentCourses();
            }
            return Result.success();
        }
    }
}