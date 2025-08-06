package com.example.attendo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_TOTAL_CLASSES = "total_classes";
    private static final String KEY_ATTENDED_CLASSES = "attended_classes";
    private static final int MIN_ATTENDANCE_PERCENT = 75;

    private TextView tvAttendancePercent, tvClassesInfo;
    private Button btnMarkAttendance;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAttendancePercent = findViewById(R.id.tvAttendancePercent);
        tvClassesInfo = findViewById(R.id.tvClassesInfo);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        btnMarkAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAttendance();
            }
        });

        updateUI();
    }

    private void markAttendance() {
        int total = prefs.getInt(KEY_TOTAL_CLASSES, 0) + 1;
        int attended = prefs.getInt(KEY_ATTENDED_CLASSES, 0) + 1;
        prefs.edit().putInt(KEY_TOTAL_CLASSES, total).putInt(KEY_ATTENDED_CLASSES, attended).apply();
        updateUI();
    }

    private void updateUI() {
        int total = prefs.getInt(KEY_TOTAL_CLASSES, 0);
        int attended = prefs.getInt(KEY_ATTENDED_CLASSES, 0);
        double percent = total == 0 ? 0 : (attended * 100.0 / total);
        tvAttendancePercent.setText("Attendance: " + String.format("%.2f", percent) + "%");
        tvClassesInfo.setText(getClassesInfo(total, attended));
    }

    private String getClassesInfo(int total, int attended) {
        if (total == 0) {
            return "No classes marked yet.";
        }
        int minRequired = (int) Math.ceil(MIN_ATTENDANCE_PERCENT / 100.0 * total);
        if (attended >= minRequired) {
            // Calculate how many more classes can be missed
            int canMiss = 0;
            int tempTotal = total;
            int tempAttended = attended;
            while (true) {
                tempTotal++;
                int minReq = (int) Math.ceil(MIN_ATTENDANCE_PERCENT / 100.0 * tempTotal);
                if (tempAttended < minReq) break;
                canMiss++;
            }
            return "You can miss " + canMiss + " more class(es) and still meet minimum attendance.";
        } else {
            // Calculate how many more classes need to attend consecutively
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
            return "You need to attend next " + (needAttend + 1) + " class(es) to reach minimum attendance.";
        }
    }
}