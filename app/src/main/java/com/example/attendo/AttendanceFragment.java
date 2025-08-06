package com.example.attendo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class AttendanceFragment extends Fragment {
    private static final String ARG_SUBJECT = "subject";
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_TOTAL_PREFIX = "total_";
    private static final String KEY_ATTENDED_PREFIX = "attended_";
    private static final int MIN_ATTENDANCE_PERCENT = 85;

    private String subject;
    private SharedPreferences prefs;
    private TextView tvAttendancePercent, tvClassesInfo;
    private Button btnPresent, btnAbsent;

    public static AttendanceFragment newInstance(String subject) {
        AttendanceFragment fragment = new AttendanceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        tvAttendancePercent = view.findViewById(R.id.tvAttendancePercent);
        tvClassesInfo = view.findViewById(R.id.tvClassesInfo);
        btnPresent = view.findViewById(R.id.btnPresent);
        btnAbsent = view.findViewById(R.id.btnAbsent);

        if (getArguments() != null) {
            subject = getArguments().getString(ARG_SUBJECT);
        }

        btnPresent.setOnClickListener(v -> markAttendance(true));
        btnAbsent.setOnClickListener(v -> markAttendance(false));

        updateUI();
        return view;
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

    private void updateUI() {
        String totalKey = KEY_TOTAL_PREFIX + subject;
        String attendedKey = KEY_ATTENDED_PREFIX + subject;
        int total = prefs.getInt(totalKey, 0);
        int attended = prefs.getInt(attendedKey, 0);
        double percent = total == 0 ? 0 : (attended * 100.0 / total);
        tvAttendancePercent.setText(String.format(Locale.getDefault(), getString(R.string.attendance), percent));
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