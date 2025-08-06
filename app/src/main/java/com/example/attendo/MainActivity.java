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

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_SUBJECTS = "subjects";

    private EditText etNewSubject;
    private Button btnAddSubject;
    private LinearLayout subjectButtonContainer;
    private SharedPreferences prefs;
    private ArrayList<String> subjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNewSubject = findViewById(R.id.etNewSubject);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        subjectButtonContainer = findViewById(R.id.subjectButtonContainer);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        subjectList = new ArrayList<>(prefs.getStringSet(KEY_SUBJECTS, new HashSet<String>()));
        renderSubjectButtons();

        btnAddSubject.setOnClickListener(v -> {
            String subject = etNewSubject.getText().toString().trim();
            if (!TextUtils.isEmpty(subject) && !subjectList.contains(subject)) {
                subjectList.add(subject);
                saveSubjects();
                renderSubjectButtons();
                etNewSubject.setText("");
            }
        });
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
            
            subjectButtonContainer.addView(btn);
        }
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
        editor.apply();
    }

    private void saveSubjects() {
        prefs.edit().putStringSet(KEY_SUBJECTS, new HashSet<>(subjectList)).apply();
    }
}