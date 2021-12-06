package com.example.attendancemgr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.attendancemgr.ui.Courses.CoursesFragment.TUTOR_OK;

public class ScanTutor extends AppCompatActivity {
TextView infoView;
EditText passcodeText, periodNumberText;
String passcode;
int period;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_tutor);
        infoView = findViewById(R.id.infoView);
        passcodeText = findViewById(R.id.textPasscode);
        periodNumberText = findViewById(R.id.textPeriodNumber);
        periodNumberText.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent.hasExtra("passcode")){
            passcode = intent.getExtras().getString("passcode");
        }

        passcodeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()==6){
                    if (passcode.equals(charSequence.toString())){
                        periodNumberText.setVisibility(View.VISIBLE);
                        passcodeText.setVisibility(View.INVISIBLE);
                        infoView.setText("Please input period");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        periodNumberText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>0){
                    period = Integer.parseInt(charSequence.toString());
                    Intent intent1 = new Intent();
                    intent1.putExtra("period", period);
                    setResult(TUTOR_OK, intent1);
                    finish();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        OnBackPressedDispatcher dispatcher = new OnBackPressedDispatcher();
        dispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }
}