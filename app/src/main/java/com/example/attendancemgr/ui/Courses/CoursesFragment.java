package com.example.attendancemgr.ui.Courses;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendancemgr.R;
import com.example.attendancemgr.ScanTutor;
import com.example.attendancemgr.data.model.DepartmentsModel;
import com.example.attendancemgr.database.AgentCourse;
import com.example.attendancemgr.database.AttendanceCourse;
import com.example.attendancemgr.database.CourseViewModel;
import com.example.attendancemgr.enrolActivity;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static com.example.attendancemgr.MainActivity2.ATD_TUTOR_FP;
import static com.example.attendancemgr.MainActivity2.ATT_COURSE;
import static com.example.attendancemgr.MainActivity2.ATT_STDs_FPs;
import static com.example.attendancemgr.MainActivity2.ATT_STDs_IDs;
import static com.example.attendancemgr.MainActivity2.ATT_STDs_NOs;
import static com.example.attendancemgr.MainActivity2.NO_DATA;
import static com.example.attendancemgr.MainActivity2.STUDENT_MATCH;
import static com.example.attendancemgr.ui.login.LoginActivity.AddressFile;
import static com.example.attendancemgr.ui.login.LoginActivity.ID_FILE;
import static com.example.attendancemgr.ui.login.LoginActivity.USERNAME;

public class CoursesFragment extends Fragment {
    LiveData<List<AgentCourse>> orgLiveData;
    private CourseViewModel mCourseViewModel;
    List<DepartmentsModel> itemsModelList = new ArrayList<>();
    String chosenCourse;
    RecyclerAdapter mAdapter;
    String mDep;
    View view;
    RecyclerView mRecyclerView;
    ProgressBar pb;
    AgentCourse openedCourse;
    boolean isDownloading;
    private List<AgentCourse> mAgentCourses;
    private List<AttendanceCourse> attendanceCourses;
    public static final int TUTOR_OK = 234;
    private final int ATT_OK = 432;
    private LinkedList<Integer> IDs;
    private LinkedList<String> AdmNos;

    LiveData<List<AttendanceCourse>> attLiveData;
    private LinkedList<byte[]> studentsFP, lecturersFP;

    Bundle args;
    ActivityResultCallback<ActivityResult> tutorScanCallback = result -> {
        if (result.getResultCode() == TUTOR_OK){
            if(result.getData().hasExtra("period")){

                savePeriod(result.getData().getExtras().getInt("period"));
                for (AgentCourse course : mAgentCourses) {
                    if (course.getCourse().equals(chosenCourse)) {
                        course.setAtt_status(AgentCourse.Att_Status.open);
                        openedCourse = course;
                    }
                    break;
                }
                getAtt_EnrolData(null);
            }
        }
    };


