package com.example.attendancemgr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendancemgr.database.AgentCourse;
import com.example.attendancemgr.database.AttendanceCourse;
import com.example.attendancemgr.database.CourseViewModel;
import com.example.attendancemgr.database.EnrolCourse;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class enrolActivity extends AppCompatActivity {
private static String chosenCourse, admNo, receivedCourse,  MODE;
EditText admNoView;
Spinner coursesSpinner, deptSpinner, facultySpinner;
LiveData<List<AttendanceCourse>> attLiveData;

TextView selectedCourses;
Button snapButton;
private Map<String, Map<String, ArrayList<String>>> facsMap;
private Map<String, ArrayList<String>> selectedFacultyMap;
private ArrayList<String> selectedDeptCourses;
private List<AgentCourse> allCourses;
private List<AttendanceCourse> attendanceCourses;

private ArrayAdapter<String> deptSpinnerAdapter, coursesAdapter;
private final String ADD = "Add courses";
private final String NEW = "new enrollment";
    private Toolbar mToolbar;
private byte[] IDCardbytes;
private List<String> coursesList;

    private final ActivityResultCallback<ActivityResult> scanCallback = result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            MODE = NEW;
            mToolbar.setSubtitle("New Enrollment");
            selectedCourses.setText("");
            snapButton.setText(R.string.capture_id);
            Intent resultIntent = result.getData();
            Object[] resultObj = (Object[]) resultIntent.getExtras().get("Result Object");
            processResult(resultObj);
            admNoView.setText("");

        }
        if (result.getData().hasExtra("BTAddress")) saveAddress((String) result.getData().getExtras().get("BTAddress"));

    };
    private final ActivityResultCallback<Bitmap> snapCallback = result -> {
        if(result!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.PNG, 100, stream);
            IDCardbytes = stream.toByteArray();
            launchFPScan();
        }
    };
