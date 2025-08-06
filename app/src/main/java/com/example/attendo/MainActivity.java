package com.example.attendo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.text.TextUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_SUBJECTS = "subjects";
    private static final String KEY_TOTAL_PREFIX = "total_";
    private static final String KEY_ATTENDED_PREFIX = "attended_";
    private static final int MIN_ATTENDANCE_PERCENT = 75;

    private TextView tvAttendancePercent, tvClassesInfo;
    private Button btnMarkAttendance, btnAddSubject;
    private EditText etNewSubject;
    private Spinner spinnerSubjects;
    private SharedPreferences prefs;
    private ArrayAdapter<String> subjectAdapter;
    private ArrayList<String> subjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAttendancePercent = findViewById(R.id.tvAttendancePercent);
        tvClassesInfo = findViewById(R.id.tvClassesInfo);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        etNewSubject = findViewById(R.id.etNewSubject);
        spinnerSubjects = findViewById(R.id.spinnerSubjects);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        subjectList = new ArrayList<>(prefs.getStringSet(KEY_SUBJECTS, new HashSet<String>()));
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjects.setAdapter(subjectAdapter);

        btnAddSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = etNewSubject.getText().toString().trim();
                if (!TextUtils.isEmpty(subject) && !subjectList.contains(subject)) {
                    subjectList.add(subject);
                    subjectAdapter.notifyDataSetChanged();
                    saveSubjects();
                    etNewSubject.setText("");
                }
            }
        });

        btnMarkAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = getSelectedSubject();
                if (subject != null) {
                    markAttendance(subject);
                }
            }
        });

        spinnerSubjects.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateUI();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                updateUI();
            }
        });

        updateUI();
    }

    private void saveSubjects() {
        prefs.edit().putStringSet(KEY_SUBJECTS, new HashSet<>(subjectList)).apply();
    }

    private String getSelectedSubject() {
        if (subjectList.isEmpty()) return null;
        return subjectList.get(spinnerSubjects.getSelectedItemPosition());
    }

    private void markAttendance(String subject) {
        String totalKey = KEY_TOTAL_PREFIX + subject;
        String attendedKey = KEY_ATTENDED_PREFIX + subject;
        int total = prefs.getInt(totalKey, 0) + 1;
        int attended = prefs.getInt(attendedKey, 0) + 1;
        prefs.edit().putInt(totalKey, total).putInt(attendedKey, attended).apply();
        updateUI();
    }

    private void updateUI() {
        String subject = getSelectedSubject();
        if (subject == null) {
            tvAttendancePercent.setText(R.string.select_subject);
            tvClassesInfo.setText("");
            btnMarkAttendance.setEnabled(false);
            return;
        }
        btnMarkAttendance.setEnabled(true);
        String totalKey = KEY_TOTAL_PREFIX + subject;
        String attendedKey = KEY_ATTENDED_PREFIX + subject;
        int total = prefs.getInt(totalKey, 0);
        int attended = prefs.getInt(attendedKey, 0);
        double percent = total == 0 ? 0 : (attended * 100.0 / total);
        tvAttendancePercent.setText(getString(R.string.attendance, percent));
        tvClassesInfo.setText(getClassesInfo(total, attended));
    }

    private String getClassesInfo(int total, int attended) {
        if (total == 0) {
            return getString(R.string.no_classes_marked);
        }
        int minRequired = (int) Math.ceil(MIN_ATTENDANCE_PERCENT / 100.0 * total);
        if (attended >= minRequired) {
            int canMiss = 0;
            int tempTotal = total;
            int tempAttended = attended;
            while (true) {
                tempTotal++;
                int minReq = (int) Math.ceil(MIN_ATTENDANCE_PERCENT / 100.0 * tempTotal);
                if (tempAttended < minReq) break;
                canMiss++;
            }
            return getString(R.string.can_miss_classes, canMiss);
        } else {
            int needAttend = 0;
            int tempTotal = total;
            int tempAttended = attended;
            while (true) {
                tempTotal++;
                tempAttended++;
                int minReq = (int) Math.ceil(MIN_ATTENDANCE_PERCENT / 100.0 * tempTotal);
                if (tempAttended >= minReq) break;
                needAttend++;
            }
            return getString(R.string.need_attend_classes, needAttend + 1);
        }
    }
}