    private ActivityResultCallback<ActivityResult>  attCallback = result -> {

        //Pattern pat;
        //Matcher mat;
        if(result.getResultCode() == ATT_OK){
            if(result.getData().hasExtra(ATT_STDs_NOs)) {
            for (String data : result.getData().getExtras().getStringArrayList(ATT_STDs_NOs)) {
                if (data.length() > 20) {
                    // ga wanda ya fara daukar attendance na farko kafin enrollment
                    mCourseViewModel.insert(new AttendanceCourse(chosenCourse,
                            null,
                            Base64.decode(data, Base64.DEFAULT),
                            AttendanceCourse.Sub_Status.unsubmitted,
                            AttendanceCourse.Cap_Status.captured,
                            new SimpleDateFormat("yyyyMMdd").format(new Date()), getPeriod()));
                } else {
                   /* pat = Pattern.compile(data);
                    mat = pat.matcher("&");
                    if (mat.find()) {*/
                    String[] admNo_id = data.split("&");
                    // for the enrolled

                    mCourseViewModel.update(new AttendanceCourse(Integer.parseInt(admNo_id[0]),
                            chosenCourse,
                            admNo_id[1],
                            null,
                            AttendanceCourse.Sub_Status.unsubmitted,
                            AttendanceCourse.Cap_Status.captured,
                            new SimpleDateFormat("yyyyMMdd").format(new Date()), getPeriod()));

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


    ActivityResultLauncher<Intent> attLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), attCallback);

    ActivityResultLauncher<Intent> tutorScanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), tutorScanCallback);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = getArguments();
        mCourseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        orgLiveData = mCourseViewModel.getAllAgentCourses();
        attLiveData = mCourseViewModel.getAllAttendanceCourses();

        lecturersFP = new LinkedList<>();
        AdmNos = new LinkedList<>();
        studentsFP = new LinkedList<>();
        IDs = new LinkedList<>();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.courses, container, false);
         mRecyclerView = root.findViewById(R.id.coursesRecycler);
        SearchView searchView = root.findViewById(R.id.coursesSearchView);
        view = root.findViewById(R.id.view);
         pb = root.findViewById(R.id.coursesProgressBar);
         pb.setVisibility(View.INVISIBLE);
        mDep = args.getString("dep");
        mAdapter = new RecyclerAdapter(getContext(), itemsModelList);
        attLiveData.observe(getViewLifecycleOwner(), attendanceCourses1 -> attendanceCourses = attendanceCourses1);
        orgLiveData.observe(getViewLifecycleOwner(), agentCourses -> {
            mAgentCourses = agentCourses;
            if(agentCourses.size()>0) {
                if (mDep == null) {
                    itemsModelList.clear();
                    Set<String> courseSet = new HashSet<>();
                    for (AgentCourse course : agentCourses) {
                        courseSet.add(course.getCourse());
                    }

                    for (String course : courseSet) {
                        itemsModelList.add(new DepartmentsModel(course));
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    itemsModelList.clear();
                    Set<String> courseSet = new HashSet<>();

                    for (AgentCourse course : agentCourses) {
                        if (course.getDept().equals(mDep))
                            courseSet.add(course.getCourse());
                    }

                    for (String course : courseSet) {
                        itemsModelList.add(new DepartmentsModel(course));
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }

        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerView.setAdapter(mAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }

        });

        return root;
    }
    public void saveAddress(String mAddress) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(AddressFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Address", mAddress);

        editor.apply();
    }
    class RecyclerAdapter extends RecyclerView.Adapter<CoursesFragment.RecyclerAdapter.ItemsViewHolder> implements Filterable {
        private final List<DepartmentsModel> departmentsModelList;
        private LayoutInflater mInflater;
        private List<DepartmentsModel> departmentsModelListFiltered;
        RecyclerAdapter(Context context, List<DepartmentsModel> departmentsModelList) {
            this.departmentsModelList = departmentsModelList;
            this.departmentsModelListFiltered = departmentsModelList;
            mInflater = LayoutInflater.from(context);
        }


        class ItemsViewHolder extends RecyclerView.ViewHolder{
            final CoursesFragment.RecyclerAdapter mAdapter;
            TextView myDept;


            ItemsViewHolder(View itemView, CoursesFragment.RecyclerAdapter adapter) {
                super(itemView);
                myDept = itemView.findViewById(R.id.deptName);
                this.mAdapter = adapter;
            }
        }
        @NonNull
        @Override
        public CoursesFragment.RecyclerAdapter.ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View mItemView = mInflater.inflate(R.layout.depts_list_view, parent, false);
            return new CoursesFragment.RecyclerAdapter.ItemsViewHolder(mItemView,this);


        }

        @Override
        public void onBindViewHolder(@NonNull CoursesFragment.RecyclerAdapter.ItemsViewHolder holder, int position) {
            final String mDept = departmentsModelListFiltered.get(position).getmDept();

            holder.myDept.setText(mDept);

            holder.itemView.setOnClickListener(v -> {
                chosenCourse = holder.myDept.getText().toString();
                if (isRecordSubmitted()) {
                    if (!isDownloading) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Fetch course data?");
                        builder.setPositiveButton("Yes", (dialog, which) -> {
                            if (!getUsername().equals(NO_DATA)) getCourseData(getUsername());
                            dialog.cancel();
                        });

                        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                        AlertDialog dialog = builder.create();

                        dialog.show();
                    } else {
                        Toast.makeText(getContext(), "Please wait...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please submit record first!", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return departmentsModelListFiltered !=null? departmentsModelListFiltered.size():0;
        }

        @Override
        public Filter getFilter() {

            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();

                    if (constraint == null || constraint.length() == 0) {
                        filterResults.count = departmentsModelList.size();
                        filterResults.values = departmentsModelList;
                    } else {
                        String searchStr = constraint.toString().toLowerCase();
                        List<DepartmentsModel> resultData = new ArrayList<>();

                        for (DepartmentsModel item : departmentsModelList) {
                            if (item.getmDept().toLowerCase().contains(searchStr)) {
                                resultData.add(item);
                            }

                            filterResults.count = resultData.size();
                            filterResults.values = resultData;
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    departmentsModelListFiltered = (List<DepartmentsModel>) results.values;
                    notifyDataSetChanged();
                }
            };
            return filter;
        }

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
        SharedPreferences preferences = getActivity().getSharedPreferences("Tutors", MODE_PRIVATE);
        String defaultFP = "";
        String tutorsFP = preferences.getString("tutorsFP", defaultFP);
        String[] tutorsFPArray = tutorsFP.split(" ");
        for (String fp : tutorsFPArray) {
            lecturersFP.add(Base64.decode(fp, Base64.DEFAULT));
            count2++;
        }

        return count2>0;
    }
    private boolean isRecordSubmitted() {
        if (attendanceCourses != null) {
            for (AttendanceCourse course : attendanceCourses) {
                if (course.getCourse().equals(chosenCourse) && course.getSub_Status() == AttendanceCourse.Sub_Status.unsubmitted && course.getCap_status().equals(AttendanceCourse.Cap_Status.captured))
                    return false;
            }
        }
        return true;
    }
    private void getAtt_EnrolData(Integer position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("What would you like to do?");
        //builder.setTitle("Register Entry or Add Fingerprint");

        builder.setPositiveButton("Enrol Student", (dialog, which) -> {
                String[][] un_enrolled = getAllUn_enrolledAttendees();
                Intent intent = new Intent(getContext(), enrolActivity.class);
                if (un_enrolled.length!=0) intent.putExtra("Un_enrolled", un_enrolled);
                intent.putExtra("chosenCourse", chosenCourse);
                startActivity(intent);
        });

        builder.setNegativeButton("Take attendance", (dialog, which) -> {
            if (position==null || mAgentCourses.get(position).getAtt_status().equals(AgentCourse.Att_Status.open)) {
                if (prepareAttData()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.fgtit.reader");
                    if (!getAddress().equals("No_Address"))
                        intent.putExtra("bt_Address", getAddress());

                    intent.putExtra(ATT_COURSE, chosenCourse);
                    intent.putExtra(ATD_TUTOR_FP, lecturersFP.toArray());
                    if (studentsFP != null) intent.putExtra(ATT_STDs_FPs, studentsFP.toArray());
                    if (AdmNos != null) intent.putExtra(ATT_STDs_NOs, AdmNos.toArray());
                    if (IDs != null) intent.putExtra(ATT_STDs_IDs, IDs.toArray());
                    intent.putExtra(STUDENT_MATCH, true);

                    attLauncher.launch(intent);
                    dialog.cancel();
                } else {
                    Toast.makeText(getContext(), "An error has occured!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Attendance is closed for the course.", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private String getAddress(){
        SharedPreferences preferences = getActivity().getSharedPreferences(AddressFile, MODE_PRIVATE);
        String defaultAddress = "No_Address";
        return preferences.getString("Address", defaultAddress);
    }
    public LinkedList<byte[]> getLecturerFP() {
        SharedPreferences preferences = getActivity().getSharedPreferences("Tutors", MODE_PRIVATE);
        String defaultData = "No_FP";
        LinkedList<byte[]> lecturerFP = new LinkedList<>();
        String fpStrings = preferences.getString("tutorsFP", defaultData);
        if (!fpStrings.equals(defaultData)) {
            String[] fpStringsArray = fpStrings.split(" ");
            for (String fp : fpStringsArray) {
                lecturerFP.add(Base64.decode(fp, Base64.DEFAULT));
            }
        }
        return lecturerFP;
    }
    private void scanTutorFP() {
        Intent intent = new Intent(getActivity(), ScanTutor.class);
        intent.putExtra("passcode", getPassCode());
        tutorScanLauncher.launch(intent);

    }

    private String getPassCode() {
        SharedPreferences preferences = getActivity().getSharedPreferences("pass code file", MODE_PRIVATE);
        String defaultCode = "No_code";
        return preferences.getString("passcode", defaultCode);
    }

    private void getCourseData(String Username) {
        class DownloadFP extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb.setVisibility(View.VISIBLE);
                isDownloading = true;
            }

            @Override
            protected void onPostExecute(String data) {
                super.onPostExecute(data);
                if (data.equals("timeout")){
                    Toast.makeText(getActivity(), "Connection timeout!", Toast.LENGTH_SHORT).show();
                } else if (!data.isEmpty()){
                    try {
                        storeAttendanceData(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    scanTutorFP();
                }
                else {
                    Toast.makeText(getActivity(), "Lecturer or course details incomplete!", Toast.LENGTH_SHORT).show();

                }
                pb.setVisibility(View.INVISIBLE);
                isDownloading = false;

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
                        String post_data = URLEncoder.encode("course", "UTF-8") + "=" + URLEncoder.encode(chosenCourse, "UTF-8")
                                + "&" + URLEncoder.encode("uname", "UTF-8") + "=" + URLEncoder.encode(Username, "UTF-8");
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
            Snackbar.make(getActivity(), view,"No network connection", Snackbar.LENGTH_LONG).show();
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
            savePassCode(tutorFP.getString("passcode"));
            builder.append(tutorFP.getString("Fingerprint_staff")).append(" ");
        }
        tutorFPStrings = builder.toString().trim();
        // continue;


        JSONArray studentsArray = jsonArray.getJSONArray(1);
        for (int i=0; i<studentsArray.length(); i++) {
            JSONObject object = studentsArray.getJSONObject(i);
            mCourseViewModel.insert(new AttendanceCourse(chosenCourse,
                    object.getString("adnumber"),
                    Base64.decode(object.getString("Fingerprint_student"), Base64.DEFAULT),
                    AttendanceCourse.Sub_Status.unsubmitted,
                    AttendanceCourse.Cap_Status.uncaptured , null, 0));

        }

        JSONArray codeArray = jsonArray.getJSONArray(2);
        JSONObject obj = codeArray.getJSONObject(0);
        String code = obj.getString("code");
        storeCode(code);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("Tutors", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("tutorsFP", tutorFPStrings);
        editor.apply();

    }

    private void savePassCode(String passcode) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("pass code file", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("passcode", passcode);
        editor.apply();
    }

    private void storeCode(String code) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("code file", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("code", code);
        editor.apply();
    }

    private String getUsername(){
        SharedPreferences preferences = getActivity().getSharedPreferences(ID_FILE, MODE_PRIVATE);
        return preferences.getString(USERNAME, NO_DATA);
    }
    public boolean isWifiCxd() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo!=null && networkInfo.isConnected();

    }
    private void savePeriod(int period) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("period file", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("period", period);
        editor.apply();
    }

    private int getPeriod() {
        SharedPreferences preferences = getActivity().getSharedPreferences("period file", MODE_PRIVATE);
        int defaultPeriod = 0;
        return preferences.getInt("Address", defaultPeriod);
    }
}