private boolean  isFirst = true;
private String[][] un_enrolled;
private CourseViewModel mCourseViewModel;
private final String[] Courses = {"", "PHY101", "PHY102"};
ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), scanCallback);
ActivityResultLauncher<Void> snapLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), snapCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrol);

        if (getIntent().hasExtra("chosenCourse")) receivedCourse = getIntent().getExtras().getString("chosenCourse");
        if (getIntent().hasExtra("Un_enrolled")) un_enrolled = (String[][]) getIntent().getExtras().get("Un_enrolled");
        mCourseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        LiveData<List<AgentCourse>> allCoursesLiveData = mCourseViewModel.getAllAgentCourses();

        coursesList = new LinkedList<>();

        admNoView = findViewById(R.id.adm_No);
        coursesSpinner = findViewById(R.id.coursesSpinnerEnrol);
        selectedCourses = findViewById(R.id.selectedCourses);
        snapButton = findViewById(R.id.captureIDbutton);
        deptSpinner = findViewById(R.id.deptSpinnerEnrol);
        facultySpinner = findViewById(R.id.facultySpinnerEnrol);
        mToolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setBackgroundResource(R.color.black);
        mToolbar.setSubtitle("New Enrollment");
        facsMap = new HashMap<>();
        MODE = NEW;
        attLiveData = mCourseViewModel.getAllAttendanceCourses();

        attLiveData.observe(enrolActivity.this, attendanceCourses1 -> attendanceCourses = attendanceCourses1);


        ArrayAdapter<String> facSpinnerAdapter = new ArrayAdapter<>(enrolActivity.this, R.layout.faculty_spinner_text);
        deptSpinnerAdapter = new ArrayAdapter<>(enrolActivity.this, R.layout.dept_spinner_text);
        coursesAdapter = new ArrayAdapter<>(enrolActivity.this, R.layout.spinner_text);

        facultySpinner.setAdapter(facSpinnerAdapter);
        facSpinnerAdapter.insert("Select faculty", 0);

        deptSpinnerAdapter.add("Select department");
        deptSpinner.setAdapter(deptSpinnerAdapter);
        deptSpinnerAdapter.setNotifyOnChange(true);

        coursesAdapter.add("Select course");
        coursesSpinner.setAdapter(coursesAdapter);
        coursesAdapter.setNotifyOnChange(true);

        facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i>0){
                    if (facsMap.containsKey((String) facultySpinner.getAdapter().getItem(i))){
                        selectedFacultyMap = facsMap.get((String) facultySpinner.getAdapter().getItem(i));

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
        deptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i>0){
                    if (selectedFacultyMap.containsKey(deptSpinnerAdapter.getItem(i))) {
                        selectedDeptCourses = selectedFacultyMap.get(deptSpinnerAdapter.getItem(i));
                        coursesAdapter.clear();
                        coursesAdapter.add("Select Course");
                        coursesAdapter.addAll(selectedDeptCourses);
                        coursesAdapter.notifyDataSetChanged();
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
                   String viewText = selectedCourses.getText().toString();
                   if (!viewText.isEmpty() && !coursesList.contains(chosenCourse)) {
                       String concat = viewText + " - " + chosenCourse;
                       selectedCourses.setText(concat);
                       coursesList.add(chosenCourse);
                   } else if (coursesList.isEmpty()) {
                       selectedCourses.setText(chosenCourse);
                       coursesList.add(chosenCourse);
                   } else if (viewText.contains(chosenCourse)) {
                       Toast.makeText(enrolActivity.this, "Course has already been selected", Toast.LENGTH_LONG).show();
                   }
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
                facs.add(obj.toString());
            }
            facSpinnerAdapter.addAll(facs);
            facSpinnerAdapter.notifyDataSetChanged();
        });
        selectedCourses.setOnLongClickListener(view -> {
            selectedCourses.setText("");
            return true;
        });
        snapButton.setOnClickListener(view -> {
            if (MODE.equals(NEW)) {
                if (admNoView.getText().length() != 0 && selectedCourses.getText().length() != 0 /*&& !pb_is_Visible*/) {
                    admNo = admNoView.getText().toString();
                    snapLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Admission number or courses field empty!", Toast.LENGTH_SHORT).show();
                }

            }else if (MODE.equals(ADD)) {
                if (admNoView.getText().length() != 0 && selectedCourses.getText().length() != 0) {
                    admNo = admNoView.getText().toString();
                    addCourses();
                }else {
                    Toast.makeText(this, "Admission number or courses field empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });

       /* admNoView.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (!statusView.getText().toString().isEmpty()) {statusView.setText(""); return true;}
            return false;
        });*/

        mToolbar.setOnMenuItemClickListener(onMenuItemClick);

    }
    private final Toolbar.OnMenuItemClickListener onMenuItemClick = menuItem -> {

        switch (menuItem.getTitle().toString()){
            case "New":
                mToolbar.setSubtitle("New Enrollment");
                mToolbar.setSubtitleTextColor(Color.WHITE);
                snapButton.setText(R.string.capture_id);
                MODE = NEW;
                break;
            case "Add":
                mToolbar.setSubtitle("Add courses");
                snapButton.setText("Add");
                mToolbar.setSubtitleTextColor(Color.WHITE);
                MODE = ADD;
                break;
        }
        return true;
    };
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

                if (deptCourses.size()>0) deptMap.put(dep, deptCourses);
            }
            facMap.put(fac, deptMap);
        }

        facsMap = facMap;
    }


    private void processResult(Object[] resultObj){
        isFirst = (boolean) resultObj[1];
        if ((int) resultObj[2] < 0) {
                mCourseViewModel.insert(new EnrolCourse(Arrays.toString(coursesList.toArray()),
                        admNoView.getText().toString(),
                        (byte[]) resultObj[0],
                        EnrolCourse.Sub_Status.unsubmitted,
                        IDCardbytes));

        } else {
                mCourseViewModel.insert(new EnrolCourse(Arrays.toString(coursesList.toArray()),
                        admNoView.getText().toString(),
                        (byte[]) resultObj[0],
                        EnrolCourse.Sub_Status.unsubmitted,
                        IDCardbytes));
            String date = null;
            int period = 0;
            if (receivedCourse != null)
                for (AttendanceCourse course: attendanceCourses) {
                    if ((int) resultObj[2] == course.getId()){
                        date = course.getDate();
                        period = course.getPeriod();
                    }
                }
              if (period>0){ mCourseViewModel.update(new AttendanceCourse((int) resultObj[2],
                        receivedCourse,
                        admNo,
                        (byte[]) resultObj[0],
                        AttendanceCourse.Sub_Status.unsubmitted,
                        AttendanceCourse.Cap_Status.captured, date, period));
              }
        }
        mToolbar.setSubtitle("Success!");
        mToolbar.setSubtitleTextColor(Color.GREEN);
        coursesList.clear();

    }
    private void launchFPScan(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage("com.fgtit.reader");
        if(intent.resolveActivity(enrolActivity.this.getPackageManager())!=null) {
        }
        if (un_enrolled!=null && isFirst) intent.putExtra("un_enrolled", un_enrolled);
        if (!getAddress().equals("No_Address")) intent.putExtra("bt_Address", getAddress());
        intent.putExtra("Enrol student", true);
        scanLauncher.launch(intent);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void addCourses(){
        mCourseViewModel.insert(new EnrolCourse(Arrays.toString(coursesList.toArray()),
                admNo,
                null,
                EnrolCourse.Sub_Status.unsubmitted,
                null));
        admNoView.setText("");
        selectedCourses.setText("");
    }
}