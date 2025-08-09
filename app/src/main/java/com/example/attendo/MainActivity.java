package com.example.attendo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_SUBJECTS = "subjects";
    private static final int MAX_SUBJECTS = 10;

    private FloatingActionButton fabAddSubject;
    private LinearLayout subjectButtonContainer;
    private SharedPreferences prefs;
    private ArrayList<String> subjectList;
    private Button btnExportData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAddSubject = findViewById(R.id.fabAddSubject);
        subjectButtonContainer = findViewById(R.id.subjectButtonContainer);
        btnExportData = findViewById(R.id.btnExportData);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        subjectList = new ArrayList<>(prefs.getStringSet(KEY_SUBJECTS, new HashSet<String>()));
        renderSubjectButtons();

        fabAddSubject.setOnClickListener(v -> {
            if (subjectList.size() >= MAX_SUBJECTS) {
                Toast.makeText(this, "You can only add up to 10 subjects.", Toast.LENGTH_SHORT).show();
            } else {
                showAddSubjectDialog();
            }
        });

        btnExportData.setOnClickListener(v -> exportAttendanceData());
    }

    private void renderSubjectButtons() {
        subjectButtonContainer.removeAllViews();
        for (String subject : subjectList) {
            Button btn = new Button(this);
            btn.setText(subject);

            // Click to open subject page
            btn.setOnClickListener(v -> {
                Intent intent = SubjectActivity.newIntent(this, subject);
                startActivity(intent);
            });

            // Long press to delete subject
            btn.setOnLongClickListener(v -> {
                showDeleteConfirmationDialog(subject);
                return true;
            });

            // Click and hold to edit subject
            btn.setOnTouchListener((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    v.setTag(System.currentTimeMillis());
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    long pressTime = (Long) v.getTag();
                    if (System.currentTimeMillis() - pressTime > 500) {
                        showEditSubjectDialog(subject);
                        return true;
                    }
                }
                return false;
            });

            subjectButtonContainer.addView(btn);
        }
    }

    private void showAddSubjectDialog() {
        if (subjectList.size() >= MAX_SUBJECTS) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_subject);

        final EditText input = new EditText(this);
        input.setHint(R.string.enter_subject_name);
        builder.setView(input);

        builder.setPositiveButton(R.string.add_subject, (dialog, which) -> {
            String subject = input.getText().toString().trim();
            if (!TextUtils.isEmpty(subject) && !subjectList.contains(subject)) {
                subjectList.add(subject);
                saveSubjects();
                renderSubjectButtons();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmationDialog(String subject) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_subject)
                .setMessage(getString(R.string.confirm_delete, subject))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    deleteSubject(subject);
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteSubject(String subject) {
        subjectList.remove(subject);
        saveSubjects();
        renderSubjectButtons();

        // Clear attendance data for this subject
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("total_" + subject);
        editor.remove("attended_" + subject);
        editor.remove("history_" + subject);
        editor.remove("notes_" + subject);
        editor.apply();
    }

    private void showEditSubjectDialog(String oldSubject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_subject);

        final EditText input = new EditText(this);
        input.setText(oldSubject);
        input.setHint(R.string.enter_subject_name);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newSubject = input.getText().toString().trim();
            if (!TextUtils.isEmpty(newSubject) && !subjectList.contains(newSubject)) {
                int index = subjectList.indexOf(oldSubject);
                subjectList.set(index, newSubject);
                saveSubjects();
                
                // Update SharedPreferences keys
                SharedPreferences.Editor editor = prefs.edit();
                String totalKey = "total_" + oldSubject;
                String attendedKey = "attended_" + oldSubject;
                String historyKey = "history_" + oldSubject;
                String notesKey = "notes_" + oldSubject;
                
                int total = prefs.getInt(totalKey, 0);
                int attended = prefs.getInt(attendedKey, 0);
                String history = prefs.getString(historyKey, "");
                String notes = prefs.getString(notesKey, "");
                
                editor.remove(totalKey);
                editor.remove(attendedKey);
                editor.remove(historyKey);
                editor.remove(notesKey);
                
                editor.putInt("total_" + newSubject, total);
                editor.putInt("attended_" + newSubject, attended);
                editor.putString("history_" + newSubject, history);
                editor.putString("notes_" + newSubject, notes);
                editor.apply();
                
                renderSubjectButtons();
                Toast.makeText(this, "Subject updated!", Toast.LENGTH_SHORT).show();
            } else if (subjectList.contains(newSubject)) {
                Toast.makeText(this, "Subject already exists!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveSubjects() {
        prefs.edit().putStringSet(KEY_SUBJECTS, new HashSet<>(subjectList)).apply();
    }

    private void exportAttendanceData() {
        if (subjectList.isEmpty()) {
            Toast.makeText(this, "No subjects to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder exportData = new StringBuilder();
        exportData.append("ATTENDO - ATTENDANCE REPORT\n");
        exportData.append("Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n\n");

        for (String subject : subjectList) {
            String totalKey = "total_" + subject;
            String attendedKey = "attended_" + subject;
            String historyKey = "history_" + subject;
            
            int total = prefs.getInt(totalKey, 0);
            int attended = prefs.getInt(attendedKey, 0);
            double percent = total == 0 ? 0 : (attended * 100.0 / total);
            
            exportData.append("SUBJECT: ").append(subject).append("\n");
            exportData.append("Total Classes: ").append(total).append("\n");
            exportData.append("Attended: ").append(attended).append("\n");
            exportData.append("Missed: ").append(total - attended).append("\n");
            exportData.append("Attendance: ").append(String.format(Locale.getDefault(), "%.2f%%", percent)).append("\n");
            
            // Add recent history
            String history = prefs.getString(historyKey, "");
            if (!history.isEmpty()) {
                String[] entries = history.split("\\|");
                exportData.append("Recent History:\n");
                int count = 0;
                for (int i = entries.length - 1; i >= 0 && count < 5; i--) {
                    if (!entries[i].isEmpty()) {
                        exportData.append("  ").append(entries[i]).append("\n");
                        count++;
                    }
                }
            }
            exportData.append("\n");
        }

        new AlertDialog.Builder(this)
            .setTitle("Export Data")
            .setMessage(exportData.toString())
            .setPositiveButton("Copy to Clipboard", (dialog, which) -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Attendance Data", exportData.toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Data copied to clipboard!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Close", null)
            .show();
    }
}