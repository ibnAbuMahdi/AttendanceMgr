package com.example.attendancemgr;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.attendancemgr.database.AgentCourse;
import com.example.attendancemgr.database.CourseViewModel;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegisterTutor extends AppCompatActivity {
androidx.appcompat.widget.AppCompatSpinner coursesSpinner, facultiesSpinner, deptsSpinner;
private List<String> tutorCourses;
private EditText OTPText;
private final ActivityResultCallback<ActivityResult> tutorScanCallback = result -> {
        if (result.getResultCode() == RESULT_OK) {
            storeCourseTutor(result.getData().getByteArrayExtra("tutorBytes"), tutorCourses);
            OTPText.setText("");
        }

    assert result.getData() != null;
    if (result.getData().hasExtra("BTAddress")) saveAddress((String) result.getData().getExtras().get("BTAddress"));

};
private boolean isVisible;
private String chosenCourse = "", trueOTP;
private final String PHONE = "phone number";
private final String OTP = "otp";
private List<AgentCourse> allCourses;
private String authStage, phoneNumber;
private Map<String, Map<String, ArrayList<String>>> facsMap;
private Map<String, ArrayList<String>> selectedFacultyMap;
private ArrayList<String> selectedDeptCourses;
TextView coursesView, statusView;
private ArrayAdapter<String> facSpinnerAdapter;
private ArrayAdapter<String> deptSpinnerAdapter;
ProgressBar pb;
Button scanButton;
private ActivityResultLauncher<Intent> enrolTutorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), tutorScanCallback);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_tutor);
        coursesSpinner = findViewById(R.id.availableCoursesSpinner);
        facultiesSpinner = findViewById(R.id.facultySpinner);
        deptsSpinner = findViewById(R.id.deptSpinner);
        coursesView = findViewById(R.id.selectedCourses);
        scanButton = findViewById(R.id.scanFingerButton);
        OTPText = findViewById(R.id.OTPEditText);
        pb = findViewById(R.id.progressBar);
        statusView = findViewById(R.id.statusTextView);
        CourseViewModel mCourseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        LiveData<List<AgentCourse>> allCoursesLiveData = mCourseViewModel.getAllAgentCourses();
        scanButton.setText("next");
        tutorCourses = new LinkedList<>();
        pb.setVisibility(View.INVISIBLE);

        facsMap = new HashMap<>();

        allCoursesLiveData.observe(this, agentCourses -> {
            allCourses = agentCourses;
            initMaps();
        });


        facSpinnerAdapter = new ArrayAdapter<>(RegisterTutor.this, R.layout.faculty_spinner_text);
        deptSpinnerAdapter = new ArrayAdapter<>(RegisterTutor.this, R.layout.dept_spinner_text);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterTutor.this, R.layout.spinner_text);

        facSpinnerAdapter.insert("Select faculty", 0);
        facultiesSpinner.setAdapter(facSpinnerAdapter);
        deptSpinnerAdapter.add("Select department");
        deptsSpinner.setAdapter(deptSpinnerAdapter);
        deptSpinnerAdapter.setNotifyOnChange(true);

        adapter.add("Select course");
        coursesSpinner.setAdapter(adapter);
        adapter.setNotifyOnChange(true);

        facultiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i>0){
                    if (facsMap.containsKey((String) facultiesSpinner.getAdapter().getItem(i))){
                         selectedFacultyMap = facsMap.get((String) facultiesSpinner.getAdapter().getItem(i));

                        Set<String> deptsSet = selectedFacultyMap.keySet();
                        deptSpinnerAdapter.clear();
                        deptSpinnerAdapter.add("Select Department");
                        deptSpinnerAdapter.addAll(deptsSet);
                        deptSpinnerAdapter.notifyDataSetChanged();

                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        deptsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i>0){
                    if (selectedFacultyMap.containsKey(deptSpinnerAdapter.getItem(i))) {
                         selectedDeptCourses = selectedFacultyMap.get(deptSpinnerAdapter.getItem(i));
                        adapter.clear();
                        adapter.add("Select Course");
                        adapter.addAll(selectedDeptCourses);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        coursesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               if (position>0) {
                   chosenCourse = selectedDeptCourses.get(position-1);
                   String space = " " + chosenCourse;
                   String viewText = coursesView.getText().toString();
                   if (!viewText.isEmpty() && !viewText.contains(chosenCourse)) {
                       String concat = viewText + " - " + chosenCourse;
                       coursesView.setText(concat);
                       tutorCourses.add(chosenCourse);
                   } else if (viewText.isEmpty()) {
                       coursesView.setText(space);
                       tutorCourses.add(chosenCourse);
                   } else if (viewText.contains(chosenCourse)) {
                       Toast.makeText(RegisterTutor.this, "Course has already been selected", Toast.LENGTH_LONG).show();
                   }
                   scanButton.setText("next");
               }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        allCoursesLiveData.observe(this, agentCourses -> {
            allCourses = agentCourses;
            initMaps();
            ArrayList<String> facs = new ArrayList<>();
            for (Object obj : facsMap.keySet()) {
                facs.add( obj.toString());
            }
            facSpinnerAdapter.addAll(facs);
            facSpinnerAdapter.notifyDataSetChanged();
        });
        coursesView.setOnLongClickListener(view -> {
            coursesView.setText("");
            return true;
        });
        OTPText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (authStage.equals(PHONE)) {


                    if (charSequence.length() == 11) {
                        authStage =
                        phoneNumber = charSequence.toString();
                        OTPText.setText("");
                        authStage = OTP;
                        scanButton.setText("get PIN");
                        getOTP(chosenCourse, phoneNumber);

                    }
                } else {
                    if(charSequence.length() == 6){
                        if (trueOTP.contentEquals(charSequence.toString())){
                            scanButton.setText("Scan");
                            scanTutor(scanButton);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void initMaps() {
        Set<String> facSet = new HashSet<>();
        Set<String> depSet = new HashSet<>();
        for (AgentCourse course : allCourses) {
            facSet.add(course.getFaculty());
            depSet.add(course.getDept());
        }
        Map<String, Map<String, ArrayList<String>>> facMap = new HashMap<>();

        for (String fac : facSet) {
            Map<String, ArrayList<String>> deptMap = new HashMap<>();
            for (String dep : depSet) {
                ArrayList<String> deptCourses = new ArrayList<>();
                for (AgentCourse course : allCourses) {
                    if (dep.equals(course.getDept()) && fac.equals(course.getFaculty())){
                        deptCourses.add(course.getCourse());
                    }
                }

                if (deptCourses.size()>0) deptMap.put(dep,  deptCourses);
            }
            facMap.put(fac, deptMap);
        }

        facsMap = facMap;
    }

    public void scanTutor(View v) {
        if (isWifiCxd()) {
            if (coursesView.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
            } else {
                if (scanButton.getText().equals("Scan") && !isVisible) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.fgtit.reader");
   /* if(intent.resolveActivity(RegisterTutor.this.getPackageManager())!=null) {
    }*/
                    intent.putExtra("Enrol tutor", true);
                    if (!getAddress().equals("No_Address"))
                        intent.putExtra("bt_Address", getAddress());

                    enrolTutorLauncher.launch(intent);
                } else if (scanButton.getText().equals("next") && !isVisible && !chosenCourse.equals("")){
                    OTPText.setVisibility(View.VISIBLE);
                    OTPText.setHint("0XXX - XXX - XXXX");
                    statusView.setVisibility(View.VISIBLE);
                    statusView.setText("Please input phone number");
                    authStage = PHONE;
                }
            }
        } else {
            Toast.makeText(this, "Please connect to the network.", Toast.LENGTH_SHORT).show();
        }
    }
    private void getOTP(String... courses){
        class getOTPAsynctask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                statusView.setText("Loading...");
                scanButton.setEnabled(false);
                isVisible = true;
            }

            @Override
            protected void onPostExecute(String otp) {
                super.onPostExecute(otp);
                if (otp!=null) trueOTP = otp;
                OTPText.setVisibility(View.VISIBLE);
                OTPText.setHint("XXXXXX");
                statusView.setText("Please input PIN code");
                scanButton.setEnabled(true);
                isVisible = false;
            }


            @Override
            protected String doInBackground(String... strings) {
                String login_url = "http://www.gstcbunza2012.org.ng/fas/otp_data.php";

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
                        String post_data = URLEncoder.encode("course", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8") + "&" +
                                URLEncoder.encode("phone", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");
                        bufferedWriter.write(post_data);
                        bufferedWriter.flush();
                        bufferedWriter.close();

                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                        StringBuilder result = new StringBuilder();
                        String line;

                        while ((line = bufferedReader.readLine()) != null) {
                            result.append(line);
                        }
                        String rslt = result.toString().trim();

                        if (rslt.length()==6) {
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
                return null;
            }
        }
        getOTPAsynctask getOTPAsynctask = new getOTPAsynctask();
        getOTPAsynctask.execute(courses);
    }
    private void storeCourseTutor(byte[] result, List<String> tutorCourses) {

        StringBuilder Courses = new StringBuilder();
        for (int i=0; i<tutorCourses.size(); i++) {
            if (i==0) {
                Courses.append(tutorCourses.get(i));
            } else {
                Courses.append(",").append(tutorCourses.get(i));
            }
        }

       // String data = "{\"Fingerprint\":\"" + Base64.encodeToString(result, Base64.DEFAULT) + "\", \"courses\":\"" + Courses.toString() + "\"}";
        String[] data = {phoneNumber, Base64.encodeToString(result, Base64.DEFAULT), Courses.toString()};
        class UploadFP extends AsyncTask<String, Void, Boolean> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb.setVisibility(View.VISIBLE);
                isVisible = true;
            }

            @Override
            protected void onPostExecute(Boolean saved) {
                super.onPostExecute(saved);
                if (saved){
                    Toast.makeText(RegisterTutor.this, "fine!", Toast.LENGTH_SHORT).show();
                    statusView.setTextColor(Color.GREEN);
                    statusView.setText("Data uploaded successfully!");
                    for (String course : tutorCourses) {
                        SharedPreferences sharedPref = getSharedPreferences(course, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("tutorFP", Base64.encodeToString(result, Base64.DEFAULT));
                        editor.apply();
                    }
                } else {
                    statusView.setTextColor(Color.RED);
                    statusView.setText("Data upload failed!");
                }
                pb.setVisibility(View.INVISIBLE);
                isVisible = false;
            }


            @Override
            protected Boolean doInBackground(String... strings) {
                String login_url = "http://www.gstcbunza2012.org.ng/fas/tutor.php";

                try {
                    java.net.URL url = new URL(login_url);
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

                       JSONObject obj = new JSONObject();
                       obj.put("phone", strings[0]);
                       obj.put("Fingerprint", strings[1]);
                       obj.put("courses", strings[2]);

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
                        if (rslt.equals("fine")) {
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
        UploadFP uploadFP = new UploadFP();
        uploadFP.execute(data);
    }

    public boolean isWifiCxd() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo!=null && networkInfo.isConnected();

    }

    public void saveAddress(String mAddress) {
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.AddressFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Address", mAddress);
        editor.apply();
    }
    private String getAddress(){
        SharedPreferences preferences = this.getSharedPreferences(MainActivity.AddressFile, MODE_PRIVATE);
        String defaultAddress = "No_Address";
        return preferences.getString("Address", defaultAddress);
    }
}