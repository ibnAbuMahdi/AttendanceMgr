package com.example.attendancemgr.ui.login;

import android.app.Activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.attendancemgr.MainActivity2;
import com.example.attendancemgr.R;
import com.example.attendancemgr.data.model.LoggedInUser;
import com.example.attendancemgr.database.AgentCourse;
import com.example.attendancemgr.database.AttendanceCourse;
import com.example.attendancemgr.database.CourseViewModel;
import com.example.attendancemgr.database.EnrolCourse;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.example.attendancemgr.data.LoginDataSource.DEFAULT_DISPLAY_NAME;

public class LoginActivity extends AppCompatActivity {
    private String isFirst, mUsername;
    private LoginViewModel loginViewModel;
    private CourseViewModel mCourseViewModel;
    public static final String ID_FILE = "id file";
    public static final String USERNAME = "table id";
    ArrayList<String[]> mCoursesData;
    private LoggedInUser fetchedUser;
    public static final String AddressFile = "bt_address_file";
    private ProgressBar loadingProgressBar;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private static final String VALID_USER = "valid user";
    private static final String INVALID_USER = "invalid user[]";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        mCourseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        Thread t = new Thread(() -> isFirst = mCourseViewModel.getAnyCourse().length<1? "yes":"yes");
        t.start();
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);

        mCoursesData = new ArrayList<>();

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                try {
                    updateUiWithUser(loginResult.getSuccess());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setResult(Activity.RESULT_OK);

            //Complete and destroy login activity once successful
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (isWifiCxd()) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    mUsername = usernameEditText.getText().toString();
                    authUser(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), isFirst);
                } else {
                    Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
           /* String[] days = {"Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};*/
            if (isWifiCxd()) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                mUsername = usernameEditText.getText().toString();
                authUser(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), isFirst);
            } else {
                Toast.makeText(LoginActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
            }
          /*  Calendar cal = Calendar.getInstance();
            Toast.makeText(this, days[cal.get(Calendar.DAY_OF_WEEK)-1], Toast.LENGTH_SHORT).show();

*/
        });
    }

    private void updateUiWithUser(LoggedInUserView model) throws JSONException {
        if (model.getmData().equals(DEFAULT_DISPLAY_NAME)){
            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
            startActivity(intent);
        } else {
            storeUsername(mUsername);
            storeCoursesData(model.getmData());
            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
            startActivity(intent);
        }
        resetTextfields();
        loadingProgressBar.setVisibility(View.GONE);
    }

    private void resetTextfields() {
        usernameEditText.setText("");
        passwordEditText.setText("");
    }



    private void storeCoursesData(String getmData) throws JSONException {
        mCourseViewModel.deleteAllAgentCourses();
        JSONArray jsonArray = new JSONArray(getmData);
        for (int i = 0 ; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            AgentCourse course = new AgentCourse(obj.getString("fac"), obj.getString("dept"),
                    obj.getString("course"),
                    AgentCourse.Att_Status.closed,
                    AgentCourse.Sub_Status.unsubmitted);
            mCourseViewModel.insert(course);
        }

    }

    private void storeUsername(String mUsername) {
        SharedPreferences sharedPref = getSharedPreferences(ID_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USERNAME, mUsername);
        editor.apply();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        resetTextfields();
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();

    }
    public boolean isWifiCxd() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo!=null && networkInfo.isConnected();

    }
    private LoggedInUser authUser(String username, String password, String isFirst){
        class authUserAsynctask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String userDetails) {
                super.onPostExecute(userDetails);

                if (userDetails != null ) {
                    if (userDetails.equals(INVALID_USER)) {
                        showLoginFailed(R.string.login_failed);
                    } else if (userDetails.equals(VALID_USER)) {
                        fetchedUser = new LoggedInUser(DEFAULT_DISPLAY_NAME);
                        try {
                            updateUiWithUser(new LoggedInUserView(fetchedUser.getmData()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        fetchedUser = new LoggedInUser(userDetails);
                        try {
                            updateUiWithUser(new LoggedInUserView(fetchedUser.getmData()));
                        } catch (JSONException e) {
                            //Toast.makeText(LoginActivity.this, "Invalid data format", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                resetTextfields();
                loadingProgressBar.setVisibility(View.GONE);

            }


            @Override
            protected String doInBackground(String... strings) {
                String login_url = "http://www.gstcbunza2012.org.ng/fas/authentication.php";
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
                        String post_data = URLEncoder.encode("uname", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8") + "&" +
                                URLEncoder.encode("pword", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8") + "&" +
                                URLEncoder.encode("first", "UTF-8") + "=" + URLEncoder.encode(strings[2], "UTF-8");
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

                        if (rslt.length() > 0) {
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
        authUserAsynctask authUserAsynctask = new authUserAsynctask();
        authUserAsynctask.execute(username, password, isFirst);
        return fetchedUser;
    }

}