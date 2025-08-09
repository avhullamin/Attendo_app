package com.example.attendo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.Toast;

public class SubjectActivity extends AppCompatActivity {
    private static final String EXTRA_SUBJECT = "subject";
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_TOTAL_PREFIX = "total_";
    private static final String KEY_ATTENDED_PREFIX = "attended_";
    private static final String KEY_ATTENDANCE_HISTORY_PREFIX = "history_";
    private static final int MIN_ATTENDANCE_PERCENT = 85;

    private String subject;
    private SharedPreferences prefs;
    private TextView tvAttendancePercent, tvClassesInfo, tvAttendanceStats;
    private Button btnPresent, btnAbsent, btnViewHistory, btnViewNotes, btnExtraClass, btnNotes;

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
        btnPresent = findViewById(R.id.btnPresent);
        btnAbsent = findViewById(R.id.btnAbsent);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnViewNotes = findViewById(R.id.btnViewNotes);
        btnExtraClass = findViewById(R.id.btnExtraClass);
        btnNotes = findViewById(R.id.btnNotes);

        btnPresent.setOnClickListener(v -> markAttendance(true, false));
        btnAbsent.setOnClickListener(v -> markAttendance(false, false));
        btnExtraClass.setOnClickListener(v -> markAttendance(true, true));
        btnViewHistory.setOnClickListener(v -> showAttendanceHistory());
        btnViewNotes.setOnClickListener(v -> showNotesHistory());
        btnNotes.setOnClickListener(v -> showNotesDialog());

        updateUI();
    }

    private void markAttendance(boolean isPresent, boolean isExtraClass) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String dailyKey = "daily_" + subject + "_" + today;
        String extraDailyKey = "extra_daily_" + subject + "_" + today;
        
        if (isExtraClass) {
            int extraCount = prefs.getInt(extraDailyKey, 0);
            if (extraCount >= 2) {
                Toast.makeText(this, "You can only mark 2 extra classes per day.", Toast.LENGTH_SHORT).show();
                return;
            }
            showExtraClassDialog(isPresent);
        } else {
            int regularCount = prefs.getInt(dailyKey, 0);
            if (regularCount >= 1) {
                Toast.makeText(this, "You can only mark attendance once per day for regular classes.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveAttendanceWithNotes(isPresent, "", false);
        }
    }

    private void showExtraClassDialog(boolean isPresent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Extra Class - Add Notes (Optional)");

        final EditText input = new EditText(this);
        input.setHint("Enter any notes about this extra class...");
        builder.setView(input);

        builder.setPositiveButton("Mark Present", (dialog, which) -> {
            String notes = input.getText().toString().trim();
            saveAttendanceWithNotes(isPresent, notes, true);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showNotesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Notes");

        final EditText input = new EditText(this);
        input.setHint("Enter your notes...");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String notes = input.getText().toString().trim();
            if (!notes.isEmpty()) {
                saveNotes(notes);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveAttendanceWithNotes(boolean isPresent, String notes, boolean isExtraClass) {
        String totalKey = KEY_TOTAL_PREFIX + subject;
        String attendedKey = KEY_ATTENDED_PREFIX + subject;
        String historyKey = KEY_ATTENDANCE_HISTORY_PREFIX + subject;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String dailyKey = "daily_" + subject + "_" + today;
        String extraDailyKey = "extra_daily_" + subject + "_" + today;
        
        int total = prefs.getInt(totalKey, 0) + 1;
        int attended = prefs.getInt(attendedKey, 0);
        if (isPresent) attended++;
        
        // Save attendance data
        prefs.edit()
            .putInt(totalKey, total)
            .putInt(attendedKey, attended)
            .apply();
        
        // Update daily counters
        if (isExtraClass) {
            int extraCount = prefs.getInt(extraDailyKey, 0) + 1;
            prefs.edit().putInt(extraDailyKey, extraCount).apply();
        } else {
            prefs.edit().putInt(dailyKey, 1).apply();
        }
        
        // Save attendance history with date and notes
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String type = isExtraClass ? "Extra Class" : "Regular";
        String attendanceEntry = currentDate + ":" + (isPresent ? "Present" : "Absent") + " (" + type + ")";
        if (!notes.isEmpty()) {
            attendanceEntry += " - " + notes;
        }
        
        // Get existing history and add new entry
        String existingHistory = prefs.getString(historyKey, "");
        String newHistory = existingHistory.isEmpty() ? attendanceEntry : existingHistory + "|" + attendanceEntry;
        prefs.edit().putString(historyKey, newHistory).apply();
        
        updateUI();
    }

    private void saveNotes(String notes) {
        String notesKey = "notes_" + subject;
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String noteEntry = currentDate + ": " + notes;
        
        String existingNotes = prefs.getString(notesKey, "");
        String newNotes = existingNotes.isEmpty() ? noteEntry : existingNotes + "|" + noteEntry;
        prefs.edit().putString(notesKey, newNotes).apply();
        
        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
    }

    private void showAttendanceHistory() {
        String historyKey = KEY_ATTENDANCE_HISTORY_PREFIX + subject;
        String history = prefs.getString(historyKey, "");
        
        if (history.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("Attendance History")
                .setMessage("No attendance records found.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }
        
        String[] entries = history.split("\\|");
        StringBuilder historyText = new StringBuilder();
        
        for (int i = entries.length - 1; i >= 0; i--) { // Show most recent first
            if (!entries[i].isEmpty()) {
                historyText.append(entries[i]).append("\n");
            }
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Attendance History")
            .setMessage(historyText.toString())
            .setPositiveButton("OK", null)
            .show();
    }

    private void showNotesHistory() {
        String notesKey = "notes_" + subject;
        String notes = prefs.getString(notesKey, "");
        
        if (notes.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("Notes History")
                .setMessage("No notes found for this subject.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }
        
        String[] entries = notes.split("\\|");
        StringBuilder notesText = new StringBuilder();
        
        for (int i = entries.length - 1; i >= 0; i--) { // Show most recent first
            if (!entries[i].isEmpty()) {
                notesText.append(entries[i]).append("\n\n");
            }
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Notes History")
            .setMessage(notesText.toString())
            .setPositiveButton("OK", null)
            .show();
    }

    private void updateUI() {
        String totalKey = KEY_TOTAL_PREFIX + subject;
        String attendedKey = KEY_ATTENDED_PREFIX + subject;
        int total = prefs.getInt(totalKey, 0);
        int attended = prefs.getInt(attendedKey, 0);
        int missed = total - attended;
        double percent = total == 0 ? 0 : (attended * 100.0 / total);
        
        tvAttendancePercent.setText(String.format(Locale.getDefault(), getString(R.string.attendance), percent));
        tvAttendanceStats.setText(getString(R.string.attendance_stats, attended, missed, total));
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