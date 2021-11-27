package com.example.attendancemgr.data;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.example.attendancemgr.MainActivity2;
import com.example.attendancemgr.data.model.LoggedInUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.example.attendancemgr.MainActivity2.AVAILABLE_JOBS;
import static com.example.attendancemgr.MainActivity2.JOBS_FILE;
import static com.example.attendancemgr.MainActivity2.NO_DATA;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    boolean dataBack;

    public static final String DEFAULT_ID = "id";
    public static final String DEFAULT_DISPLAY_NAME = "display name";

    public Result<LoggedInUser> login(String username, String password, String isFirst) {

        try {

                return new Result.Success<LoggedInUser>(null);


        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
    }


private static class InvalidUserException extends Exception{
    @NonNull
    @Override
    public String toString() {
        return "Invalid Username or Password";
    }
    }
}