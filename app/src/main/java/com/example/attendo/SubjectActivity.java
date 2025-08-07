package com.example.attendo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class SubjectActivity extends AppCompatActivity {
    private static final String EXTRA_SUBJECT = "subject";
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_TOTAL_PREFIX = "total_";
    private static final String KEY_ATTENDED_PREFIX = "attended_";
    private static final String KEY_EXTRA_PREFIX = "extra_";
    private static final int MIN_ATTENDANCE_PERCENT = 85;

    private String subject;
    private SharedPreferences prefs;
    private TextView tvAttendancePercent, tvClassesInfo, tvAttendanceStats, tvExtraClasses;
    private Button btnPresent, btnAbsent, btnExtraClass;

    public static Intent newIntent(Context context, String subject) {
        Intent intent = new Intent(context, SubjectActivity.class);
        intent.putExtra(EXTRA_SUBJECT, subject);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        subject = getIntent().getStringExtra(EXTRA_SUBJECT);
        if (subject == null) {
            finish();
            return;
        }

        setTitle(subject);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        tvAttendancePercent = findViewById(R.id.tvAttendancePercent);
        tvClassesInfo = findViewById(R.id.tvClassesInfo);
        tvAttendanceStats = findViewById(R.id.tvAttendanceStats);
        tvExtraClasses = findViewById(R.id.tvExtraClasses);
        btnPresent = findViewById(R.id.btnPresent);
        btnAbsent = findViewById(R.id.btnAbsent);
        btnExtraClass = findViewById(R.id.btnExtraClass);

        btnPresent.setOnClickListener(v -> markAttendance(true));
        btnAbsent.setOnClickListener(v -> markAttendance(false));
        btnExtraClass.setOnClickListener(v -> markExtraClass());

        updateUI();
    }

    private void markAttendance(boolean isPresent) {
        String totalKey = KEY_TOTAL_PREFIX + subject;
        String attendedKey = KEY_ATTENDED_PREFIX + subject;
        int total = prefs.getInt(totalKey, 0) + 1;
        int attended = prefs.getInt(attendedKey, 0);
        if (isPresent) attended++;
        prefs.edit().putInt(totalKey, total).putInt(attendedKey, attended).apply();
        updateUI();
    }

    private void markExtraClass() {
        String extraKey = KEY_EXTRA_PREFIX + subject;
        int extraClasses = prefs.getInt(extraKey, 0) + 1;
        prefs.edit().putInt(extraKey, extraClasses).apply();
        updateUI();
        Toast.makeText(this, getString(R.string.extra_class_marked), Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        String totalKey = KEY_TOTAL_PREFIX + subject;
        String attendedKey = KEY_ATTENDED_PREFIX + subject;
        String extraKey = KEY_EXTRA_PREFIX + subject;
        int total = prefs.getInt(totalKey, 0);
        int attended = prefs.getInt(attendedKey, 0);
        int missed = total - attended;
        int extraClasses = prefs.getInt(extraKey, 0);
        double percent = total == 0 ? 0 : (attended * 100.0 / total);
        
        tvAttendancePercent.setText(String.format(Locale.getDefault(), getString(R.string.attendance), percent));
        tvAttendanceStats.setText(getString(R.string.attendance_stats, attended, missed, total));
        tvExtraClasses.setText(String.format(Locale.getDefault(), getString(R.string.extra_classes), extraClasses));
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
            while (true) {
                tempTotal++;
                int minReq = (int) Math.ceil(MIN_ATTENDANCE_PERCENT / 100.0 * tempTotal);
                if (attended < minReq) break;
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