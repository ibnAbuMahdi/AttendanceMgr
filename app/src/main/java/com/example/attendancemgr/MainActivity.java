package com.example.attendancemgr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendancemgr.ui.CourseListAdapter;
import com.example.attendancemgr.database.AgentCourse;
import com.example.attendancemgr.database.AttendanceCourse;
import com.example.attendancemgr.database.CourseViewModel;
import com.example.attendancemgr.database.EnrolCourse;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private final String[] Courses = {"Select a course","PHY101", "PHY102"};
    private String chosenCourse,  jsonAttendance, jsonEnrol;
    private final int DOWNLOADING = 1;
    private final int OPEN_ATTENDANCE_CODE = 2;
    private final int NOT_DOWNLOADING = 0;
    private LinkedList<byte[]> lecturerFP;
    private final String ATTENDANCE_RECORD = "Attendance list";
    private final String COURSE_CODE = "Course code";
    private final String  ATT_COURSE = "Course";
    private final String  ATT_TUTOR_FP = "Authenticate tutor";
    private final String ATD_TUTOR_FP = "Tutor FPs";
    private static final String STUDENT_MATCH = "Authenticate student";
    private final String  ATT_STDs_FPs = "Students FPs";
    public final String  ATT_STDs_NOs = "Students NOs";
    public final String  ATT_STDs_IDs = "Attendance IDs";
    public final String TUTOR_VALID = "is tutor valid?";
    private LinkedList<String> AdmNos;
    private boolean FPStatus, pb_is_Visible, isSubmitted, isSubmitting;
    private int downloadStatus = NOT_DOWNLOADING;
    private ProgressBar pb;
    private LinkedList<byte[]> studentsFP;
    private LinkedList<Integer> IDs;
    Button scanButton, regButton, submitRecordButton;
    private CourseListAdapter listAdapter;
    private CourseViewModel mCourseViewModel;
    private List<AgentCourse> mAgentCourses;
    private List<AttendanceCourse> attendanceCourses;
    private List<EnrolCourse> enrolCourses;
    private List<String> courseString;
    private final int TUTOR_OK = 234;
    private final int ATT_OK = 432;
    private int idOPen = -1, tempID;
    private AgentCourse openedCourse;
    public static final String AddressFile = "bt_address_file";

    private ActivityResultCallback<ActivityResult>  attCallback = result -> {

        //Pattern pat;
        //Matcher mat;
       if(result.getResultCode() == ATT_OK){ if(result.getData().hasExtra(ATT_STDs_NOs)) {
            for (String data : result.getData().getExtras().getStringArrayList(ATT_STDs_NOs)) {
                if (data.length() > 20) {
                    // ga wanda ya fara daukar attendance na farko kafin enrollment
                   /* mCourseViewModel.insert(new AttendanceCourse(chosenCourse,
                            null,
                            Base64.decode(data, Base64.DEFAULT),
                            AttendanceCourse.Sub_Status.unsubmitted,
                            AttendanceCourse.Cap_Status.captured));*/
                } else {
                   /* pat = Pattern.compile(data);
                    mat = pat.matcher("&");
                    if (mat.find()) {*/
                        String[] admNo_id = data.split("&");
                        // for the enrolled
                       /* mCourseViewModel.update(new AttendanceCourse(Integer.parseInt(admNo_id[0]),
                                chosenCourse,
                                admNo_id[1],
                                null,
                                AttendanceCourse.Sub_Status.unsubmitted,
                                AttendanceCourse.Cap_Status.captured));
*/
                    /*}else {
                                mCourseViewModel.insert(new AttendanceCourse(chosenCourse, data, null, AttendanceCourse.Sub_Status.unsubmitted, AttendanceCourse.Cap_Status.captured));
                            }*/
                }
            }
          if (openedCourse != null ){
              openedCourse.setAtt_status(AgentCourse.Att_Status.closed);
              mCourseViewModel.update(openedCourse);
          }

            if (result.getData().hasExtra("BTAddress")) saveAddress((String) result.getData().getExtras().get("BTAddress"));
        }}
    };

    ActivityResultCallback<ActivityResult> tutorScanCallback = result -> {
        if (result.getResultCode() == TUTOR_OK){
            for (AgentCourse course : mAgentCourses) {
                if (course.getCourse().equals(chosenCourse)) {
                    course.setAtt_status(AgentCourse.Att_Status.open);
                    openedCourse = course;
                }
                break;
            }
            getAtt_EnrolData(null);
        }
       if (result.getData() != null && result.getData().hasExtra("BTAddress")) saveAddress((String) result.getData().getExtras().get("BTAddress"));
    };
    public Spinner courseSpinner;
    ActivityResultLauncher<Intent> tutorScanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), tutorScanCallback);
    ActivityResultLauncher<Intent> attLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), attCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mCourseViewModel = new CourseViewModel(getApplication());
        mCourseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        courseSpinner = findViewById(R.id.selectCourseSpinner);
        LiveData<List<AgentCourse>> agentLiveData = mCourseViewModel.getAllAgentCourses();
        LiveData<List<AttendanceCourse>> attLiveData = mCourseViewModel.getAllAttendanceCourses();
        LiveData<List<EnrolCourse>> enrolLiveData = mCourseViewModel.getAllEnrolCourses();
        /*ActionBar bar = this.getActionBar();
        Toast.makeText(this, bar.getTitle(), Toast.LENGTH_SHORT).show();*/
        courseString = new LinkedList<>();

        if (openedCourse != null) mCourseViewModel.update(openedCourse);

        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_text);
        mAdapter.setNotifyOnChange(true);

        agentLiveData.observe(MainActivity.this, agentCourses -> {
            if (agentCourses != null){
                mAgentCourses = agentCourses;
                courseString.clear();
                for (AgentCourse course: agentCourses) {
                    courseString.add(course.getCourse());
                }
                mAdapter.clear();
                mAdapter.insert("Please select a course", 0);
                mAdapter.addAll(courseString);
                getAllRecords();
            }
        });

        Thread t = new Thread(() -> {
            if(mCourseViewModel.getAnyCourse().length<1) initAgentCourses();
        });
        t.start();

        courseSpinner.setAdapter(mAdapter);

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenCourse = mAdapter.getItem(position);
                if (!chosenCourse.equals(mAdapter.getItem(0))) {
                    if(idOPen != position && downloadStatus == NOT_DOWNLOADING){
                        if (chosenCourse!=null && isWifiCxd()) {
                            tempID = position;
                            getFP(chosenCourse);
                        } else {
                            Toast.makeText(MainActivity.this, "No Internet connection.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        getAtt_EnrolData(position);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Please select a course.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                chosenCourse = Courses[0];
            }
        });

        enrolLiveData.observe(this, enrolCourses1 ->{
            enrolCourses = enrolCourses1;
            updateSubmitButton();
        });

        attLiveData.observe(this, attendanceCourses1 -> {
            if (attendanceCourses1!=null){
               attendanceCourses = attendanceCourses1;
                updateSubmitButton();
            }
        });

        pb = findViewById(R.id.progressBarSelectCourse);
        pb.setVisibility(View.INVISIBLE);
        FPStatus = false;
        scanButton = findViewById(R.id.scanFPButton);
        regButton = findViewById(R.id.registerTutorButton);
        scanButton.setVisibility(View.INVISIBLE);
        submitRecordButton = findViewById(R.id.submitRecordButton);
        regButton.setBackgroundColor(Color.GREEN);
        //
        lecturerFP = new LinkedList<>();
        AdmNos = new LinkedList<>();
        studentsFP = new LinkedList<>();
        IDs = new LinkedList<>();

        scanButton.setOnClickListener(view -> {
            if (!pb_is_Visible && getLecturerFP() != null){
                scanTutorFP();
            }
        });

        regButton.setOnClickListener(view ->{
            if (!pb_is_Visible) {
                Intent intent = new Intent(MainActivity.this, RegisterTutor.class);
                startActivity(intent);
            }
        });

        // Update the cached copy of the words in the adapter.
        //adapter.notifyDataSetChanged();

        submitRecordButton.setOnClickListener(View -> {
            if (!isSubmitted && !pb_is_Visible && !isSubmitting){
                submitRecord();
            }
        });

    }

    private String[] convertToArray(List<AgentCourse> agentCourses) {
       ArrayList<String> courses = new ArrayList<>();
        for (AgentCourse course : agentCourses) {
            courses.add(course.getCourse());
        }
        return (String[]) courses.toArray();
    }

    private void scanTutorFP() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage("com.fgtit.reader");
            if (!getAddress().equals("No_Address"))
                intent.putExtra("bt_Address", getAddress());
            intent.putExtra(ATT_COURSE, chosenCourse);
            intent.putExtra(ATT_TUTOR_FP, getLecturerFP().toArray());
           tutorScanLauncher.launch(intent);

    }

    public LinkedList<byte[]> getLecturerFP() {
        SharedPreferences preferences = this.getSharedPreferences("Tutors", MODE_PRIVATE);
        String defaultData = "No_FP";
        String fpStrings = preferences.getString("tutorsFP", defaultData);
       if (!fpStrings.equals(defaultData)) {
           String[] fpStringsArray = fpStrings.split(" ");
           for (String fp : fpStringsArray) {
               lecturerFP.add(Base64.decode(fp, Base64.DEFAULT));
           }
       }
        return lecturerFP;
    }
    private void initAgentCourses(){
        final String [] courses = {"PHY101", "PHY401", "PHY102",
                "PHY302"};
        for (int i = 0 ; i <= courses.length - 1 ; i++) {
            AgentCourse course = new AgentCourse("Sciences","Physics", courses[i], AgentCourse.Att_Status.closed, AgentCourse.Sub_Status.unsubmitted);
            mCourseViewModel.insert(course);

        }
    }
    void getAllRecords(){
        attendanceCourses = mCourseViewModel.getAllAttendanceCourses().getValue();
        enrolCourses = mCourseViewModel.getAllEnrolCourses().getValue();
    }
    void updateSubmitButton(){
        if (isRecordSubmitted()){
            submitRecordButton.setBackgroundColor(Color.GREEN);
            isSubmitted = true;
        } else {
            submitRecordButton.setBackgroundColor(Color.RED);
            isSubmitted = false;
        }
    }
    private boolean isRecordSubmitted() {
        int count = 0;
        if (attendanceCourses != null) {
            for (AttendanceCourse course : attendanceCourses) {
                if (course.getSub_Status() == AttendanceCourse.Sub_Status.unsubmitted &&
                        course.getCap_status().equals(AttendanceCourse.Cap_Status.captured)) count++;
            }
        }
        if (enrolCourses != null) {
            for (EnrolCourse course : enrolCourses) {
                if (course.getSub_status() == EnrolCourse.Sub_Status.unsubmitted) count++;
            }
        }

        return count<1;
    }

    private void getFP(String chosenCourse) {
        class DownloadFP extends AsyncTask<Void, Void, String>{
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb.setVisibility(View.VISIBLE);
                downloadStatus = DOWNLOADING;
                pb_is_Visible = true;
            }

            @Override
            protected void onPostExecute(String data) {
                super.onPostExecute(data);
            if (data.equals("timeout")){
                    Toast.makeText(MainActivity.this, "Connection timeout!", Toast.LENGTH_SHORT).show();
                } else if (!data.isEmpty()){
                    try {
                        storeAttendanceData(data);
                        idOPen = tempID;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    scanButton.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(MainActivity.this, "Lecturer or course details incomplete!", Toast.LENGTH_SHORT).show();

                }
                pb.setVisibility(View.INVISIBLE);
                pb_is_Visible = false;
                downloadStatus = NOT_DOWNLOADING;

            }

            @Override
            protected String doInBackground(Void... voids) {

                String login_url = "http://www.gstcbunza2012.org.ng/fas/fetch_data.php";
                try {
                    java.net.URL url = new URL(login_url);
                    try {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setReadTimeout(15000);
                        httpURLConnection.setConnectTimeout(15000);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);

                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        String post_data = URLEncoder.encode("course", "UTF-8") + "=" + URLEncoder.encode(chosenCourse, "UTF-8");
                        bufferedWriter.write(post_data);
                        bufferedWriter.flush();
                        bufferedWriter.close();

                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                        StringBuilder result = new StringBuilder();
                        String line;
                        new Timer().schedule(new TimerTask() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void run() {
                                if (inputStream == null){
                                    onPostExecute("timeout");
                                    httpURLConnection.disconnect();

                                }
                            }

                        }, 15000);
                        while ((line = bufferedReader.readLine()) != null) {
                            result.append(line);
                        }
                        String rslt = result.toString().trim();
                        if (!rslt.isEmpty() & rslt.length()>20) {
                            return rslt;

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
                return "";
            }
        }
        DownloadFP downloadFP = new DownloadFP();
       if (isWifiCxd()) {
           downloadFP.execute();
       } else {
           Snackbar.make(MainActivity.this, submitRecordButton, "No network connection", Snackbar.LENGTH_LONG);
       }
    }

    private void storeAttendanceData(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        JSONObject tutorFP;

        StringBuilder builder = new StringBuilder();
        String tutorFPStrings;

          //  JSONObject object = jsonArray.getJSONObject(i);
            JSONArray tutorsArray = jsonArray.getJSONArray(0);

                for (int j=0; j<tutorsArray.length(); j++) {
                    tutorFP = tutorsArray.getJSONObject(j);
                    builder.append(tutorFP.getString("Fingerprint_staff")).append(" ");
                }
                tutorFPStrings = builder.toString().trim();
               // continue;


                JSONArray studentsArray = jsonArray.getJSONArray(1);
        for (int i=0; i<studentsArray.length(); i++) {
            JSONObject object = studentsArray.getJSONObject(i);
           /* mCourseViewModel.insert(new AttendanceCourse(chosenCourse,
                    object.getString("adnumber"),
                    Base64.decode(object.getString("Fingerprint_student"), Base64.DEFAULT),
                    AttendanceCourse.Sub_Status.unsubmitted,
                    AttendanceCourse.Cap_Status.uncaptured));*/

        }



        SharedPreferences sharedPref = getSharedPreferences("Tutors", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("tutorsFP", tutorFPStrings);
        editor.apply();

    }


    private String[][] getAllUn_enrolledAttendees(){
        String[][] temp = new String[attendanceCourses.size()][];
        int count = 0;
        for (int j=0; j<attendanceCourses.size(); j++){
            if (attendanceCourses.get(j).getAdmNo() == null){
                temp[j][0] = String.valueOf(attendanceCourses.get(j).getId());
                temp[j][1] = Base64.encodeToString(attendanceCourses.get(j).getFP(),Base64.DEFAULT);
                count++;
            }
        }
        if (count>0) return temp;
        return new String[0][0];
    }
    private boolean prepareAttData(){

        int count2 = 0;
        for (AttendanceCourse course : attendanceCourses) {
            if (course.getCap_status().equals(AttendanceCourse.Cap_Status.uncaptured)) {
                studentsFP.add(course.getFP());
                AdmNos.add(course.getAdmNo());
                IDs.add(course.getId());

            }
        }
        SharedPreferences preferences = this.getSharedPreferences("Tutors", MODE_PRIVATE);
        String defaultFP = "";
        String tutorsFP = preferences.getString("tutorsFP", defaultFP);
        String[] tutorsFPArray = tutorsFP.split(" ");
        for (String fp : tutorsFPArray) {
            lecturerFP.add(Base64.decode(fp, Base64.DEFAULT));
            count2++;
        }

        return count2>0;
    }
    private void getAtt_EnrolData(Integer position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("What would you like to do?");
        //builder.setTitle("Register Entry or Add Fingerprint");

        builder.setPositiveButton("Enrol Student", (dialog, which) -> {
            if(isSubmitting){
                Toast.makeText(this, "Please wait, system is busy.", Toast.LENGTH_SHORT).show();
            } else {
                String[][] un_enrolled = getAllUn_enrolledAttendees();
                Intent intent = new Intent(MainActivity.this, enrolActivity.class);
                if (un_enrolled.length!=0) intent.putExtra("Un_enrolled", un_enrolled);
                intent.putExtra("chosenCourse", chosenCourse);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Take attendance", (dialog, which) -> {
            if (position==null || mAgentCourses.get(position).getAtt_status().equals(AgentCourse.Att_Status.open)) {
                if (prepareAttData()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.fgtit.reader");
                        if (!getAddress().equals("No_Address"))
                            intent.putExtra("bt_Address", getAddress());

                        intent.putExtra(ATT_COURSE, chosenCourse);
                        intent.putExtra(ATD_TUTOR_FP, lecturerFP.toArray());
                        if (studentsFP != null) intent.putExtra(ATT_STDs_FPs, studentsFP.toArray());
                        if (AdmNos != null) intent.putExtra(ATT_STDs_NOs, AdmNos.toArray());
                        if (IDs != null) intent.putExtra(ATT_STDs_IDs, IDs.toArray());
                    intent.putExtra(STUDENT_MATCH, true);

                    attLauncher.launch(intent);
                    dialog.cancel();
                } else {
                    Toast.makeText(this, "An error has occured!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Attendance is closed for the course.", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private String getAddress(){
        SharedPreferences preferences = this.getSharedPreferences(AddressFile, MODE_PRIVATE);
        String defaultAddress = "No_Address";
        return preferences.getString("Address", defaultAddress);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    void  updateAgentCourseSub() throws JSONException {
        Set<AttendanceCourse> att_crs = new HashSet<>(attendanceCourses);
        int attCount = 0;
        for (AgentCourse agCourse: mAgentCourses) {
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
        for (AgentCourse agCourse: mAgentCourses) {
            for (String course:enrol_crs) {
                if (course.equals(agCourse.getCourse())) {
                    agCourse.setSub_status(AgentCourse.Sub_Status.submitted);
                    mCourseViewModel.update(agCourse);
                    enrol_crs.remove(course);
                    break;
                }
            }
        }
        if (enrol_crs.size()==0 && attCount>0) updateSubmitButton();
    }
    private JSONArray prepareRecordJsonArray() throws JSONException {
        JSONArray attArray = new JSONArray();
        for (AttendanceCourse course:attendanceCourses) {
            if (attendanceCourses.size() == 0){
                break;
            }else if(course.getCap_status().equals(AttendanceCourse.Cap_Status.captured)) {
                attArray.put(new JSONObject().put("att_course", course.getCourse()).put("AdmNo", course.getAdmNo()));
            }
        }
        for (EnrolCourse course:enrolCourses) {
            if (enrolCourses.size() == 0){
                break;
            }else{
                attArray.put(new JSONObject().put("enrol_course", new JSONArray(course.getCourse()))
                        .put("AdmNo", course.getAdmNo())
                        .put("fp", Base64.encodeToString(course.getFP(), Base64.DEFAULT))
                        .put("IDImg", Base64.encodeToString(course.getIDImg(), Base64.DEFAULT))
                );
            }

        }

    return attArray;
    }
    void submitRecord(){
        class SubmitRecordAsynTask extends AsyncTask<Void, Void, Boolean>{
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb.setVisibility(View.VISIBLE);
                isSubmitting = true;
            }

            @RequiresApi(api = Build.VERSION_CODES.R)
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
                    isSubmitting = false;
                    pb.setVisibility(View.GONE);
                }
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


                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        outputStream.write(prepareRecordJsonArray().toString().getBytes(StandardCharsets.UTF_8));

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

        SubmitRecordAsynTask submit = new SubmitRecordAsynTask();
        submit.execute();

    }
    public boolean isWifiCxd() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo!=null && networkInfo.isConnected();

    }
    public void saveAddress(String mAddress) {
        SharedPreferences sharedPref = getSharedPreferences(AddressFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Address", mAddress);

        editor.apply();
    }

}